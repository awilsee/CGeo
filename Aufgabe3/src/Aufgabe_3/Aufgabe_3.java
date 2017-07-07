package Aufgabe_3;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.TreeSet;

import javafx.collections.transformation.SortedList;


class Aufgabe3 {

	private static final String FILE1 = "data\\s_1000_10.dat";
	private static final String FILE2 = "data\\s_1000_1.dat";
	private static final String FILE3 = "data\\s_10000_1.dat";
	private static final String FILE4 = "data\\s_100000_1.dat";
	public static ArrayList<double[]> data;
	public static TreeSet<Integer> intersectionsChecked;
	public static int numCrossedLines;
	
	private enum EventType {
		Startpoint, Endpoint, Intersection
	}
	
	private static class EventPoint {
		public double x = 0, y = 0;
		public EventType type = EventType.Startpoint;
		public int seg1_Data_Index = -1, seg2_Data_Index = -1;
	}
	
	public static class IntersectionPoints {
		public double x = 0, y = 0;
		public SortedList<Integer> points;
	}

	private static ArrayList<double[]> parseFile(String fileName) throws IOException {
		FileReader fr = new FileReader(fileName);
		BufferedReader br = new BufferedReader(fr);

		String line = "";
		line = br.readLine();  // read first line

		data = new ArrayList<double[]>(); // data from all segments in file

		while (line != null) {	// read as long as there are further segments
			int j = 0;
			String[] row = line.split(" "); // split lines at spaces
			double[] dataLine = new double[4]; // initialize array with coordinates from line
			for (String str : row) {
				dataLine[j++] = Double.parseDouble(str); // save coordinates in array
			}
			// change order of coordinates, so the smaller one always is the first
			if (dataLine[0] > dataLine[2] ||
					(dataLine[0] == dataLine[2] && dataLine[1] > dataLine[3])) {
				double temp = dataLine[0];
				dataLine[0] = dataLine[2];
				dataLine[2] = temp;
				temp = dataLine[1];
				dataLine[1] = dataLine[3];
				dataLine[3] = temp;
			}
			
			data.add(dataLine); // ad coordinates of line to data of file
			line = br.readLine(); // next line
		}

		br.close(); // close file

		return data;
	}

	// initialize event queue with first line
	private static TreeMap<double[], EventPoint> generateEventQueue(ArrayList<double[]> data) {
		// initialize event queue with comparator:
		// x-, y-coordinate and type of the event point have the given order
		TreeMap<double[], EventPoint> eventQueue = new TreeMap<double[], EventPoint>(new Comparator<double[]>()
	    {
	        public int compare(double[] o1, double[] o2)
	        {
    			if (o1[0] == o2[0]) {
    				if (o1[1] == o2[1]) {
    					if (o1[2] == o2[2]) {
            				return -1;
            			} else {
            				return ((Double) o1[2]).compareTo((Double) o2[2]);
            			}
        			} else {
        				return ((Double) o1[1]).compareTo((Double) o2[1]);
        			}
    			} else {
    				return ((Double) o1[0]).compareTo((Double) o2[0]);
    			} 
	        } 
	    });
		
		// add all start & end points of the data to the event queue
		for (int i = 0; i < data.size(); i++) {
			EventPoint event_Startpoint = new EventPoint();
			event_Startpoint.x = data.get(i)[0];
			event_Startpoint.y = data.get(i)[1];
			event_Startpoint.type = EventType.Startpoint;
			event_Startpoint.seg1_Data_Index = i;
			addToEventQueue(event_Startpoint, eventQueue);

			EventPoint event_Endpoint = new EventPoint();
			event_Endpoint.x = data.get(i)[2];
			event_Endpoint.y = data.get(i)[3];
			event_Endpoint.type = EventType.Endpoint;
			event_Endpoint.seg1_Data_Index = i;
			addToEventQueue(event_Endpoint, eventQueue);
		}
		return eventQueue;
	}

