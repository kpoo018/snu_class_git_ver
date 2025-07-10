/*--------------------------------------------------------------------*/
/* heapmgr1.c                                                          */
/* Author: Donghwi Kim, KyoungSoo Park, 양정욱                         */
/*--------------------------------------------------------------------*/

#include <stdio.h>
#include <stdlib.h>
#include <assert.h>

#include "chunk.h"

#define FALSE 0
#define TRUE  1

enum {
   MEMALLOC_MIN = 1024,
};

/* g_free_head: point to first chunk in the free list */
static Chunk_T g_free_head = NULL;

/* g_heap_start, g_heap_end: start and end of the heap area.
 * g_heap_end will move if you increase the heap */
static void *g_heap_start = NULL, *g_heap_end = NULL;

#ifndef NDEBUG
/* check_heap_validity:
 * Validity check for entire data structures for chunks. Note that this
 * is basic sanity check, and passing this test does not guarantee the
 * integrity of your code. 
 * Returns 1 on success or 0 (zero) on failure. 
 */
static int
check_heap_validity(void)
{
   Chunk_T w;

   if (g_heap_start == NULL) {
      fprintf(stderr, "Uninitialized heap start\n");
      return FALSE;
   }

   if (g_heap_end == NULL) {
      fprintf(stderr, "Uninitialized heap end\n");
      return FALSE;
   }

   if (g_heap_start == g_heap_end) {
      if (g_free_head == NULL)
         return 1;
      fprintf(stderr, "Inconsistent empty heap\n");
      return FALSE;
   }

   for (w = (Chunk_T)g_heap_start; 
        w && w < (Chunk_T)g_heap_end;
        w = chunk_get_next_adjacent(w, g_heap_start, g_heap_end)) {

      if (!chunk_is_valid(w, g_heap_start, g_heap_end)) 
         return 0;
   }

   for (w = g_free_head; w; w = chunk_get_ptr(w)) {
      Chunk_T n;

      if (chunk_get_status(w) != CHUNK_FREE) {
         fprintf(stderr, "Non-free chunk in the free chunk list\n");
         return 0;
      }

      if (!chunk_is_valid(w, g_heap_start, g_heap_end))
         return 0;

      n = chunk_get_next_adjacent(w, g_heap_start, g_heap_end);
      if (n != NULL && n == chunk_get_ptr(w)) {
         fprintf(stderr, "Uncoalesced chunks\n");
         return 0;
      }
      if(chunk_get_ptr(w) == g_free_head)
         break;
   }
   return TRUE;
}
#endif

/*--------------------------------------------------------------*/
/* size_to_units:
 * Returns capable number of units for 'size' bytes. 
 */
/*--------------------------------------------------------------*/
static size_t
size_to_units(size_t size)
{
  return (size + (CHUNK_UNIT-1))/CHUNK_UNIT;
}
/*--------------------------------------------------------------*/
/* get_header_from_data_ptr:
 * Returns the header pointer that contains data 'm'. 
 */
/*--------------------------------------------------------------*/
static Chunk_T
get_header_from_data_ptr(void *m)
{
  return (Chunk_T)((char *)m - CHUNK_UNIT);
}
/*--------------------------------------------------------------------*/
/* init_my_heap: 
 * Initialize data structures and global variables for
 * chunk management. 
 */
/*--------------------------------------------------------------------*/
static void
init_my_heap(void)
{
   /* Initialize g_heap_start and g_heap_end */
   g_heap_start = g_heap_end = sbrk(0);
   if (g_heap_start == (void *)-1) {
      fprintf(stderr, "sbrk(0) failed\n");
      exit(-1);
   }
}


/*--------------------------------------------------------------------*/
/* insert_chunk_to_free_head:
 * Insert a chunk, 'c', into the head of the free chunk list. 
 * The status of 'c' is set to CHUNK_FREE
 */
/*--------------------------------------------------------------------*/
static void
insert_chunk_to_free_head(Chunk_T c)
{
   Chunk_T c_footer=chunk_get_footer(c, g_heap_start, g_heap_end);
   assert (c_footer != NULL && c < c_footer);
   assert (chunk_get_units(c) >= 1);
   assert (chunk_get_status(c) == CHUNK_IN_USE);

   chunk_set_status(c, CHUNK_FREE);
   chunk_set_status(c_footer, CHUNK_FREE);

   /* If the free chunk list is empty, chunk c points to itself. */
   if (g_free_head == NULL) {
      g_free_head = c;
      chunk_set_ptr(c, c);
      chunk_set_ptr(c_footer, c);
   }
   else {
      Chunk_T g_free_head_footer = chunk_get_footer(g_free_head, g_heap_start, g_heap_end);
      assert (g_free_head_footer != NULL && g_free_head < g_free_head_footer);

      Chunk_T last = chunk_get_ptr(g_free_head_footer);
      assert (last != NULL);

      chunk_set_ptr(c, g_free_head);
      chunk_set_ptr(c_footer, last);

      chunk_set_ptr(last, c);
      chunk_set_ptr(g_free_head_footer, c);
      
      g_free_head = c;
   }
}

