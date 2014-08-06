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
  Iterator* createIterator();
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
private:
  int cursor_;
  BookVector* bookvec_;
};

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

Iterator* BookVector::createIterator() {
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

  return 0;
}