	public static void addToEventQueue(EventPoint event, TreeMap<double[], EventPoint> eventQueue) {
		if (event.type == EventType.Intersection) { // if event type of point is an intersection
			// change segments, so the smaller index is always first
			if (event.seg1_Data_Index < event.seg2_Data_Index) {
				int temp = event.seg1_Data_Index;
				event.seg1_Data_Index = event.seg2_Data_Index;
				event.seg2_Data_Index = temp;
			}

			// return if intersection already found
			if (intersectionsChecked.contains(event.seg1_Data_Index * data.size() + event.seg2_Data_Index)) {
				return;
			}
			// add intersection to checked list
			intersectionsChecked.add(event.seg1_Data_Index * data.size() + event.seg2_Data_Index);
		}

		double eventType_Double = 0;
		// set order for the types of points
		switch (event.type) {
			case Startpoint:
				eventType_Double = 1;
				break;
			case Endpoint:
				eventType_Double = 3;
				break;
			case Intersection:
				eventType_Double = 2;
			default:
				break;
				
		}
		// add point to event queue
		eventQueue.put(new double[] {event.x, event.y, eventType_Double}, event);
	}

	public static int addToSweepLine(int seg_Data_Index, LinkedList<Integer> sweepLine) {
		double[] newSeg = data.get(seg_Data_Index);
		
		// just add to sweep line, if it's the first element
		if (sweepLine.size() == 0) {
			sweepLine.add(seg_Data_Index);
			return 0; // return position in sweep line
		}
		// iterate through all segments in sweep line
		for (int i = 0; i < sweepLine.size(); i++) {
			double[] compSeg = data.get(sweepLine.get(i));
			// calculate y-coordinate of first point of segment i in sweep line (on x-coordinate of new segment)
			double compSeg_y1 = (newSeg[0] - compSeg[0]) / (compSeg[2] - compSeg[0]) * (compSeg[3] - compSeg[1]) + compSeg[1];

			// if y-coordinate of first point of new segment is smaller than of segment i
			if (newSeg[1] < compSeg_y1) {
				// add to sweep line & return position
				sweepLine.add(i, seg_Data_Index);
				return i;
			// if y-coordinate of first point of new segment and segment i are equal
			} else if (newSeg[1] == compSeg_y1) {
				// find smaller x-coordinate of second points of segments
				double x2;
				if (newSeg[2] <= compSeg[2]) x2 = newSeg[2];
				else x2 = compSeg[2];

				// calculate y-coordinates of second points of segments
				double newSeg_y2 = (x2 - newSeg[0]) / (newSeg[2] - newSeg[0]) * (newSeg[3] - newSeg[1]) + newSeg[1];
				double compSeg_y2 = (x2 - compSeg[0]) / (compSeg[2] - compSeg[0]) * (compSeg[3] - compSeg[1]) + compSeg[1];

				// if y-coordinate of second point of new segment is higher than of segment i
				// they will have an intersection and therefore will swap position later
				if (newSeg_y2 >= compSeg_y2) {
					// add to sweep line & return position
					sweepLine.add(i, seg_Data_Index);
					return i;
				} else {
					// add to sweep line & return position
					sweepLine.add(i + 1, seg_Data_Index);
					return i + 1;
				}
			}
		}

		// add to sweep line & return position
		sweepLine.add(seg_Data_Index);
		return sweepLine.size() - 1;
	}

	// calculate counterclockwise test for given points
	private static double calcCcw(double p1_x, double p1_y, double p2_x, double p2_y, double p3_x, double p3_y) {
		return (p1_x * p2_y - p1_y * p2_x) + (p2_x * p3_y - p2_y * p3_x)
				+ (p1_y * p3_x - p1_x * p3_y);
	}

