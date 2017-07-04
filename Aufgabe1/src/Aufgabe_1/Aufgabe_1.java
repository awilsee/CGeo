package Aufgabe_1;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

class Aufgabe1 {

	private static final String FILE1 = "data\\Aufgabe_1\\s_1000_1.dat";
	private static final String FILE2 = "data\\Aufgabe_1\\s_10000_1.dat";
	private static final String FILE3 = "data\\Aufgabe_1\\s_100000_1.dat";

	private static ArrayList<double[]> parseFile(String fileName) throws IOException {
		FileReader fr = new FileReader(fileName);
		BufferedReader br = new BufferedReader(fr);

		String zeile = "";
		zeile = br.readLine(); // read first line

		ArrayList<double[]> data = new ArrayList<double[]>(); // data from all lines in file

		while (zeile != null) { // read as long as there are further lines
			int j = 0;
			String[] row = zeile.split(" "); // split lines at spaces
			double[] dataLine = new double[4]; // initialize array with coordinates from line
			for (String str : row) {
				dataLine[j++] = Double.parseDouble(str); // save coordinates in array
			}
			data.add(dataLine); // ad coordinates of line to data of file
			zeile = br.readLine(); // next line
		}

		br.close(); // close file

		return data;
	}

	// calculate counterclockwise test for given points
	private static double calcCcw(double p1_x, double p1_y, double p2_x, double p2_y, double p3_x, double p3_y) {
		return (p1_x * p2_y - p1_y * p2_x) + (p2_x * p3_y - p2_y * p3_x)
				+ (p1_y * p3_x - p1_x * p3_y);
	}

	// calculate number of crossed lines in data
	private static int numCrossedLines(ArrayList<double[]> data) {
		int numCrosses = 0; // initialize number of crosses
		for (int i = data.size() - 1; i > 0; i--) { // test every line in the data
			for (int j = i - 1; j >= 0; j--) {      // with every other line in the data
				// initialize points
				Point2D.Double i1 = new Point2D.Double(data.get(i)[0], data.get(i)[1]);
				Point2D.Double i2 = new Point2D.Double(data.get(i)[2], data.get(i)[3]);
				Point2D.Double j1 = new Point2D.Double(data.get(j)[0], data.get(j)[1]);
				Point2D.Double j2 = new Point2D.Double(data.get(j)[2], data.get(j)[3]);
				// counterclockwise tests for the two lines
				double ccw1 = calcCcw(i1.x, i1.y, i2.x, i2.y, j1.x, j1.y);
				double ccw2 = calcCcw(i1.x, i1.y, i2.x, i2.y, j2.x, j2.y);
				double ccw3 = calcCcw(j1.x, j1.y, j2.x, j2.y, i1.x, i1.y);
				double ccw4 = calcCcw(j1.x, j1.y, j2.x, j2.y, i2.x, i2.y);
				// test if two lines cross (from script)
				// if necessary, increment number of crosses
				if (ccw1 == 0 && ccw2 == 0) {
					double lambda1 = (j1.x - i1.x) / (i2.x - i1.x);
					double lambda2 = (j2.x - i1.x) / (i2.x - i1.x);
					if (i1.x == j1.x || i1.x == j2.x || i2.x == j1.x || i2.x == j2.x) {
						numCrosses++;
					} else if ((0 <= lambda1 && lambda1 <= 1) || (0 <= lambda2 && lambda2 <= 1)) {
						numCrosses++;
					}
				} else if (ccw1 * ccw2 <= 0 && ccw3 * ccw4 <= 0) {
					numCrosses++;
				}
			}
		}
		return numCrosses;
	}

	public static void main(String[] args) throws IOException {
		// execute functions for different data files and print their results
		long timeStart = System.currentTimeMillis();
		ArrayList<double[]> data = parseFile(FILE1);
		int numCrossedLines = numCrossedLines(data);
		long timeEnd = System.currentTimeMillis();
		System.out.println("Anzahl an Überschneidungen: " + numCrossedLines);
		System.out.println("Verlaufszeit s_1000_1.dat: " + (timeEnd - timeStart) + " Millisek.");

		timeStart = System.currentTimeMillis();
		data = parseFile(FILE2);
		numCrossedLines = numCrossedLines(data);
		timeEnd = System.currentTimeMillis();
		System.out.println("\nAnzahl an Überschneidungen: " + numCrossedLines);
		System.out.println("Verlaufszeit s_10000_1.dat: " + (timeEnd - timeStart) + " Millisek.");

		timeStart = System.currentTimeMillis();
		data = parseFile(FILE3);
		numCrossedLines = numCrossedLines(data);
		timeEnd = System.currentTimeMillis();
		System.out.println("\nAnzahl an Überschneidungen: " + numCrossedLines);
		System.out.println("Verlaufszeit s_100000_1.dat: " + (timeEnd - timeStart) + " Millisek.");
	}
}