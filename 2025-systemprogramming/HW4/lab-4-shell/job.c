/*---------------------------------------------------------------------------*/
/* job.c                                                                     */
/* Author: Jongki Park, Kyoungsoo Park                                       */
/*---------------------------------------------------------------------------*/

#include "job.h"

extern struct job_manager *manager;
int job_id = 0;
/*---------------------------------------------------------------------------*/
void init_job_manager() {
	manager = (struct job_manager *)calloc(1, sizeof(struct job_manager));
	if (manager == NULL) {
		fprintf(stderr, "[Error] job manager allocation failed\n");
		exit(EXIT_FAILURE);
	}
}
/*---------------------------------------------------------------------------*/
struct job *find_job_by_jid(int job_id) {
    if (manager == NULL) {
        fprintf(stderr, "[Error] find_job_by_jid: Job manager is NULL\n");
        return NULL;
    }

    struct job *current = manager->jobs;

    while (current != NULL) {
        if (current->job_id == job_id) {
            return current;
        }
        current = current->next;
    }

    return NULL;
}
/*---------------------------------------------------------------------------*/
struct job *find_job_fg() {
    if (manager == NULL) {
        fprintf(stderr, "[Error] find_job_fg: Job manager is NULL\n");
        return NULL;
    }

    struct job *current = manager->jobs;

    while (current != NULL) {
        if (current->state == foreground) {
            return current;
        }
        current = current->next;
    }

    return NULL;
}
/*---------------------------------------------------------------------------*/
//
// TODO: new job control functions in job.c start
// 
int give_job_id() {
    if (manager == NULL) {
        fprintf(stderr, "[Error] give_job_id: Job manager is NULL\n");
        return -1;
    }

    return job_id++; // Return the next job ID
}


void add_done_bg_job(struct job *j) {
    if (manager == NULL) {
        fprintf(stderr, "[Error] add_done_bg_job: Job manager is NULL\n");
        return;
    }

    j->next = NULL; 

    if (manager->done_bg_jobs == NULL) {
        manager->done_bg_jobs = j;
    } else {
        struct job *tail = manager->done_bg_jobs;
        while (tail->next) {
            tail = tail->next;
        }
        tail->next = j;
    }
}

void reset_job_id() {
    job_id = 0; // Reset the job ID counter
}
//
// TODO: new job control functions in job.c end
// 
/*---------------------------------------------------------------------------*/