# Graph Theory Facts and Propositions

## General:
1. Handshake Theorem: $\sum_{v \in V(G)} deg(v) = 2|E|$
2. Proposition: Every graph with $\geq 2$ vertices has **two vertices** of the **same degree.**
3. Proposition: the n-cube has $2^n$ vertices and $n2^{n-1}$ edges
4. Theorem: if there is a walk from vertex x to vertex y in G then there is a path from x to y in G.
5. Corollary: if there is a path from x to y in G and a path from y to z in G then there is a path from x to z in G.
6. Theorem: let G be a graph and let v be a vertex in G. If for each w in G there is a path from v to w,  then G is connected. *For any vertex, you can get to any other vertex.*
7. Theorem: a graph G is **not connected** iff there exists a proper subset of x of V(G) such that the **cut** induced by x is empty.
8. Proposition: if every very has degree $\geq 2$ then G **has a cycle**.
9. Theorem (Dirac): if G is a graph on **n > 3 vertices** where every vertex has degree $\geq \frac{n}{2}$, then G has a cycle containing every vertex. G is a **Hamiltonian Graph**.
10. Theorem (Chvtal '72): if G is a graph on n vertices with degree $d_1 \leq d_2 \leq d_3 ... \leq d_n$ then if $d_i \geq i$ or $d_{n-i} \geq n - i$ for all $i \leq \frac{n}{2}$, then G is Hamiltonian.
11. Theorem (Tutte): Every **4-connected** planar graph is **Hamiltonian**.
12. Theorem: Every connected graph in which every vertex has even degree is *Eulerian*. An Eulerian graph has an Euler tour, which is a closed walk that contains every edge once.
13. Lemma: if e = {x,y} is a bridge of a connected graph G, then G-e has precisely two components. Furthermore, x and y are in different components.
14. Theorem: An edge is a bridge of a graph G iff it is **not contained in a cycle of G**.
15. Corollary: If there are two distinct paths from u to v in G then G contains a cycle.
16. Lemma: There is a unique path between every pair of vertices u and v in a tree.
17. Lemma: **Every edge** of a tree T is a **bridge**.
18. Theorem: A tree with **at least 2 vertices** has at least two vertices of **degree 1**.
19. Theorem: if T is a tree, then |E(T)| = |V(T) - 1|.
20. Proposition: If x,y are vertices of a tree T, then there is a unique path of T from x to y.
21. Proposition: A graph G has a **spanning tree** iff it is **connected**.
22. Corollary: Every connected graph on n vertices has $\geq n-1$ edges.
23. Corollary: Every **connected** graph on n vertices, n-1 edges **is a tree**.
24. Proposition: Every tree is **bipartite**.
25. Proposition: If G is a bipartite graph and u,v $\in V(G)$ then if u and v are in the **same part** of a bipartition, then every walk from u to v has **even length**. If u,v are in **different parts**, then every walk from u to v has **odd length**.
24. Proposition: If G is a graph with no **odd cycles**, then **G is bipartite**.
25. Theorem: Prim's algorithm outputs a min-weight spanning tree.
26. Proposition: A graph is planar iff it has a spherical embedding.
27. Theorem: If a connected planar embedding has faces $f_1, f_2, ...$ then $\sum_{i=1} deg(f_i) = 2|E(G)|$
28. Corollary: If the connected graph G has a planar embedding with f faces, then average degree of a face is $\frac{2|E(G)|}{f}$.
29. Theorem (Euler): let G be a connected graph with |V| vertices and |E| edges. If G has a planar embedding with |F| faces, then |V| - |E| + |F| = 2.
30. Theorem: There are exactly **five** *non-isomorphic, planar dual, platonic solids*:
    1. Tetrahedron:
        1. 4 faces
        2. 4 vertices
        3. 6 edges
    2. Cube:
        1. 6 faces
        2. 8 vertices
        3. 12 edges
    3. Octahedron:
        1. 8 faces
        2. 6 vertices
        3. 12 edges
    4. Icosahedron:
        1. 20 faces
        2. 12 vertices
        3. 30 edges
    5. Do-decahedron:
        1. 12 faces
        2. 20 vertices
        3. 30 edges
31. Lemma: Let G be a planar embedding with |V| vertices, |E| edges and |F| faces. Then {d,k} is one of the five pairs of faces and vertices: {3,3}, {3,4}, {4,3}, {5,3}, {3,5}
32. Lemma: If G is **connected** and **not a tree** then in a planar embedding of G, the boundary of each face contains a **cycle**.
33. Lemma: Let G be a planar embedding with |V| vertices and |E| edges. If each face has degree at least d, then (d-2)|E| $\leq$ d(|V|-2)$.
34. Corollary: In any planar embedding of a graph with **at least 2 edges**, **each face** has degree $\geq 3$.
35. Lemma: In any planar embedding of a graph with $\geq$ **1 cycle**, the **boundary** of every face **contains a cycle**.
36. Lemma (Test 1): If G = (V,E) is a planar graph and |E| $\geq 2$, then |E| $\leq$ 3|V|-6.
37. Corollary: $K_5$ is non-planar |V| = 5, |E| = 10.
38. Corollary: A **planar graph** has a *vertex of degree at most 5*.
39. Lemma (Test 2): If G = (V,E) is a planar graph and every cycle has length $\geq$ g, where g is the **girth**, the length of the smallest cycle, and |E| $\geq \frac{1}{2}g$, then $|E| \leq \frac{g}{g-2}(|V| - 2)$
40. Corollary: $K_{3,3}$ is non-planar because it has no triangles, so g = 4 and it fails Test 2.
41. Kuratowski's Theorem: A graph is planar **iff** it has **no subdivision** of $K_{3,3}$ or $K_5$ as a **subgraph**.
42. Theorem: A graph is **2-colourable** iff it is **bipartite**.
43. Theorem: $K_n$ is n-colourable and not k-colourable for k < n.
44. Five-Colour-Theorem: **Every** planar graph is **5-colourable**.
45. Theorem: **Every** planar graph is **4-colourable**.
46. Lemma: M is **not** a **maximum matching** iff there exists an **M-augmenting path**.
47. Lemma: If M is a **matching** of G and C is a **cover** of G then $|M| \leq |C|$.
48. Lemma: If M is matching and C is a cover and |M| = |C| then M is a maximum matching and C is a minimum cover.
49. Theorem (Konig's Theorem): If G is **bipartite**, then the size of the **maximum matching** is **equal** to the size of the **minimum cover**.
50. Lemma: Let G be a **bipartite** graph with bipartition A,B where |A| = |B| = n. If G has |E| edges then **G has a matching** of **at least** size $\frac{|E|}{n}$.
51. Theorem (Hall's): An (A,B)-bigraph G has a matching that **saturates** A iff for **every subset** S of A, |S| $\leq$ |N(S)|.
52. Corollary: An (A,B) bigraph G has a **perfect matching** iff |A|=|B| and for S is a subset of A, |S| $\leq$ |N(S)|.
56. Proposition: If k $\geq 1$ and G is a **k-regular, bipartite** graph, then G has a **perfect matching**.
57. Corollary: If G is a **k-regular, bipartite** graph, then E(G) has a partition into **k perfect matches** of G.
58. Corollary: Following from right above, **every k-regular, bipartite** graph is **k-edge colourable**.

