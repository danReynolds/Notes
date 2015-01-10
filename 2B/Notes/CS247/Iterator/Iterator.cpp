#include <vector>
#include <iostream>

class Iterator;
class BookVectorIterator;

class AbstractBook {
public:
  virtual Iterator* createIterator() = 0;
  virtual void addPage(int) = 0;
  virtual int getPage(int) = 0;
};

class BookVector {
public:
  BookVectorIterator* createIterator();
  void addPage(int);
  int getPage(int);
  int size();
private:
  std::vector<int> pages_;
};

class Iterator {
public:
  virtual void first() = 0;
  virtual int next() = 0;
  virtual bool hasNext() = 0;
};

class BookVectorIterator : public Iterator {
public:
  BookVectorIterator(BookVector*);
  void first();
  int next();
  bool hasNext();
  BookVectorIterator& operator++(); //give our iterator increment abilities
  BookVectorIterator operator++(int); //give our iterator increment abilities
  int operator*();
private:
  int cursor_;
  BookVector* bookvec_;
};

int BookVectorIterator::operator*() {
  return bookvec_->getPage(cursor_);
}

BookVectorIterator BookVectorIterator::operator++(int x) { //one with param is post-increment
  BookVectorIterator temp = BookVectorIterator(bookvec_);
  cursor_++;
  return temp; //return a new temp because we need to make the original's cursor increment
}

BookVectorIterator& BookVectorIterator::operator++() {
  cursor_++;
  return *this;
}

BookVectorIterator::BookVectorIterator(BookVector* vec) {
  bookvec_ = vec;
  first();
}

void BookVectorIterator::first() {
  cursor_ = 0;
}

bool BookVectorIterator::hasNext() {
  return cursor_ < bookvec_->size();
}

int BookVectorIterator::next() {
  return bookvec_->getPage(cursor_++);
}

int BookVector::size() {
  return pages_.size();
}

BookVectorIterator* BookVector::createIterator() {
  return new BookVectorIterator(this);
}

void BookVector::addPage(int x) {
  pages_.push_back(x);
}

int BookVector::getPage(int x) {
  return pages_[x];
}


int main() {
  
  BookVector* a = new BookVector();
  a->addPage(1);
  a->addPage(2);
  a->addPage(4);

  Iterator* iterator = a->createIterator();
  while (iterator->hasNext()) {
    std::cout << iterator->next() << std::endl;
  }

  iterator->first();

  while (iterator->hasNext()) {
    std::cout << iterator->next() << std::endl;
  }

  BookVectorIterator b = *(a->createIterator());
  std::cout << *(b++);
  std::cout << *b;
  std::cout << *(++b);

  return 0;

}
