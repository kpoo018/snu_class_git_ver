#pragma once

#include <cstddef>

namespace CP {
template <typename T> class Vector {
public:
  class Iterator {
  public:
    Iterator(Vector *vec){
      this->vec_=vec;
      this->idx_=0;
    }
    Iterator(Vector *vec, size_t idx){
      if(idx <= vec->size_){
        this->vec_=vec;
        this->idx_=idx;
      } 
    }
    Iterator(const Iterator &iter){
      this->vec_=iter.vec_;
      this->idx_=iter.idx_;
    }
    Iterator &operator=(const Iterator &iter){
      this->vec_=iter.vec_;
      this->idx_=iter.idx_;
      return *this;
    }
    T &operator*(){
      return *((this->vec_->data_ + (this->idx_)));
    }
    bool operator==(const Iterator &iter) const{
      if(*((this->vec_->data_ + (this->idx_)))==*((iter.vec_->data_ + (iter.idx_)))) return true;
      else return false;
    }
    bool operator!=(const Iterator &iter) const{
      if(*((this->vec_->data_ + (this->idx_)))!=*((iter.vec_->data_ + (iter.idx_)))) return true;
      else return false;
    }
    Iterator &operator++(){
      if (this->idx_ < vec_-> size_){
        this->idx_ += 1;
      }
      return *this;
    }//prefix
    Iterator operator++(int){
      Iterator result(*this);
      ++(*this);
      return result;
    }//postfix
    Iterator &operator--(){
      if(this->idx_ != 0){
        this->idx_ -= 1;
      }
      return *this;
    }
    Iterator operator--(int){
      Iterator result(*this);
      --(*this);
      return result;
    }

  private:
    Vector *vec_;
    size_t idx_;
  };

  class ReverseIterator {
  public:
    ReverseIterator(Vector *vec){
      this->vec_=vec;
      this->idx_=0;
    }
    ReverseIterator(Vector *vec, size_t idx){
      this->vec_=vec;
      this->idx_=idx;
    }
    ReverseIterator(const ReverseIterator &iter){
      this->vec_=iter.vec_;
      this->idx_=iter.idx_;
    }
    ReverseIterator &operator=(const ReverseIterator &iter){
      this->vec_=iter.vec_;
      this->idx_=iter.idx_;
      return *this;
    }
    T &operator*(){
      return *((this->vec_->data_) + (this->vec_->size_ -1) - (this->idx_));
    }
    bool operator==(const ReverseIterator &iter) const{
      if(*((this->vec_->data_) + (this->vec_->size_ -1) - (this->idx_))==*((iter.vec_->data_) + (iter.vec_->size_ -1) - (iter.idx_))) return true;
      else return false;
    }
    bool operator!=(const ReverseIterator &iter) const{
      if(*((this->vec_->data_) + (this->vec_->size_ -1) - (this->idx_))!=*((iter.vec_->data_) + (iter.vec_->size_ -1) - (iter.idx_))) return true;
      else return false;
    }
    ReverseIterator &operator++(){
      if (this->idx_ < vec_-> size_){
        this->idx_ += 1;
      }
      return *this;
    }
    ReverseIterator operator++(int){
      ReverseIterator result(*this);
      ++(*this);
      return result;
    }
    ReverseIterator &operator--(){
      if (this->idx_ < vec_-> size_){
        this->idx_ -= 1;
      }
      return *this;
    }
    ReverseIterator operator--(int){
      ReverseIterator result(*this);
      --(*this);
      return result;
    }

  private:
    Vector *vec_;
    size_t idx_;
  };

  Vector(){
    this->data_ = new T[0];
    this->size_ = 0;
    this->capacity_ = 0;
  }
  ~Vector(){
    delete[] this->data_;
  }
  Vector(const Vector &vec){
    for(int i=0; i<vec.size_; i++){
      this->data_[i] = vec.data_[i];
    }
    this->size_ = vec.size_;
    this->capacity_ = vec.capacity_;
  }

  Vector &operator=(const Vector &vec){
    for(int i=0; i<vec.size_; i++){
      this->data_[i] = vec.data_[i];
    }
    this->size_ = vec.size_;
    this->capacity_ = vec.capacity_;
    return *this;
  }

  T &operator[](size_t idx){
    return *(this->data_+idx);
  }

  Iterator begin(){
    Iterator result = Iterator(this);
    return result;
  }
  Iterator end(){
    Iterator result = Iterator(this, this->size_ );
    return result;
  }
  ReverseIterator rbegin(){
    ReverseIterator result = ReverseIterator(this);
    return result;
  }
  ReverseIterator rend(){
    ReverseIterator result = ReverseIterator(this, this->size_);
    return result;
  }

  size_t size() const{
    return this->size_;
  }
  size_t capacity() const{
    return this->capacity_;
  }

  void reserve(size_t capacity){
    this->capacity_ = capacity;
    T *data = new T[capacity];
    for(int i = 0; i<size_; i++){
      data[i]=this->data_[i];
    }
    delete[] this->data_;
    this->data_ = data;
    return;
  }

  void push_back(T &data){
    if(this->capacity_ == 0){
      this->reserve(1);
    }
    if(this->size_==this->capacity_){
      this->reserve(2*(this->capacity_));
    }
    this->data_[size_] = data;
    this->size_ += 1;
    return;
  }

  void pop_back(){
    if(size_ == 0) return;
    this->size_ -= 1;
    return;
  }

private:
  T *data_;
  size_t size_;
  size_t capacity_;
};
} // namespace CP