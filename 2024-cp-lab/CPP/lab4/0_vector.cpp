#include <iostream>
#include <vector>

static void printVector(std::vector<int> &vec) {
  std::cout << "size : " << vec.size() << " capacity : " << vec.capacity()
            << std::endl;
  for (std::vector<int>::iterator itr = vec.begin(); itr != vec.end(); ++itr) {
    std::cout << *itr << std::endl;
  }
}

int main() {
  std::vector<int> vec_1, vec_2(3, 3), vec_3;
  vec_1.push_back(3);
  vec_1.push_back(1);
  vec_1.push_back(4);
  vec_1.push_back(1);
  vec_1.push_back(5);
  vec_1.push_back(9);
  vec_1.push_back(2);
  std::cout << "after push_back" << std::endl;
  printVector(vec_1);
  vec_1.pop_back();
  vec_1.pop_back();
  std::cout << "after pop_back" << std::endl;
  printVector(vec_1);
  std::cout << "after initalize" << std::endl;
  printVector(vec_2);
  vec_3.reserve(7);
  std::cout << "after reserve" << std::endl;
  printVector(vec_3);
  return 0;
}