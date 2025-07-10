#include <iostream>

int main() {
  int i1 = 1;
  int i2 = 2;

  int *ptr1 = &i1;
  int *ptr2 = ptr1;

  *ptr1 = 3;
  i2 = *ptr2;

  std::cout << "i1 = " << i1 << std::endl;
  std::cout << "i2 = " << i2 << std::endl;
  std::cout << "*ptr1 = " << *ptr1 << std::endl;
  std::cout << "*ptr2 = " << *ptr2 << std::endl;
}