/************************************************
 *                                              *
 *   2023-2 Programming Practice Term Project   *
 *         Name: 양정욱
 *         Student Number: 2023-19674           *
 *                                              *
 ************************************************/
//추가 규칙으로 사과를 먹을 시 뱀의 꼬리와 머리가 바뀌어서 반대방향으로 가도록 만들었습니다.
//이를 위해 changeHead라는 함수를 만들어서 사용하였습니다.

#include <time.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// do not modify the grid size
#define GRID_WIDTH 20
#define GRID_HEIGHT 20

// you can adjust constants below 
#define INITIAL_SNAKE_LENGTH 3
#define INITIAL_SNAKE_X 5
#define INITIAL_SNAKE_Y 5
#define INITIAL_SNAKE_DIRECTION 0 // 0: up, 1: right, 2: down, 3: left
#define MAX_APPLE 1
#define SPEED 5 // higher is faster

typedef struct {
    char *name;
    char *id;
} StudentInfo;

//뱀을 linkedlist로 저장
struct slist{
    struct slist *prev;
    int x,y,dir; //x,y는 위치를 dir은 방향을 저장
    struct slist *next;
};
typedef struct slist snakelist;

// do not edit this struct; it is used to send data to frontend
typedef struct {
    char name[50]; // Assuming name won't exceed 50 characters
    int score;
} LeaderboardEntry;

StudentInfo student;

// the grid that is sent to frontend
// the characters stored in array will show in grid
// if the cell contains '*', it would be rendered green. you can use it to draw the snake.
// if the cell contains '@', it would be rendered as an apple. you can use it to place and draw an apple.
char grid[GRID_HEIGHT][GRID_WIDTH];

// reads in backend
int snakeLength = INITIAL_SNAKE_LENGTH;
int gameOver = 0; // 0 for not over, 1 for game over
snakelist *head=NULL;
snakelist *tail=NULL;
int apple_x=-1;
int apple_y=-1;

int snakeDirection = INITIAL_SNAKE_DIRECTION; // 0: Up, 1: Right, 2: Down, 3: Left
int snakeHeadX = GRID_WIDTH / 2;
int snakeHeadY = GRID_HEIGHT / 2;
LeaderboardEntry list[5];


// The functions here are used in the backend to communicate with the frontend
// The parameters and return values are defined in the frontend code - you should not change them
LeaderboardEntry* getLeaderboardData();
int getLeaderboardSize();
void initializeGame();
void updateGame();
void setDirection(const char* direction);
void handleGameOver();
void appendToLeaderboard(const char *name, int score);
void setapple();
int kda();
void changeHead();


// test variable
// remove it when you implement the game
int i = 0;


// returns a list of leaderboard entries sorted in descending order
// higher score first
// if same score, sort alphabetically, but Anonymous should come last
LeaderboardEntry* getLeaderboardData() {
    // write your code here
    int size=getLeaderboardSize();
    LeaderboardEntry temp;
    FILE *fp=fopen("leaderboard.txt","r");
    for(int i=0;i<size;i++){
        fscanf(fp,"%s %d",list[i].name,&list[i].score);
    }

    for(int j=0;j<size;j++){
        for(int i=0;i<size-1;i++){
            temp = list[i];
            if(list[i].score==list[i+1].score){
                if(!strcmp(list[i].name,"Anonymus")){
                    list[i]=list[i+1];
                    list[i+1]=temp;
                }
                if(strcasecmp(list[i].name,list[i+1].name)>-1){
                    list[i]=list[i+1];
                    list[i+1]=temp;
                }
            }
            if(list[i].score<list[i+1].score){
                list[i]=list[i+1];
                list[i+1]=temp;
            }
        }
    } //sorting

    while(fscanf(fp,"%s %d",temp.name,&temp.score)!=EOF){
        
        for(int i=0;i<size;i++){
            if(temp.score>list[i].score){
                for(int j=size-1;j>i;j--){
                    list[j]=list[j-1];
                }
                list[i]=temp;
                break;
            }
            if(temp.score==list[i].score){
                if(!strcmp(temp.name,"Anonymus")) continue;
                if(strcasecmp(temp.name,list[i].name)<1){
                    for(int j=size-1;j>i;j--){
                        list[j]=list[j-1];
                    }
                    list[i]=temp;
                    break;
                }
            }
        
        }
    } //sorting 

    
    fclose(fp);
    return list;
}

// returns the number of entries to display in the leaderboard
// the frontend will only display up to top 5 entries
// return 5 if there are more than 5 entries, otherwise return the number of entries
int getLeaderboardSize() {
    FILE *fp=fopen("leaderboard.txt","r");
    char line[255];
    int cnt=0;
    while (fgets(line, sizeof(line), fp) != NULL ) {
	    cnt++;
        if(cnt>=5) break;
        }
    fclose(fp);
    return cnt;

}