/*--------------------------------------------------------------------*/
/* remove_chunk_from_list:
 * Removes 'c' from the free chunk list.
 * If 'c' is the only chunk, head of that bin be NULL 
 */
/*--------------------------------------------------------------------*/
static void
remove_chunk_from_list(Chunk_T c)
{
   Chunk_T c_footer = chunk_get_footer(c, g_heap_start, g_heap_end);
   assert (chunk_get_status(c) == CHUNK_FREE);
   assert (c_footer !=NULL && chunk_get_status(c_footer) == CHUNK_FREE);
   
   Chunk_T prev = chunk_get_ptr(c_footer);  
   Chunk_T next = chunk_get_ptr(c);
   assert (prev != NULL);
   assert (next != NULL);
   
   if (prev == c && next == c) { 
      /* c is the only chunk in the free list */
      g_free_head = NULL;

   } 
   else {
      Chunk_T next_footer = chunk_get_footer(next, g_heap_start, g_heap_end);
      assert (next_footer != NULL && chunk_get_status(next_footer) == CHUNK_FREE);
      chunk_set_ptr(prev, next);
      chunk_set_ptr(next_footer, prev);

      if (g_free_head == c) {
         assert (next != c);
         g_free_head = next;
      }
   }
   chunk_set_ptr(c, NULL);
   chunk_set_ptr(c_footer, NULL);
   chunk_set_status(c, CHUNK_IN_USE);
   chunk_set_status(c_footer, CHUNK_IN_USE);
}

/*--------------------------------------------------------------------*/
/* split_chunk:
 * Split 'c' into two chunks s.t. the size of one chunk is 'units' and
 * the size of the other chunk is (original size - 'units' - 1).
 * returns the chunk with 'units'
 * Returns the data chunk. */
/*--------------------------------------------------------------------*/
static Chunk_T 
split_chunk(Chunk_T c, size_t units)
{
   Chunk_T c2, c_new_footer, c2_footer;
   size_t all_units;

   assert (c >= (Chunk_T)g_heap_start && c <= (Chunk_T)g_heap_end);
   assert (chunk_get_status(c) == CHUNK_FREE);
   assert (chunk_get_units(c) > units + 2); /* assume chunk with header and footer unit */

   remove_chunk_from_list(c);
   
   /* adjust the first chunk */
   all_units = chunk_get_units(c);
   chunk_set_units(c, all_units - units - 2);

   c_new_footer = chunk_get_footer(c, g_heap_start, g_heap_end);
   assert (c_new_footer != NULL);

   chunk_set_units(c_new_footer, all_units - units - 2);
   chunk_set_status(c_new_footer, CHUNK_IN_USE);
   assert (chunk_get_status(c_new_footer) == chunk_get_status(c));

   insert_chunk_to_free_head(c);
   
   /* prepare for the second chunk */
   c2 = chunk_get_next_adjacent(c, g_heap_start, g_heap_end);
   assert (c2 != NULL && c_new_footer<c2);
   chunk_set_units(c2, units);
   chunk_set_status(c2, CHUNK_IN_USE);
   chunk_set_ptr(c2, NULL);

   c2_footer = chunk_get_footer(c2, g_heap_start, g_heap_end);
   assert (c2_footer != NULL && c2 < c2_footer);
   chunk_set_units(c2_footer, units);
   chunk_set_status(c2_footer, CHUNK_IN_USE);
   chunk_set_ptr(c2_footer, NULL);
   assert (chunk_get_status(c2_footer) == chunk_get_status(c2));

   return c2;
}

/*--------------------------------------------------------------------*/
/* insert_chunk:
 * Insert a chunk, 'c', into the free chunk list. 
 * The status of 'c' is set to CHUNK_FREE
 * If 'c' can be merged with adjacent chunks, it will be merged and
 * inserted into the free list. 
 */
static Chunk_T
insert_chunk(Chunk_T c) {
   Chunk_T n = chunk_get_next_adjacent(c, g_heap_start, g_heap_end);
   Chunk_T p_footer = chunk_get_prev_adjacent_footer(c, g_heap_start, g_heap_end);
   Chunk_T c_footer = chunk_get_footer(c, g_heap_start, g_heap_end);
   assert (c_footer != NULL && c < c_footer);

   bool p_free = (p_footer != NULL && chunk_get_status(p_footer) == CHUNK_FREE);
   bool n_free = (n != NULL && chunk_get_status(n) == CHUNK_FREE);

   if (p_free && n_free) {
      Chunk_T p = chunk_get_header(p_footer, g_heap_start, g_heap_end);
      assert (p != NULL); 
      assert (chunk_get_status(p) == CHUNK_FREE);

      Chunk_T n_footer = chunk_get_footer(n, g_heap_start, g_heap_end);
      assert (n_footer != NULL && n < n_footer);
      assert (chunk_get_status(n_footer) == CHUNK_FREE);

      remove_chunk_from_list(p);
      remove_chunk_from_list(n);

      int new_units = chunk_get_units(p) + chunk_get_units(c) + chunk_get_units(n) + 4; /* +4 for headers and footers */

      chunk_set_units(p, new_units);
      chunk_set_units(n_footer, new_units);
      c=p;

   } else if (n_free) {
      Chunk_T n_footer = chunk_get_footer(n, g_heap_start, g_heap_end);
      assert (n_footer != NULL && n < n_footer);
      assert (chunk_get_status(n_footer) == CHUNK_FREE);

      remove_chunk_from_list(n);

      int new_units = chunk_get_units(c) + chunk_get_units(n) + 2; /* +2 for header and footer */
      chunk_set_units(c, new_units);
      chunk_set_units(n_footer, new_units);
 

   } else if (p_free) {
      Chunk_T p = chunk_get_header(p_footer, g_heap_start, g_heap_end);
      assert (p != NULL); 
      assert (chunk_get_status(p) == CHUNK_FREE);

      remove_chunk_from_list(p);

      int new_units = chunk_get_units(p) + chunk_get_units(c) + 2; /* +2 for header and footer */
      chunk_set_units(p, new_units);
      chunk_set_units(c_footer, new_units);
      c = p;
   }

   insert_chunk_to_free_head(c);
   return c;

}