	// calculate if two lines cross
	private static boolean intersectionTest(int seg1_Data_Index, int seg2_Data_Index) {
		// initialize points
		Point2D.Double i1 = new Point2D.Double(data.get(seg1_Data_Index)[0], data.get(seg1_Data_Index)[1]);
		Point2D.Double i2 = new Point2D.Double(data.get(seg1_Data_Index)[2], data.get(seg1_Data_Index)[3]);
		Point2D.Double j1 = new Point2D.Double(data.get(seg2_Data_Index)[0], data.get(seg2_Data_Index)[1]);
		Point2D.Double j2 = new Point2D.Double(data.get(seg2_Data_Index)[2], data.get(seg2_Data_Index)[3]);
		// counterclockwise tests for the two lines
		double ccw1 = calcCcw(i1.x, i1.y, i2.x, i2.y, j1.x, j1.y);
		double ccw2 = calcCcw(i1.x, i1.y, i2.x, i2.y, j2.x, j2.y);
		double ccw3 = calcCcw(j1.x, j1.y, j2.x, j2.y, i1.x, i1.y);
		double ccw4 = calcCcw(j1.x, j1.y, j2.x, j2.y, i2.x, i2.y);
		// test if two lines cross (from script)
		// return boolean value if the two lines cross
		if (ccw1 == 0 && ccw2 == 0) {
			double lambda1 = (j1.x - i1.x) / (i2.x - i1.x);
			double lambda2 = (j2.x - i1.x) / (i2.x - i1.x);
			if (i1.x == j1.x || i1.x == j2.x || i2.x == j1.x || i2.x == j2.x) {
				return true;
			} else if ((0 <= lambda1 && lambda1 <= 1) || (0 <= lambda2 && lambda2 <= 1)) {
				return true;
			}
		} else if (ccw1 * ccw2 <= 0 && ccw3 * ccw4 <= 0) {
			return true;
		}

		return false;
	}
	
	public static double[] intersection(int seg1_Data_Index, int seg2_Data_Index) {
		// initialize points
		Point2D.Double i1 = new Point2D.Double(data.get(seg1_Data_Index)[0], data.get(seg1_Data_Index)[1]);
		Point2D.Double i2 = new Point2D.Double(data.get(seg1_Data_Index)[2], data.get(seg1_Data_Index)[3]);
		Point2D.Double j1 = new Point2D.Double(data.get(seg2_Data_Index)[0], data.get(seg2_Data_Index)[1]);
		Point2D.Double j2 = new Point2D.Double(data.get(seg2_Data_Index)[2], data.get(seg2_Data_Index)[3]);
		// calculate and return the coordinates of the intersection
		double lambda_denominator = ((i2.x - i1.x) * (j2.y - j1.y) - (i2.y - i1.y) * (j2.x - j1.x));
		if (lambda_denominator == 0) {
			double[] intersection = {i1.x, i1.y};
			return intersection;
		} else {
			double lambda = ((j1.x - i1.x) * (j2.y - j1.y) - (j1.y - i1.y) * (j2.x - j1.x)) / lambda_denominator;
			double[] intersection = {i1.x + lambda * (i2.x - i1.x), i1.y + lambda * (i2.y - i1.y)};
			return intersection;
		}
	}
	
	public static void treatStartpoint(EventPoint event, LinkedList<Integer> sweepLine, TreeMap<double[], EventPoint> eventQueue) {
		// add segment to sweep line
		int seg_SweepLine_Index = addToSweepLine(event.seg1_Data_Index, sweepLine);
		// intersection test with next segment, if segment not the first element in sweep line
		if (seg_SweepLine_Index > 0) {
			// add intersection event, if lines cross
			if (intersectionTest(sweepLine.get(seg_SweepLine_Index), sweepLine.get(seg_SweepLine_Index - 1))) {
				EventPoint intersectionEvent = new EventPoint();
				double[] intersection = intersection(sweepLine.get(seg_SweepLine_Index), sweepLine.get(seg_SweepLine_Index - 1));
				intersectionEvent.x = intersection[0];
				intersectionEvent.y = intersection[1];
				intersectionEvent.type = EventType.Intersection;
				intersectionEvent.seg1_Data_Index = sweepLine.get(seg_SweepLine_Index);
				intersectionEvent.seg2_Data_Index = sweepLine.get(seg_SweepLine_Index - 1);
				addToEventQueue(intersectionEvent, eventQueue);
			}
		}
		// intersection test with previous segment, if segment not the last element in sweep line
		if (seg_SweepLine_Index < sweepLine.size() - 1) {
			// add intersection event, if lines cross
			if (intersectionTest(sweepLine.get(seg_SweepLine_Index), sweepLine.get(seg_SweepLine_Index + 1))) {
				EventPoint intersectionEvent = new EventPoint();
				double[] intersection = intersection(sweepLine.get(seg_SweepLine_Index), sweepLine.get(seg_SweepLine_Index + 1));
				intersectionEvent.x = intersection[0];
				intersectionEvent.y = intersection[1];
				intersectionEvent.type = EventType.Intersection;
				intersectionEvent.seg1_Data_Index = sweepLine.get(seg_SweepLine_Index);
				intersectionEvent.seg2_Data_Index = sweepLine.get(seg_SweepLine_Index + 1);
				addToEventQueue(intersectionEvent, eventQueue);
			}
		}
	}
	
