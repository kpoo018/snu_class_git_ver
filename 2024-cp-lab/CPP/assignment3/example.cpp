#include "Vector.hpp"
#include <iostream>

using namespace CP;

template class CP::Vector<int>;

int main() {
  Vector<int> v;

  std::cout << "Pushing back 5 elements." << std::endl;
  for (int i = 1; i <= 5; ++i) {
    v.push_back(i);
  }

  std::cout << "Contents of vector: ";
  for (Vector<int>::Iterator it = v.begin(); it != v.end(); ++it) {
    std::cout << *it << " ";
  }
  std::cout << std::endl;

  std::cout << "Vector size: " << v.size() << std::endl;
  std::cout << "Vector capacity: " << v.capacity() << std::endl;

  std::cout << "Accessing third element using operator[]: " << v[2]
            << std::endl;

  std::cout << "Popping back one element." << std::endl;
  v.pop_back();

  std::cout << "Contents of vector after pop_back: ";
  for (Vector<int>::Iterator it = v.begin(); it != v.end(); ++it) {
    std::cout << *it << " ";
  }
  std::cout << std::endl;

  std::cout << "Using reverse iterators: ";
  for (Vector<int>::ReverseIterator it = v.rbegin(); it != v.rend(); ++it) {
    std::cout << *it << " ";
  }
  std::cout << std::endl;

  return 0;
}
