# Multi-Dimensional Data
1. Each item has d aspects (coordinates)
2. Aspect values $x_i$ are numbers
3. Each item corresponds to a point in d-dimensional space
4. We concentrate on d=2, example are points in the Euclidean plane.


# One-Dimensional Range Search
1. First Solution:
    * Running time: $O(\log n+k)$, k number of reported items
    * Problem: does not generalize to higher dimensions
2. Second Solution:
balanced BST such as an AVL tree

## Implementation of Range Search:
    BST-RangeSearch(T,$k_1$,$k_2$)
    T: a balanced search tree, $k_1$, $k_2$ are search keys
    if T = nil then return  
    if key(T) < $k_1$ then
      BST-RangeSearch(T.right, $k_1$, $k_2$)
    if key(T) > $k_2$ then

