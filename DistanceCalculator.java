package pythagorean;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/*
 * For simplicity I'm implementing the points with integer values.
 * In order to do it with real numbers, the logic would be the same 
 * but the java.awt.Point object couldn't be directly used.
 */
public class DistanceCalculator extends Thread {
	
	/*
	 * Each Thread has a ArrayList of the pairs of coordinates that it should calculate.
	 */
	private ArrayList<int[]> threadCells;
	
	/*
	 * points are pairs of integers
	 * distances is the matrix to be returned
	 * n is the side of the matrix
	 * [curRow, curCol] is the next position in the matrix distances to be added to the ArrayList
	 * threadCells of one of the threads.
	 */
	private static Point[] points;
	private static double[][] distances;
	private static int n;
	private static int curRow = 0;
	private static int curCol = 0;

	
	
	public DistanceCalculator() {
		threadCells = new ArrayList<int[]>();
	}	

	//The diagonal remains zero.
	//Once we are at the last column, we move to the first column of the next row.
	//If we are at the last row, returns false.
	private static boolean move() {
		curCol++;
		if (curCol == points.length) {
			curRow += 1;
			curCol = curRow + 1;
		}
		if (curRow == points.length-1) {
			return false;
		}
		return true;
	}


	private double getDistance(Point p1, Point p2) {
		double distance = p1.distance(p2);
		return Math.round(distance * 100.0) / 100.0;
	}

	//Every thread calculates the distances for the relevant pairs of points. 
	//That is, the pairs in threadCells.
	//Being that the matrix is symmetric, it's enough to calculate for the upper side.
	public void run() {
		for (int[] pair:threadCells) {
			int row = pair[0];
			int col = pair[1];
			double distance = getDistance(points[row], points[col]);
			distances[row][col] = distance;
			distances[col][row] = distance; 
		}
	}
	

	/*
	 * Receives an array of points and a number of threads. 
	 * Calculates the distances matrix using a similar amount of load in every thread.
	 */
	public static double[][] getDistancesMatrix(Point[] points, int threadsNum) {
		
		DistanceCalculator.points = points;
		DistanceCalculator.n = points.length;	
		
		distances = new double[n][n];
		
		DistanceCalculator[] calculators = new DistanceCalculator[threadsNum];
		
		for (int i=0;i<threadsNum;i++) {
			calculators[i] = new DistanceCalculator();
		}
		
		int curCalculator = 0;
		
		//We iterate over the threads and push to threadCells the next cell.
		while(move()) {
			calculators[curCalculator].threadCells.add(new int[]{curRow, curCol});
		}
		
		//Once every thread has the information regarding which cells are the relevant to it,
		//the threads can run.
		for (int i=0;i<threadsNum;i++) {
			calculators[i].start();
		}
		
		try {
			for (DistanceCalculator dc:calculators) {
				dc.join();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//Once all threads finished running we can return the matrix.
		
		System.out.println("All the threads ended running");
		System.out.println();
		return distances;
		
	}
	
	private static void printPoints(Point[] points) {
		System.out.println("The points are:");
		for (int i=0;i<points.length;i++) {
			System.out.println(i + " " + points[i].toString().replace("java.awt.Point", ""));
		}
		System.out.println();
		System.out.println();
	}
	
	/*
	 * Throws an exception if one of the distances calculated doesn't agree with the Math.sqrt one.
	 */
	public static void test() throws Exception {
		for (int i=0;i<n;i++) {
			for (int j=0;j<n;j++) {
				double dist1 = distances[i][j];
				double deltaX = points[i].getX() - points[j].getX();
				double deltaY = points[i].getY() - points[j].getY();
				double dist2 = Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));
				if (Math.abs(dist1 - dist2) > 0.01) {
					throw new Exception("error in distance calculation in cell: [" + i + "," + j +"]");
				}
			}
		}
	}
	
	
	public static void main(String[] args) throws Exception {
			
		Point[] points = new Point[10];
		
		Random rand = new Random(); 
		for (int i=0;i<points.length;i++) {
			int x = rand.nextInt(1000);
			int y = rand.nextInt(1000);
			points[i] = new Point(x, y);
		}
		
		printPoints(points);

		double[][] distances = getDistancesMatrix(points, 3);
		
		test();
		
		String distances_string = Arrays.deepToString(distances);
		
		distances_string = distances_string.replace("],", System.lineSeparator());
		
		System.out.println("The distances matrix is:");
		System.out.println();
		System.out.println(distances_string);
		
	}


}
