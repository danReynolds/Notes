#include <fcntl.h>
#include <sys/mman.h>
#include <stdio.h>
#include <errno.h>

int main() {
  printf("CHAR size: %d\n", (int)sizeof(char));

  int rc;
  int size = 17;
  int fd = open("mmap.txt", O_RDONLY);
  unsigned int i;

  if (!fd) {
    printf("FAILED to open file for READING");
  }

  int fd2 = open("mmapWRITTEN.txt", O_RDWR|O_TRUNC|O_CREAT);

  if (!fd2) {
    printf("FAILED to open file for WRITING");
  }

  char* src = mmap(0, size, PROT_READ, MAP_SHARED, fd, 0);

  if (src == MAP_FAILED) {
    printf("FAILED with: %d", errno);
  }

  char* dest = mmap(NULL, size, PROT_READ|PROT_WRITE, MAP_SHARED, fd2, 0);

  if (dest == MAP_FAILED) {
    printf("FAILED with: %d", errno);
  }

  // Initially the file is empty, seek to the size and put a null terminator there so that it now has proper file size to receive characters.
  rc = lseek(fd2, size, SEEK_SET);
  write(fd2, "\n\0", 2);

  for (i = 0; i < size; i++) {
    dest[i] = src[i];
  }

  printf("DONE\n");

  close(fd);
  close(fd2);
  munmap(src, size);
  munmap(dest, size);
}