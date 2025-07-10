//--------------------------------------------------------------------------------------------------
// System Programming                         I/O Lab                                    Spring 2025
//
/// @file
/// @brief resursively traverse directory tree and list all entries
/// @author <yourname>
/// @studid <studentid>
//--------------------------------------------------------------------------------------------------

#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <dirent.h>
#include <errno.h>
#include <unistd.h>
#include <stdarg.h>
#include <assert.h>
#include <grp.h>
#include <pwd.h>

#define MAX_DIR 64            ///< maximum number of supported directories

/// @brief output control flags
#define F_TREE      0x1       ///< enable tree view
#define F_SUMMARY   0x2       ///< enable summary
#define F_VERBOSE   0x4       ///< turn on verbose mode

/// @brief struct holding the summary
struct summary {
  unsigned int dirs;          ///< number of directories encountered
  unsigned int files;         ///< number of files
  unsigned int links;         ///< number of links
  unsigned int fifos;         ///< number of pipes
  unsigned int socks;         ///< number of sockets

  unsigned long long size;    ///< total size (in bytes)
  unsigned long long blocks;  ///< total number of blocks (512 byte blocks)
};


/// @brief abort the program with EXIT_FAILURE and an optional error message
///
/// @param msg optional error message or NULL
void panic(const char *msg)
{
  if (msg) fprintf(stderr, "%s\n", msg);
  exit(EXIT_FAILURE);
}


/// @brief read next directory entry from open directory 'dir'. Ignores '.' and '..' entries
///
/// @param dir open DIR* stream
/// @retval entry on success
/// @retval NULL on error or if there are no more entries
struct dirent *getNext(DIR *dir)
{
  struct dirent *next;
  int ignore;

  do {
    errno = 0;
    next = readdir(dir);
    if (errno != 0) perror(NULL);
    ignore = next && ((strcmp(next->d_name, ".") == 0) || (strcmp(next->d_name, "..") == 0));
  } while (next && ignore);

  return next;
}


/// @brief qsort comparator to sort directory entries. Sorted by name, directories first.
///
/// @param a pointer to first entry
/// @param b pointer to second entry
/// @retval -1 if a<b
/// @retval 0  if a==b
/// @retval 1  if a>b
static int dirent_compare(const void *a, const void *b)
{
  struct dirent *e1 = (struct dirent*)a;
  struct dirent *e2 = (struct dirent*)b;

  // if one of the entries is a directory, it comes first
  if (e1->d_type != e2->d_type) {
    if (e1->d_type == DT_DIR) return -1;
    if (e2->d_type == DT_DIR) return 1;
  }

  // otherwise sorty by name
  return strcmp(e1->d_name, e2->d_name);
}

void updateStats(struct summary *stats, struct stat *fileStat, unsigned char d_type) {
  if(d_type == DT_REG) stats->files++;
  else if(d_type == DT_DIR)  stats->dirs++;
  else if(d_type == DT_LNK)  stats->links++;
  else if(d_type == DT_FIFO) stats->fifos++;
  else if(d_type == DT_SOCK) stats->socks++;
  
  stats->size += fileStat->st_size;
  stats->blocks += fileStat->st_blocks;
 
}

//d_type을 사람이 읽을 수 있는 char로 변환환
char getDT(unsigned d_type){
  switch(d_type){
    case DT_REG:
      return(' ');
    case DT_DIR:
      return('d');
    case DT_BLK:
      return('b');
    case DT_FIFO:
      return('f');
    case DT_LNK:
      return('l');
    case DT_CHR:
      return('c');
    case DT_SOCK:
      return('s');
    default:
      perror("getDT");
      return('\0');
  }
}

// Generate a new prefix string (new_pstr) for the entries of the current directory
// by appropriately processing the prefix string (pstr) used by the current directory.
char *getNewPstr(const char *pstr, int i, int num_entries, unsigned int flags){

  char *new_pstr=NULL;
  if(flags & F_TREE) {
    if(pstr && strlen(pstr)>0){
      if(asprintf(&new_pstr,"%.*s%s%s",(int)strlen(pstr)-2,pstr,(pstr[strlen(pstr)-2]=='|')?"| ":"  ", (i==num_entries-1)?"`-":"|-") == -1) {
        return NULL;
      }
    }
    else {
      if(asprintf(&new_pstr,"%s",(i==num_entries-1)?"`-":"|-") == -1) {
        return NULL;
      }
    }
  }
  else {
    if(asprintf(&new_pstr,"%s  ",pstr) == -1) {
      return NULL;
    }
  }

  return new_pstr;
}

