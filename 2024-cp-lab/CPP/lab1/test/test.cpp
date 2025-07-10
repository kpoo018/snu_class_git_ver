#include <iostream>

typedef float t_f;

struct student {
   int age;
   char name[20];
   t_f grade;
};

typedef struct student t_student;

int main()
{
   t_f grade = 4.3;
   t_student s1 = {25, "Junguk Yang", grade};
   t_student *ptr = &s1;

}