// initialize states for a new game
void initializeGame() {
    /* TO DO */
    student.name = "양정욱";  // put your name
    student.id = "2023-19674"; // put your student id

    memset(grid, ' ', sizeof(grid));
    // write your code here

    snakeLength=INITIAL_SNAKE_LENGTH;
    snakeDirection=INITIAL_SNAKE_DIRECTION;

    head=malloc(sizeof(snakelist));
    head->prev=NULL;
    head->x=snakeHeadX;
    head->dir=INITIAL_SNAKE_DIRECTION;
    head->y=snakeHeadY;
    tail=head;
    for(int i=0;i<3;i++){
        tail->next=malloc(sizeof(snakelist));
        tail->next->prev=tail;
        tail=tail->next;
        tail->x=(tail->prev)->x;
        tail->y=(tail->prev)->y+1;
        tail->dir=INITIAL_SNAKE_DIRECTION;
        tail->next=NULL;
    }

    snakelist *current=head;
    while(current!=NULL){
        grid[current->y][current->x]='*';
        current=current->next;
    }

    setapple();
    grid[apple_y][apple_x]='@';

    gameOver = 0;

    // below is test code
    // i = 0;
}


// calls every update cycle 
// (default: 1 sec, interval depends on SPEED)
void updateGame() {
    // write your code here
    // below is a test code.

    if(gameOver==1) return;
    
    head->prev=malloc(sizeof(snakelist));
    head->prev->next=head;
    head=head->prev;
    head->dir=snakeDirection;

    switch(snakeDirection){
        case 0:
        head->x=head->next->x;
        head->y=head->next->y-1;
        break;

        case 1:
        head->x=head->next->x+1;
        head->y=head->next->y;
        break;

        case 2:
        head->x=head->next->x;
        head->y=head->next->y+1;
        break;

        case 3:
        head->x=head->next->x-1;
        head->y=head->next->y;
        break;
    }

    if(!(head->x==apple_x&&head->y==apple_y)){
        tail=tail->prev;
        free(tail->next);
        tail->next=NULL;
    }

    if(!kda()){
        handleGameOver();
        return;
    }

    if(head->x==apple_x&&head->y==apple_y){
        snakeLength++;
        changeHead();
        setapple();
    }

    memset(grid, ' ', sizeof(grid));

    snakelist *current=head;
    while(current!=NULL){
        grid[current->y][current->x]='*';
        current=current->next;
    }
    
    grid[apple_y][apple_x]='@';

}

// get direction input and apply it to snake
// inputs are: "UP" "RIGHT" "DOWN" "LEFT"
void setDirection(const char* direction) {
    int inputdir;
    if(!strcmp(direction,"UP")) {
        inputdir=0;
    }
    if(!strcmp(direction,"RIGHT")) {
        inputdir=1;   
    }
    if(!strcmp(direction,"DOWN")) {
        inputdir=2; 
    }
    if(!strcmp(direction,"LEFT")) {
        inputdir=3;
    }

    if((head->dir+2)%4!=inputdir){
        snakeDirection=inputdir;
    }
    // write your code here
}

// calls when game is over
// you should set gameOver to 1 here
void handleGameOver() {

    snakelist *current=head->next;
    while(current->next!=NULL){
        free(current->prev);
        current->prev=NULL;
        current=current->next;
    }
    free(current);

    gameOver = 1;
}

// add a new entry to leaderboard
// write to file leaderboard.txt
// set gameOver to 0 again because gameOver handling is complete
void appendToLeaderboard(const char *name, int score) {
    // write your code here
    FILE *fp= fopen("leaderboard.txt","a");
    if(!strcmp(name,"")){
        fprintf(fp,"%s %d\n","Anonymus",score);
    }
    else{
        fprintf(fp,"%s %d\n",name,score);
    }
    
    fclose(fp);
    //gameOver = 0;

}

//빈공간에 사과를 랜덤하게 생성
void setapple(){
    snakelist *current=head;
    apple_x=rand()%20;
    apple_y=rand()%20;
    while(current!=NULL){
        if(current->x==apple_x&&current->y==apple_y){
            srand(time(NULL));
            apple_x=rand()%20;
            apple_y=rand()%20;
            current=head;
            continue;
        }
        current=current->next;
    }

    
}

//죽었는지 확인 죽었으면 0, 살았으면 1을 return
int kda(){
    if(head->x<0||head->x>=GRID_WIDTH||head->y<0||head->y>=GRID_HEIGHT){
        return 0;
    }
    snakelist *current=head->next;
    while(current!=NULL){
        if(head->x==current->x&&head->y==current->y){
            return 0;
        }
        current=current->next;
    }
    return 1;
}

//뱀의 꼬리와 머리 바꾸기
void changeHead(){
    snakelist *temp1,*temp2,*current;
    temp1=head;
    head=tail;
    tail=temp1;

    head->next=head->prev;
    head->prev=NULL;
    head->dir=(head->dir+2)%4;

    tail->prev=tail->next;
    tail->next=NULL;
    tail->dir=(tail->dir+2)%4;

    current=head->next;

    while(current!=tail){
        temp2=current->next;
        current->next=current->prev;
        current->prev=temp2;
        current->dir=(current->dir+2)%4;
        current=current->next;
    }

    snakeDirection=head->dir;

}