	public static void treatEndpoint(EventPoint event, LinkedList<Integer> sweepLine, TreeMap<double[], EventPoint> eventQueue) {
		// find current sweep line index of segment
		int seg_SweepLine_Index = -1;
		for (int i = 0; i < sweepLine.size(); i++) {
			if (event.seg1_Data_Index == sweepLine.get(i)) {
				seg_SweepLine_Index = i;
			}
		}

		// intersection test of previous & next segment, if segment not the first or last element in sweep line
		if (seg_SweepLine_Index > 0 && seg_SweepLine_Index < sweepLine.size() - 1) {
			// add intersection event, if lines cross
			if (intersectionTest(sweepLine.get(seg_SweepLine_Index - 1), sweepLine.get(seg_SweepLine_Index + 1))) {
				EventPoint intersectionEvent = new EventPoint();
				double[] intersection = intersection(sweepLine.get(seg_SweepLine_Index - 1), sweepLine.get(seg_SweepLine_Index + 1));
				intersectionEvent.x = intersection[0];
				intersectionEvent.y = intersection[1];
				intersectionEvent.type = EventType.Intersection;
				intersectionEvent.seg1_Data_Index = sweepLine.get(seg_SweepLine_Index - 1);
				intersectionEvent.seg2_Data_Index = sweepLine.get(seg_SweepLine_Index + 1);
				addToEventQueue(intersectionEvent, eventQueue);
			}
		}
		// remove segment from sweep line
		sweepLine.remove(seg_SweepLine_Index);
	}

	public static void treatIntersection(EventPoint event, LinkedList<Integer> sweepLine, TreeMap<double[], EventPoint> eventQueue) {
		numCrossedLines++; // increment crossed lines
		// find current sweep line indices of intersection segments
		int seg1_SweepLine_Index = -1, seg2_SweepLine_Index = -1;
		for (int i = 0; i < sweepLine.size(); i++) {
			if (event.seg1_Data_Index == sweepLine.get(i)) {
				seg1_SweepLine_Index = i;
				if (seg2_SweepLine_Index != -1) break;
			} else if (event.seg2_Data_Index == sweepLine.get(i)) {
				seg2_SweepLine_Index = i;
				if (seg1_SweepLine_Index != -1) break;
			}
		}

		// change position of the segments in sweep line
		sweepLine.set(seg1_SweepLine_Index, event.seg2_Data_Index);
		sweepLine.set(seg2_SweepLine_Index, event.seg1_Data_Index);

		// change order of segments, so the smaller one always is the first
		if (seg1_SweepLine_Index > seg2_SweepLine_Index) {
			int temp = seg1_SweepLine_Index;
			seg1_SweepLine_Index = seg2_SweepLine_Index;
			seg2_SweepLine_Index = temp;
		}

		// intersection test with next segment, if segment with smaller index not the first element in sweep line
		if (seg1_SweepLine_Index > 0) {
			// add intersection event, if lines cross
			if (intersectionTest(sweepLine.get(seg1_SweepLine_Index), sweepLine.get(seg1_SweepLine_Index - 1))) {
				EventPoint intersectionEvent = new EventPoint();
				double[] intersection = intersection(sweepLine.get(seg1_SweepLine_Index), sweepLine.get(seg1_SweepLine_Index - 1));
				intersectionEvent.x = intersection[0];
				intersectionEvent.y = intersection[1];
				intersectionEvent.type = EventType.Intersection;
				intersectionEvent.seg1_Data_Index = sweepLine.get(seg1_SweepLine_Index);
				intersectionEvent.seg2_Data_Index = sweepLine.get(seg1_SweepLine_Index - 1);
				addToEventQueue(intersectionEvent, eventQueue);
			}
		}
		// intersection test with previous segment, if segment with higher index not the last element in sweep line
		if (seg2_SweepLine_Index < sweepLine.size() - 1) {
			// add intersection event, if lines cross
			if (intersectionTest(sweepLine.get(seg2_SweepLine_Index), sweepLine.get(seg2_SweepLine_Index + 1))) {
				EventPoint intersectionEvent = new EventPoint();
				double[] intersection = intersection(sweepLine.get(seg2_SweepLine_Index), sweepLine.get(seg2_SweepLine_Index + 1));
				intersectionEvent.x = intersection[0];
				intersectionEvent.y = intersection[1];
				intersectionEvent.type = EventType.Intersection;
				intersectionEvent.seg1_Data_Index = sweepLine.get(seg2_SweepLine_Index);
				intersectionEvent.seg2_Data_Index = sweepLine.get(seg2_SweepLine_Index + 1);
				addToEventQueue(intersectionEvent, eventQueue);
			}
		}
	}