//Generate new directory name(new_dn) 
//by concatenating entry_name to the current directory name(dn)
char *getNewDn(const char *dn, const char *entry_name) {

  char *new_dn;
  if(asprintf(&new_dn,"%s/%s",dn,entry_name) == -1) {
    return NULL;
  }
  return new_dn;
}

//Generate new print string(new_print)
//by concatenating entry_name to the current prefix string(pstr)
char *getNewPrint(const char *pstr, const char *entry_name) {
  char *new_print;
  if(asprintf(&new_print,"%s%s",pstr,entry_name) == -1) {
    return NULL;
  }
  return new_print;
}


/// @brief recursively process directory @a dn and print its tree
///
/// @param dn absolute or relative path string
/// @param pstr prefix string printed in front of each entry
/// @param stats pointer to statistics
/// @param flags output control flags (F_*)
void processDir(const char *dn, const char *pstr, struct summary *stats, unsigned int flags)
{
  // open directory
  DIR *dir = opendir(dn);
  if (dir == NULL) { //error handling
    char *new_pstr=getNewPstr(pstr,0,1,flags);
    char *errormsg=NULL;

    if(asprintf(&errormsg,"%sERROR",new_pstr) == -1) { //error error handling... is it really necessary?
      free(new_pstr);                      
      perror("make errormsg");
      return;
    }

    perror(errormsg);

    free(errormsg);
    free(new_pstr);
    return;
  }


  // read all entries from directory
  struct dirent *entries=NULL;
  struct dirent *entry;
  int num_entries = 0;
  int capacity = MAX_DIR;

  entries = malloc(capacity*sizeof(struct dirent));
  while((entry=getNext(dir))!=NULL) {
    if(num_entries>=capacity) {
      capacity *= 2;
      struct dirent *new_entries = realloc(entries, (capacity)*sizeof(struct dirent));
      if(new_entries == NULL) {
        perror("realloc");
        free(entries);
        return;
      }
      entries = new_entries;
    }
    entries[num_entries++] = *entry; 
  }

  closedir(dir);



  // sort entries
  qsort(entries, num_entries, sizeof(struct dirent), dirent_compare);



  // print sorted entries
  for(int i=0; i<num_entries; i++) {

    entry = &entries[i];

    char *new_pstr = getNewPstr(pstr, i, num_entries, flags); 
    if(new_pstr==NULL){ //error handling
      perror("new_pstr");
      return;
    }

    char *new_dn = getNewDn(dn, entry->d_name); 
    if(new_dn==NULL){ //error handling
      perror("new_dn");
      return;
    } 

    char *new_print = getNewPrint(new_pstr, entry->d_name);
    if(new_print==NULL){ //error handling
      perror("new_print");
      return;
    }

    // get file statistics
    struct stat fileStat;
    if (lstat(new_dn, &fileStat) == -1) {
      perror("stat");
      return;
    }

    updateStats(stats, &fileStat, entry->d_type);  


    // print entry
    if(flags&F_VERBOSE) {
      if(strlen(new_print)<=54) printf("%-54s",new_print);
      else printf("%-51.51s...",new_print);
  
      printf("  %8.8s:%-8.8s  %10ld  %8ld  %c\n",getpwuid(fileStat.st_uid)->pw_name, getgrgid(fileStat.st_gid)->gr_name, fileStat.st_size, fileStat.st_blocks, getDT(entry->d_type));
    }
    else {
      if(strlen(new_print)<=54) printf("%s\n",new_print);
      else printf("%-51.51s...\n",new_print);
    }
    if(entry->d_type == DT_DIR) {
      processDir(new_dn, new_pstr, stats, flags);
    }
       
    free(new_pstr);
    free(new_dn);  
    free(new_print);  
  } 
}


/// @brief print program syntax and an optional error message. Aborts the program with EXIT_FAILURE
///
/// @param argv0 command line argument 0 (executable)
/// @param error optional error (format) string (printf format) or NULL
/// @param ... parameter to the error format string
void syntax(const char *argv0, const char *error, ...)
{
  if (error) {
    va_list ap;

    va_start(ap, error);
    vfprintf(stderr, error, ap);
    va_end(ap);

    printf("\n\n");
  }

  assert(argv0 != NULL);

  fprintf(stderr, "Usage %s [-t] [-s] [-v] [-h] [path...]\n"
                  "Gather information about directory trees. If no path is given, the current directory\n"
                  "is analyzed.\n"
                  "\n"
                  "Options:\n"
                  " -t        print the directory tree (default if no other option specified)\n"
                  " -s        print summary of directories (total number of files, total file size, etc)\n"
                  " -v        print detailed information for each file. Turns on tree view.\n"
                  " -h        print this help\n"
                  " path...   list of space-separated paths (max %d). Default is the current directory.\n",
                  basename(argv0), MAX_DIR);

  exit(EXIT_FAILURE);
}


