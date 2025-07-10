#include <iostream>

struct student {
  int age;
  char name[20];
  float grade;

  void changeGrade(float (*func_ptr)(float)) {
    this->grade = func_ptr(this->grade);
  }
};


float up(float f) { return f + 1; }

float down(float f) { return f - 1; }

int main() {
  struct student s1 = {25, "Jaejin Lee", 4.3};

  s1.changeGrade(down);

  std::cout << s1.grade << std::endl;

  s1.changeGrade(up);

  std::cout << s1.grade << std::endl;
}