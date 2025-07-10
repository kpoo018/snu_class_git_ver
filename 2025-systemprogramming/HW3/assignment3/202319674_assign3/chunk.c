/*--------------------------------------------------------------------*/
/* chunkbase.c                                                        */
/* Author: Donghwi Kim, KyoungSoo Park, 양정욱                         */
/*--------------------------------------------------------------------*/

#include <stdio.h>
#include <stddef.h>
#include <unistd.h>
#include <stdlib.h>
#include <assert.h>

#include "chunk.h"

struct Chunk {
   Chunk_T ptr;       /* Pointer to the next chunk in the free chunk list if this is header*/
                       /* Or Pointer to the prev chunk if this is footer */
   int units;          /* Capacity of a chunk (chunk units) */
   int status;         /* CHUNK_FREE or CHUNK_IN_USE */
};

/*--------------------------------------------------------------------*/
int
chunk_get_status(Chunk_T c)
{
   return c->status;
}
/*--------------------------------------------------------------------*/
void
chunk_set_status(Chunk_T c, int status)
{
   c->status = status;
}
/*--------------------------------------------------------------------*/
int
chunk_get_units(Chunk_T c)
{
   return c->units;
}
/*--------------------------------------------------------------------*/
void
chunk_set_units(Chunk_T c, int units)
{
   c->units = units;
}
/*--------------------------------------------------------------------*/
Chunk_T
chunk_get_ptr(Chunk_T c)
{
  return c->ptr;
}
/*--------------------------------------------------------------------*/
void
chunk_set_ptr(Chunk_T c, Chunk_T ptr)
{
   c->ptr = ptr;
}
/*--------------------------------------------------------------------*/
Chunk_T
chunk_get_next_adjacent(Chunk_T c, void* start, void* end)
{
    Chunk_T n;

    assert((void *)c >= start);

    /* Note that a chunk consists of one chunk unit for a header, and
    * many chunk units for data. */
    n = c + c->units + 2; /* +2 for header and footer */

    /* If 'c' is the last chunk in memory space, then return NULL. */
    if ((void *)n >= end)
        return NULL;

    return n;
}
/*--------------------------------------------------------------------*/
Chunk_T
chunk_get_prev_adjacent_footer(Chunk_T c, void* start, void* end)
{
    Chunk_T prev_footer;

    assert((void *)c < end);

    /* Note that a chunk consists of one chunk unit for a header, and
    * many chunk units for data. */
    prev_footer = c - 1;

    /* If 'c' is the first chunk in memory space, then return NULL. */
    if ((void *)prev_footer <=start)
        return NULL;

    return prev_footer;
}
/*--------------------------------------------------------------------*/
Chunk_T
chunk_get_footer(Chunk_T c, void* start, void* end)
{
    Chunk_T c_footer;

    assert((void *)c >= start);

    /* Note that a chunk consists of one chunk unit for a header, and
        * many chunk units for data. */
    c_footer = c + c->units + 1; /* +1 for footer */

    assert((void *)c_footer <= end);

    return c_footer;
}
/*--------------------------------------------------------------------*/
Chunk_T
chunk_get_header(Chunk_T c_footer, void* start, void* end)
{
    Chunk_T c;

    assert((void *)c_footer  <= end);

    c = c_footer - c_footer->units - 1; /* -1 for header */

    assert((void *)c >= start);

    return c;
}

#ifndef NDEBUG
/*--------------------------------------------------------------------*/
int 
chunk_is_valid(Chunk_T c, void *start, void *end)
/* Return 1 (TRUE) iff c is valid */
{
   assert(c != NULL);
   assert(start != NULL);
   assert(end != NULL);

   if (c < (Chunk_T)start)
      {fprintf(stderr, "Bad heap start\n"); return 0; }
   if (c >= (Chunk_T)end)
      {fprintf(stderr, "Bad heap end\n"); return 0; }
   if (c->units == 0)
      {fprintf(stderr, "Zero units\n"); return 0; }
   return 1;
}
#endif