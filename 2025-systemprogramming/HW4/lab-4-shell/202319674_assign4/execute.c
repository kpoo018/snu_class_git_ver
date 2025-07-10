/*---------------------------------------------------------------------------*/
/* execute.c                                                                 */
/* Author: Jongki Park, Kyoungsoo Park                                       */
/*---------------------------------------------------------------------------*/

#include "dynarray.h"
#include "token.h"
#include "util.h"
#include "lexsyn.h"
#include "snush.h"
#include "execute.h"
#include "job.h"

extern struct job_manager *manager;
/*---------------------------------------------------------------------------*/
void redout_handler(char *fname) {
	//
	// TODO: redout_handler() in execute.c
	// 
	int fd;

	fd = open(fname, O_WRONLY | O_CREAT | O_TRUNC, 644);
	if (fd < 0) {
		error_print(NULL, PERROR);
		exit(EXIT_FAILURE);
	}
	else {
		dup2(fd, STDOUT_FILENO);
		close(fd);
	}
	//
	// TODO: redout_handler() in execute.c
	// 
}
/*---------------------------------------------------------------------------*/
void redin_handler(char *fname) {
	int fd;

	fd = open(fname, O_RDONLY);
	if (fd < 0) {
		error_print(NULL, PERROR);
		exit(EXIT_FAILURE);
	}
	else {
		dup2(fd, STDIN_FILENO);
		close(fd);
	}
}
/*---------------------------------------------------------------------------*/
int build_command_partial(DynArray_T oTokens, int start, 
						int end, char *args[]) {
	int i, ret = 0, redin = FALSE, redout = FALSE, cnt = 0;
	struct Token *t;

	/* Build command */
	for (i = start; i < end; i++) {

		t = dynarray_get(oTokens, i);

		if (t->token_type == TOKEN_WORD) {
			if (redin == TRUE) {
				redin_handler(t->token_value);
				redin = FALSE;
			}
			else if (redout == TRUE) {
				redout_handler(t->token_value);
				redout = FALSE;
			}
			else
				args[cnt++] = t->token_value;
		}
		else if (t->token_type == TOKEN_REDIN)
			redin = TRUE;
		else if (t->token_type == TOKEN_REDOUT)
			redout = TRUE;
	}
	args[cnt] = NULL;

#ifdef DEBUG
	for (i = 0; i < cnt; i++)
	{
		if (args[i] == NULL)
			printf("CMD: NULL\n");
		else
			printf("CMD: %s\n", args[i]);
	}
	printf("END\n");
#endif
	return ret;
}
/*---------------------------------------------------------------------------*/
int build_command(DynArray_T oTokens, char *args[]) {
	return build_command_partial(oTokens, 0, 
								dynarray_get_length(oTokens), args);
}
/*---------------------------------------------------------------------------*/
void execute_builtin(DynArray_T oTokens, enum BuiltinType btype) {
	int ret;
	char *dir = NULL;
	struct Token *t1;

	switch (btype) {
	case B_EXIT:
		if (dynarray_get_length(oTokens) == 1) {
			// printf("\n");
			dynarray_map(oTokens, free_token, NULL);
			dynarray_free(oTokens);

			exit(EXIT_SUCCESS);
		}
		else
			error_print("exit does not take any parameters", FPRINTF);

		break;

	case B_CD:
		if (dynarray_get_length(oTokens) == 1) {
			dir = getenv("HOME");
			if (dir == NULL) {
				error_print("cd: HOME variable not set", FPRINTF);
				break;
			}
		}
		else if (dynarray_get_length(oTokens) == 2) {
			t1 = dynarray_get(oTokens, 1);
			if (t1->token_type == TOKEN_WORD)
				dir = t1->token_value;
		}

		if (dir == NULL) {
			error_print("cd takes one parameter", FPRINTF);
			break;
		}
		else {
			ret = chdir(dir);
			if (ret < 0)
				error_print(NULL, PERROR);
		}
		break;

	default:
		error_print("Bug found in execute_builtin", FPRINTF);
		exit(EXIT_FAILURE);
	}
}
/*---------------------------------------------------------------------------*/
void wait_fg(int jobid) {
	while (1) {
        struct job *job = find_job_by_jid(jobid);

        if (job == NULL) {
            break;
        }

        if (job->state != foreground) {
            break;
        }
        sleep(0);
    }

    return;
}
/*---------------------------------------------------------------------------*/
void print_job(int jobid, pid_t pgid) {
    fprintf(stdout, 
		"[%d] Process group: %d running in the background\n", jobid, pgid);
}
/*---------------------------------------------------------------------------*/
int fork_exec(DynArray_T oTokens, int is_background) {
	//
	// TODO-START: fork_exec() in execute.c
	//
	// If you want to run the newely forked process in the foreground, 
	// call wait_fg(). If you want to run the newely forked process 
	// in the background, call print_job().
	// All the exited processes should be handled in 
	// the sigchld_handler() in snush.c.
	// 
	char *args[MAX_ARGS_CNT + 1];
    pid_t pid;
    int jobid = manager->n_jobs + 1;

    struct job *j = malloc(sizeof(struct job));
    j->job_id   = give_job_id();
    j->state    = is_background ? background : foreground;
    j->pgid     = 0;             
    j->total_num = 1;
    j->curr_num  = 0;
    j->pid_list = malloc(sizeof(pid_t) * j->total_num);
    j->next     = manager->jobs;
    manager->jobs = j;
    manager->n_jobs++;

    

    pid = fork();
    if (pid < 0) {
        error_print(NULL, PERROR);
        return -1;
    }
    else if (pid == 0) {
		build_command(oTokens, args);

        setpgid(0, 0);
		if(!is_background)
			tcsetpgrp(STDIN_FILENO, getpid());
        execvp(args[0], args);

        error_print(args[0], PERROR);
		exit(EXIT_FAILURE);
    }
    else {
        j->pgid = pid;
        setpgid(pid, j->pgid);

        j->pid_list[j->curr_num++] = pid;

        if (is_background) {
            print_job(j->job_id, j->pgid);
        } else {
            wait_fg(j->job_id);
			tcsetpgrp(STDIN_FILENO, getpid());
        }
    }

    return jobid;
	
	//
	// TODO-END: fork_exec() in execute.c
	//
}
/*---------------------------------------------------------------------------*/
int iter_pipe_fork_exec(int n_pipe, DynArray_T oTokens, int is_background) {
	//
	// TODO-START: iter_pipe_fork_exec() in execute.c
	//
	// If you want to run the newely forked process in the foreground, 
	// call wait_fg(). If you want to run the newely forked process 
	// in the background, call print_job().
	// All the exited processes should be handled in 
	// the sigchld_handler() in snush.c.
	//
	
	int n_cmds = n_pipe + 1;
    char *args[MAX_ARGS_CNT + 1];
    pid_t pid;
    int jobid = give_job_id();

    // 1) Job 등록
    struct job *j = malloc(sizeof(*j));
    j->job_id    = jobid;
    j->state     = is_background ? background : foreground;
    j->pgid      = 0;
    j->total_num = n_cmds;
    j->curr_num  = 0;
    j->pid_list  = malloc(sizeof(pid_t) * n_cmds);
    j->next      = manager->jobs;
    manager->jobs = j;
    manager->n_jobs++;

    // 2) 파이프 연결 변수
    int prev_fd = -1, fds[2];
    int start = 0, length = dynarray_get_length(oTokens);

    for (int i = 0; i < n_cmds; i++) {
        // 토큰 범위 계산 (start .. end-1)
        int end = start;
        while (end < length) {
            struct Token *t = dynarray_get(oTokens, end);
            if (t->token_type == TOKEN_PIPE) break;
            end++;
        }

        if (i < n_pipe) {
            if (pipe(fds) < 0) {
                error_print(NULL, PERROR);
                return -1;
            }
        }

        // 3) fork
        pid = fork();
        if (pid < 0) {
            error_print(NULL, PERROR);
            return -1;
        }
        else if (pid == 0) {
            // └── 자식: 프로세스 그룹 설정
            setpgid(0, j->pgid ? j->pgid : getpid());
            if (!is_background)
                tcsetpgrp(STDIN_FILENO, getpgrp());

            // 이전 파이프 읽기 끝 → STDIN
            if (prev_fd != -1) {
                dup2(prev_fd, STDIN_FILENO);
                close(prev_fd);
            }
            // 새 파이프 쓰기 끝 → STDOUT
            if (i < n_pipe) {
                close(fds[0]);
                dup2(fds[1], STDOUT_FILENO);
                close(fds[1]);
            }

            // 토큰(start..end) → args 벡터 구성
            build_command_partial(oTokens, start, end, args);
            execvp(args[0], args);
            error_print(args[0], PERROR);
            _exit(EXIT_FAILURE);
        }
        else {
            // └── 부모: 프로세스 그룹에 자식 추가
            if (j->pgid == 0) {
                j->pgid = pid;
                setpgid(pid, pid);
                if (!is_background)
                    tcsetpgrp(STDIN_FILENO, pid);
            } else {
                setpgid(pid, j->pgid);
            }

            // PID 리스트 등록
            j->pid_list[j->curr_num++] = pid;

            // 이전 파이프 닫기
            if (prev_fd != -1) close(prev_fd);
            // 쓰기 끝 닫고, 읽기 끝을 다음 prev_fd로
            if (i < n_pipe) {
                close(fds[1]);
                prev_fd = fds[0];
            }

            // 다음 명령 start 위치 설정 (파이프 토큰 건너뛰기)
            start = end + 1;
        }
    }

    // 4) 포그라운드/백그라운드 분기
    if (is_background) {
        print_job(j->job_id, j->pgid);
    } else {
        wait_fg(j->job_id);
        // 제어권을 쉘로 복귀
        tcsetpgrp(STDIN_FILENO, getpid());
    }

    return jobid;
	
	//
	// TODO-END: iter_pipe_fork_exec() in execute.c
	//
}
/*---------------------------------------------------------------------------*/