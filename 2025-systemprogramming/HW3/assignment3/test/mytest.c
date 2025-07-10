/*--------------------------------------------------------------------*/
/* testheapmgr.c                                                      */
/* Author: Bob Dondero                                                */
/* modifyed by Juyoung Park                                           */
/*--------------------------------------------------------------------*/

#include "heapmgr.h"
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <string.h>
#include <assert.h>
#include <sys/time.h>
#include <sys/resource.h>

#ifndef __USE_MISC
#define __USE_MISC
#endif
#include <unistd.h>
int main(int argc, char *argv[])
{
   int *f=heapmgr_malloc(sizeof(int));
   heapmgr_free(f);
}