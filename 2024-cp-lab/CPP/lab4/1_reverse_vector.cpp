#include <iostream>
#include <vector>

template <class Iter> static void printIter(Iter begin, Iter end) {
  for (Iter itr = begin ; itr != end; ++itr) {
    std::cout << *itr << std::endl;
  }
}

int main() {
  std::vector<int> vec = {3, 1, 4, 1, 5, 9, 2, 6, 5, 3,
                          5, 8, 9, 7, 9, 3, 2, 3, 8};
  printIter(vec.rbegin(), vec.rend());
  return 0;
}