	public static int countCrossedLines(String datafile) throws IOException {
		intersectionsChecked = new TreeSet<Integer>(); // initialize checked intersections
		numCrossedLines = 0; // initialize number of crossed lines

		data = parseFile(datafile);
		TreeMap<double[], EventPoint> eventQueue = generateEventQueue(data); // add segments in data to event queue
		LinkedList<Integer> sweepLine = new LinkedList<Integer>(); // initialize sweep line

		// while event queue not empty, treat events in event queue 
		while (!eventQueue.isEmpty()) {
			EventPoint event = eventQueue.pollFirstEntry().getValue();
			switch (event.type) {
				case Startpoint:
					treatStartpoint(event, sweepLine, eventQueue);
					break;
				case Endpoint:
					treatEndpoint(event, sweepLine, eventQueue);
					break;
				case Intersection:
					treatIntersection(event, sweepLine, eventQueue);
				default:
					break;
			}
		}
		
		return numCrossedLines; // return number of crossed lines
	}
	
	public static void main(String[] args) throws IOException {
		// execute functions for different data files and print their results
		long timeStart = System.currentTimeMillis();
		int numCrossedLines = countCrossedLines(FILE1);
		long timeEnd = System.currentTimeMillis();
		System.out.println("Anzahl an Überschneidungen: " + numCrossedLines);
		System.out.println("Verlaufszeit s_1000_10.dat: " + (timeEnd - timeStart) + " Millisek.");
		
		timeStart = System.currentTimeMillis();
		numCrossedLines = countCrossedLines(FILE2);
		timeEnd = System.currentTimeMillis();
		System.out.println("\nAnzahl an Überschneidungen: " + numCrossedLines);
		System.out.println("Verlaufszeit s_1000_1.dat: " + (timeEnd - timeStart) + " Millisek.");
		
		timeStart = System.currentTimeMillis();
		numCrossedLines = countCrossedLines(FILE3);
		timeEnd = System.currentTimeMillis();
		System.out.println("\nAnzahl an Überschneidungen: " + numCrossedLines);
		System.out.println("Verlaufszeit s_10000_1.dat: " + (timeEnd - timeStart) + " Millisek.");
		
		timeStart = System.currentTimeMillis();
		numCrossedLines = countCrossedLines(FILE4);
		timeEnd = System.currentTimeMillis();
		System.out.println("\nAnzahl an Überschneidungen: " + numCrossedLines);
		System.out.println("Verlaufszeit s_100000_1.dat: " + (timeEnd - timeStart) + " Millisek.");
	}
}