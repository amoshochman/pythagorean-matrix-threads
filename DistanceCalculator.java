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
	 * Each Thread has a ArrayList of the pairs of coordinates that it should
	 * calculate.
	 */
	private ArrayList<int[]> threadCells;

	/*
	 * "points" are pairs of integers. "matrix" are the distances. "curRow, curCol"
	 * is the position to assign to the next thread.
	 */
	private static Point[] points;
	private static double[][] matrix;
	private static int n;
	private static int curRow;
	private static int curCol;
	private static int threadsNum;
	private static String inputErrorString = "Provided input should be an odd number of integers bigger or equal than 3.";

	public DistanceCalculator() {
		threadCells = new ArrayList<int[]>();
	}

	/*
	 * Every thread calculates the distances for the relevant pairs of points. That
	 * is, the pairs in threadCells. Being that the matrix is symmetric, it's enough
	 * to do the calculation for the upper side.
	 */
	public void run() {
		for (int[] pair : threadCells) {
			int row = pair[0];
			int col = pair[1];
			double distance = getDistance(points[row], points[col]);
			matrix[row][col] = distance;
			matrix[col][row] = distance;
		}
	}

	private static void setThreadsNum(int threadsNum) {
		DistanceCalculator.threadsNum = threadsNum;
	}

	/*
	 * Sets DistanceCalculator.points member provided an ArrayList of int arrays.
	 */
	private static void setPoints(ArrayList<int[]> pointsArray) {
		n = pointsArray.size();
		curRow = 0;
		curCol = 0;
		points = new Point[n];
		for (int i = 0; i < n; i++) {
			int[] pair = pointsArray.get(i);
			points[i] = new Point(pair[0], pair[1]);
		}
	}

	// The diagonal remains zero.
	// Once we are at the last column, we move to the first column of the next row.
	// Returns false iff we are at the last cell.
	private static boolean move() {
		curCol++;
		if (curCol == points.length) {
			curRow += 1;
			curCol = curRow + 1;
		}
		if (curRow == points.length - 1) {
			return false;
		}
		return true;
	}

	private static double getDistance(Point p1, Point p2) {
		double distance = p1.distance(p2);
		return Math.round(distance * 100.0) / 100.0;
	}

	/*
	 * Calculates the distances matrix using similar amount of load among the
	 * threads.
	 */
	public static void setMatrix() {

		matrix = new double[n][n];

		if (n == 1) {
			return;
		}

		DistanceCalculator[] calculators = new DistanceCalculator[threadsNum];

		for (int i = 0; i < threadsNum; i++) {
			calculators[i] = new DistanceCalculator();
		}

		int curCalculator = 0;

		// We iterate over the threads and push to threadCells the next cell.
		while (move()) {
			calculators[curCalculator].threadCells.add(new int[] { curRow, curCol });
		}

		// Once every thread has the information regarding which cells are the relevant
		// to it, the threads can run.
		for (int i = 0; i < threadsNum; i++) {
			calculators[i].start();
		}

		try {
			for (DistanceCalculator dc : calculators) {
				dc.join();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.printf("All the threads ended running... ");

	}

	/*
	 * Throws an exception if one of the distances calculated doesn't agree with the
	 * Math.sqrt one.
	 */
	public static void test() throws Exception {
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				double dist1 = matrix[i][j];
				double deltaX = points[i].getX() - points[j].getX();
				double deltaY = points[i].getY() - points[j].getY();
				double dist2 = Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));
				if (Math.abs(dist1 - dist2) > 0.01) {
					throw new Exception("error in distance calculation in cell: [" + i + "," + j + "]");
				}
			}
		}
	}

	private static ArrayList<int[]> getRandomPoints(int pointsNumber) {
		Random rand = new Random();
		ArrayList<int[]> pointsArray = new ArrayList<int[]>();
		for (int i = 0; i < pointsNumber; i++) {
			int x = rand.nextInt(1000);
			int y = rand.nextInt(1000);
			int[] pointArr = { x, y };
			pointsArray.add(pointArr);
		}
		return pointsArray;
	}

	private static void printMatrix() {
		String distances_string = Arrays.deepToString(matrix);
		distances_string = distances_string.replace("],", System.lineSeparator());
		System.out.println("Distances matrix:");
		System.out.println(distances_string);
	}

	private static void printPoints() {
		System.out.println("Points:");
		for (int i = 0; i < points.length; i++) {
			System.out.println(i + " " + points[i].toString().replace("java.awt.Point", ""));
		}
		System.out.println();
	}

	private static void processAndPrint(ArrayList<int[]> pointsArray, int threadsNum) throws Exception {
		setThreadsNum(threadsNum);
		setPoints(pointsArray);
		printPoints();
		setMatrix();
		printMatrix();
		test();
	}

	public static void main(String[] args) throws Exception {

		if (args.length == 0) {
			// a sanity check using random numbers
			System.out.println("<<-----Random Values----->>");
			processAndPrint(getRandomPoints(3), 2);
			return;
		}

		System.out.println("<<-----Provided Values----->>");

		ArrayList<int[]> pointsArray = new ArrayList();
		if (args.length % 2 == 0 || args.length == 1) {
			throw new Exception(inputErrorString);
		}
		int[] int_args = new int[args.length];
		for (int i = 0; i < args.length; i++) {
			try {
				int_args[i] = Integer.parseInt(args[i]);
			} catch (Exception e) {
				throw new Exception(inputErrorString);
			}
		}
		for (int i = 0; i < args.length - 1; i += 2) {
			int x = int_args[i];
			int y = int_args[i + 1];
			int[] pointArr = { x, y };
			pointsArray.add(pointArr);
		}
		processAndPrint(pointsArray, int_args[args.length - 1]);

	}

}
