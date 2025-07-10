#include <stdio.h>

int main(){
    int *p = 0, *q = 1;
    scanf("%d", &p);
    q=(int *)&p;
    printf("1=%d 2=%d\n", *q, *(q+1));
    return 0;
}