/*--------------------------------------------------------------------*/
/* allocate_more_memory: 
 * Allocate a new chunk which is capable of holding 'units' chunk
 * units in memory by increasing the heap, and return the new
 * chunk. 'prev' should be the last chunk in the free list.
 * This function also performs chunk merging with "prev" if possible
 * after allocating a new chunk. 
*/
/*--------------------------------------------------------------------*/
static Chunk_T
allocate_more_memory(size_t units)
{
   Chunk_T c;

   if (units < MEMALLOC_MIN)
      units = MEMALLOC_MIN;
   
   /* Note that we need to allocate two more units for header and footer. */
   c = (Chunk_T)sbrk((units + 2) * CHUNK_UNIT);
   if (c == (Chunk_T)-1)
      return NULL;
   
   g_heap_end = sbrk(0);
   chunk_set_units(c, units);
   chunk_set_ptr(c, NULL);
   chunk_set_status(c, CHUNK_IN_USE);

   Chunk_T c_footer = chunk_get_footer(c, g_heap_start, g_heap_end);
   assert (c_footer != NULL && c < c_footer);
   chunk_set_units(c_footer, units);
   chunk_set_ptr(c_footer, NULL);
   chunk_set_status(c_footer, CHUNK_IN_USE);
   
   /* Insert the newly allocated chunk 'c' to the free list. */
   c = insert_chunk(c);

   assert(check_heap_validity());
   return c;
}
/*--------------------------------------------------------------*/
/* heapmgr_malloc:
 * Dynamically allocate a memory capable of holding size bytes. 
 * Substitute for GNU malloc().                                 
 */
/*--------------------------------------------------------------*/
void *
heapmgr_malloc(size_t size)
{
   static int is_init = FALSE;
   Chunk_T c=g_free_head;
   size_t units;
   
   if (size <= 0)
      return NULL;
   
   if (is_init == FALSE) {
      init_my_heap();
      is_init = TRUE;
   }
   /* see if everything is OK before doing any operations */
   assert(check_heap_validity());

   units = size_to_units(size);

   if (c != NULL) {

      if (chunk_get_units(c) >= units) {
         if (chunk_get_units(c) > units + 2) 
            c = split_chunk(c, units);
         else
            remove_chunk_from_list(c);
         
         assert(check_heap_validity());
         return (void *)((char *)c + CHUNK_UNIT);
      }
      
      for (c = chunk_get_ptr(c); 
         c != g_free_head; 
         c = chunk_get_ptr(c)) {
 
       if (chunk_get_units(c) >= units) {
          if (chunk_get_units(c) > units + 2) 
             c = split_chunk(c, units);
          else
             remove_chunk_from_list(c);
          
          assert(check_heap_validity());
          return (void *)((char *)c + CHUNK_UNIT);
       }
       
    }
   }
   
   
   /* allocate new memory */
   c = allocate_more_memory(units);
   if (c == NULL) {
     assert(check_heap_validity());
     return NULL;
   }
   assert(chunk_get_units(c) >= units);

   if (chunk_get_units(c) > units + 2) 
      c = split_chunk(c, units);
   else 
      remove_chunk_from_list(c);
      
   assert(check_heap_validity());
   return (void *)((char *)c + CHUNK_UNIT);
}
/*--------------------------------------------------------------*/
/* heapmgr_free:
 * Releases dynamically allocated memory. 
 * Substitute for GNU free().                                   */
/*--------------------------------------------------------------*/
void
heapmgr_free(void *m)
{
   Chunk_T c;
   
   if (m == NULL)
      return;

   /* check everything is OK before freeing 'm' */
   assert(check_heap_validity());

   /* get the chunk header pointer from m */
   c = get_header_from_data_ptr(m);
   
   assert (chunk_get_status(c) != CHUNK_FREE);
   
   
   c=insert_chunk(c);

   assert(check_heap_validity());
}