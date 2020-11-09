# pythagorean-matrix-threads
### Multi-threaded app that calculates euclidean distances for a list of points.

Given a list of points p1, ..., pn and a number of threads T, it calculates a squared matrix M of size n where M[i, j] is the distance between points i and j,
using for that calculation T threads.

Compile through javac DistanceCalculator.java, run through java [optional_params]
  
optional_params should be an odd number of integer values separated with spaces. That is:
  p1_x p1_y ... pn_x pn_x t
  Where point i = (pi_x, pi_y).
  
  For example, use "1 2 3 4 5" to indicate p1 = (1,2), p2 = (3,4) and 5 threads.
  
If no parameters are passed, a random matrix is calculated.