/// @brief program entry point
int main(int argc, char *argv[])
{
  //
  // default directory is the current directory (".")
  //
  const char CURDIR[] = ".";
  const char *directories[MAX_DIR];
  int   ndir = 0;

  struct summary tstat;
  unsigned int flags = 0;

  //
  // parse arguments
  //
  for (int i = 1; i < argc; i++) {
    if (argv[i][0] == '-') {
      // format: "-<flag>"
      if      (!strcmp(argv[i], "-t")) flags |= F_TREE;
      else if (!strcmp(argv[i], "-s")) flags |= F_SUMMARY;
      else if (!strcmp(argv[i], "-v")) flags |= F_VERBOSE;
      else if (!strcmp(argv[i], "-h")) syntax(argv[0], NULL);
      else syntax(argv[0], "Unrecognized option '%s'.", argv[i]);
    } else {
      // anything else is recognized as a directory
      if (ndir < MAX_DIR) {
        directories[ndir++] = argv[i];
      } else {
        printf("Warning: maximum number of directories exceeded, ignoring '%s'.\n", argv[i]);
      }
    }
  }

  // if no directory was specified, use the current directory
  if (ndir == 0) directories[ndir++] = CURDIR;


  //
  // process each directory
  //
  // TODO
  //
  // Pseudo-code
  // - reset statistics (tstat)
  // - loop over all entries in 'directories' (number of entires stored in 'ndir')
  //   - reset statistics (dstat)
  //   - if F_SUMMARY flag set: print header
  //   - print directory name
  //   - call processDir() for the directory
  //   - if F_SUMMARY flag set: print summary & update statistics

  // reset statistics (tstat)
  memset(&tstat, 0, sizeof(tstat));

  struct summary dstat;

  for (int i = 0; i < ndir; i++) {

    // reset statistics (dstat)
    memset(&dstat, 0, sizeof(dstat));

    // print header
    if (flags & F_SUMMARY) {
      if (flags & F_VERBOSE)
        printf("Name                                                        User:Group           Size    Blocks Type\n");
      else
        printf("Name\n");
      printf("----------------------------------------------------------------------------------------------------\n");
    }

    // print directory name
    printf("%s\n", directories[i]);

    // call processDir() for the directory
    processDir(directories[i], "", &dstat, flags);

    //print summary
    if (flags & F_SUMMARY) {
      printf("----------------------------------------------------------------------------------------------------\n");
      char *new_print;
      int new_print_len=asprintf(&new_print,"%d file%s, %d director%s, %d link%s, %d pipe%s, and %d socket%s", 
        dstat.files, dstat.files==1?"":"s", dstat.dirs, dstat.dirs==1?"y":"ies", dstat.links, dstat.links==1?"":"s", dstat.fifos, dstat.fifos==1?"":"s", dstat.socks, dstat.socks==1?"":"s");
      if(new_print_len<=68) printf("%-68s",new_print);
      else printf("%-65.65s...",new_print);
      
      free(new_print);

      if(flags & F_VERBOSE) printf("   %14lld %9lld", dstat.size, dstat.blocks);
      printf("\n\n");

    }

    // update total statistics
    tstat.dirs += dstat.dirs;
    tstat.files += dstat.files;
    tstat.links += dstat.links;
    tstat.fifos += dstat.fifos;
    tstat.socks += dstat.socks;
    tstat.size += dstat.size;
    tstat.blocks += dstat.blocks;
  }


  //...


  //
  // print grand total
  //
  if ((flags & F_SUMMARY) && (ndir > 1)) {
    printf("Analyzed %d directories:\n"
           "  total # of files:        %16d\n"
           "  total # of directories:  %16d\n"
           "  total # of links:        %16d\n"
           "  total # of pipes:        %16d\n"
           "  total # of sockets:      %16d\n",
           ndir, tstat.files, tstat.dirs, tstat.links, tstat.fifos, tstat.socks);

    if (flags & F_VERBOSE) {
      printf("  total file size:         %16llu\n"
             "  total # of blocks:       %16llu\n",
             tstat.size, tstat.blocks);
    }

  }

  //
  // that's all, folks!
  //
  return EXIT_SUCCESS;
}

