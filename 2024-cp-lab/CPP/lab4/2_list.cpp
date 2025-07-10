#include <iostream>
#include <list>

template <class Iter> static void printIter(Iter begin, Iter end) {
  for (Iter itr = begin; itr != end; ++itr) {
    std::cout << *itr << std::endl;
  }
}

int main() {
  std::list<int> li;
  li.push_back(3);
  li.push_back(1);
  li.push_back(4);
  li.push_back(1);
  li.push_back(5);
  li.push_back(9);
  li.push_back(2);
  std::list<int>::iterator iter = li.begin();
  for (int i = 0; i < 3; ++i) {
    ++iter;
  }
  li.insert(iter, 0);
  printIter(li.begin(), li.end());
  return 0;
}