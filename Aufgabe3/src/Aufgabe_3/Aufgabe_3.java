package Aufgabe_3;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.transformation.SortedList;


class Aufgabe3 {

	private static final String FILE1 = "data\\s_1000_1.dat";
	private static final String FILE2 = "data\\s_1000_10.dat";
	public static ArrayList<double[]> data;
	
	private enum EventType {
		Startpoint, Endpoint, Intersection
	}
	
	private static class EventPoint {
		public double x = 0, y = 0;
		public EventType type = EventType.Startpoint;
		public int seg1_Data_Index = -1, seg2_Data_Index = -1;
	}

	private static ArrayList<double[]> parseFile(String fileName) throws IOException {
		FileReader fr = new FileReader(fileName);
		BufferedReader br = new BufferedReader(fr);

		String zeile = "";
		zeile = br.readLine();

		data = new ArrayList<double[]>();

		while (zeile != null) {
			int j = 0;
			String[] row = zeile.split(" ");
			double[] dataLine = new double[4];
			for (String str : row) {
				dataLine[j++] = Double.parseDouble(str);
			}
			
			if (dataLine[0] > dataLine[2] ||
					(dataLine[0] == dataLine[2] && dataLine[1] > dataLine[3])) {
				double temp = dataLine[0];
				dataLine[0] = dataLine[2];
				dataLine[2] = temp;
				temp = dataLine[1];
				dataLine[1] = dataLine[3];
				dataLine[3] = temp;
			}
			
			data.add(dataLine);
			zeile = br.readLine();
		}
		;

		br.close();

		return data;
	}

	private static SortedList<EventPoint> generateEventQueue(ArrayList<double[]> data) {
		SortedList<EventPoint> eventQueue = new SortedList<EventPoint>(FXCollections.observableArrayList());
		
		for (int i = 0; i < data.size(); i++) {
			EventPoint event = new EventPoint();
			event.x = data.get(i)[0];
			event.y = data.get(i)[1];
			event.type = EventType.Startpoint;
			event.seg1_Data_Index = i;
			addToSortedList(event, eventQueue);
			
			event.x = data.get(i)[2];
			event.y = data.get(i)[3];
			event.type = EventType.Endpoint;
			addToSortedList(event, eventQueue);
		}
		return eventQueue;
	}
	
	public static void addToSortedList(EventPoint event, SortedList<EventPoint> eventQueue) {
		if (event.type == EventType.Intersection && event.seg1_Data_Index < event.seg2_Data_Index) {
			int temp = event.seg1_Data_Index;
			event.seg1_Data_Index = event.seg2_Data_Index;
			event.seg2_Data_Index = temp;
		}
		if (eventQueue.size() == 0) {
			eventQueue.add(0, event);
		}
		for (int i = 0; i < eventQueue.size(); i++) {
			EventPoint queue_Event = eventQueue.get(i);
			if (event.x < queue_Event.x) {
				eventQueue.add(i, event);
				break;
			} else if (event.x == queue_Event.x && event.y < queue_Event.y) {
				eventQueue.add(i, event);
				break;
			} else if (event.x == queue_Event.x && event.y == queue_Event.y) {
				if (event.type != EventType.Intersection || queue_Event.type != EventType.Intersection
						|| event.seg1_Data_Index != queue_Event.seg1_Data_Index || event.seg2_Data_Index != queue_Event.seg2_Data_Index) {
					eventQueue.add(i, event);
				}
				break;
			}
		}
	}
	
	public static int addToSweepLine(int seg_Data_Index, SortedList<Integer> sweepLine) {
		double[] newSeg = data.get(seg_Data_Index);
		
		for (int i = 0; i < sweepLine.size(); i++) {
			double[] compSeg = data.get(sweepLine.get(i));
			double compSeg_y = (newSeg[0] - compSeg[0]) / (compSeg[2] - compSeg[0]) * (compSeg[3] - compSeg[1]) + compSeg[1];
			
			if (newSeg[1] < compSeg_y) {
				sweepLine.add(i, seg_Data_Index);
				return i;
			} else if (newSeg[1] == compSeg_y) {
				double x;
				if (newSeg[2] <= compSeg[2]) x = newSeg[2];
				else x = compSeg[2];

				double y1 = (x - newSeg[0]) / (newSeg[2] - newSeg[0]) * (newSeg[3] - newSeg[1]) + newSeg[1];
				double y2 = (x - compSeg[0]) / (compSeg[2] - compSeg[0]) * (compSeg[3] - compSeg[1]) + compSeg[1];

				if (y1 >= y2) {
					sweepLine.add(i, seg_Data_Index);
				} else {
					sweepLine.add(i + 1, seg_Data_Index);
				}

				return i;
			}
		}

		return -1;
	}

	private static double calcCcw(double p1_x, double p1_y, double p2_x, double p2_y, double p3_x, double p3_y) {
		return (p1_x * p2_y - p1_y * p2_x) + (p2_x * p3_y - p2_y * p3_x)
				+ (p1_y * p3_x - p1_x * p3_y);
	}

	private static boolean intersectionTest(int seg1_Data_Index, int seg2_Data_Index) {
		double i1_x = data.get(seg1_Data_Index)[0];
		double i1_y = data.get(seg1_Data_Index)[1];
		double i2_x = data.get(seg1_Data_Index)[2];
		double i2_y = data.get(seg1_Data_Index)[3];
		double j1_x = data.get(seg2_Data_Index)[0];
		double j1_y = data.get(seg2_Data_Index)[1];
		double j2_x = data.get(seg2_Data_Index)[2];
		double j2_y = data.get(seg2_Data_Index)[3];
		double ccw1 = calcCcw(i1_x, i1_y, i2_x, i2_y, j1_x, j1_y);
		double ccw2 = calcCcw(i1_x, i1_y, i2_x, i2_y, j2_x, j2_y);
		double ccw3 = calcCcw(j1_x, j1_y, j2_x, j2_y, i1_x, i1_y);
		double ccw4 = calcCcw(j1_x, j1_y, j2_x, j2_y, i2_x, i2_y);
		if (ccw1 == 0 && ccw2 == 0) {
			double lambda1 = (j1_x - i1_x) / (i2_x - i1_x);
			double lambda2 = (j2_x - i1_x) / (i2_x - i1_x);
			if (i1_x == j1_x || i1_x == j2_x || i2_x == j1_x || i2_x == j2_x) {
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
		double i1_x = data.get(seg1_Data_Index)[0];
		double i1_y = data.get(seg1_Data_Index)[1];
		double i2_x = data.get(seg1_Data_Index)[2];
		double i2_y = data.get(seg1_Data_Index)[3];
		double j1_x = data.get(seg2_Data_Index)[0];
		double j1_y = data.get(seg2_Data_Index)[1];
		double j2_x = data.get(seg2_Data_Index)[2];
		double j2_y = data.get(seg2_Data_Index)[3];
		double lambda_denominator = ((i2_x - i1_x) * (j2_y - j1_y) - (i2_y - i1_y) * (j2_x - j1_x));
		if (lambda_denominator == 0) {
			double[] intersection = {i1_x, i1_y};
			return intersection;
		} else {
			double lambda = ((j1_x - i1_x) * (j2_y - j1_y) - (j1_y - i1_y) * (j2_x - j1_x)) / lambda_denominator;
			double[] intersection = {i1_x + lambda * (i2_x - i1_x), i1_y + lambda * (i2_y - i1_y)};
			return intersection;
		}
	}
	
	public static void treatStartpoint(EventPoint event, SortedList<Integer> sweepLine, SortedList<EventPoint> eventQueue) {
		int seg_SweepLine_Index = addToSweepLine(event.seg1_Data_Index, sweepLine);
		if (seg_SweepLine_Index > 0) {
			if (intersectionTest(seg_SweepLine_Index, seg_SweepLine_Index - 1)) {
				EventPoint intersectionEvent = new EventPoint();
				double [] intersection = intersection(seg_SweepLine_Index, seg_SweepLine_Index - 1);
				intersectionEvent.x = intersection[0];
				intersectionEvent.y = intersection[1];
				intersectionEvent.type = EventType.Intersection;
				intersectionEvent.seg1_Data_Index = sweepLine.get(seg_SweepLine_Index);
				intersectionEvent.seg2_Data_Index = sweepLine.get(seg_SweepLine_Index - 1);
				addToSortedList(intersectionEvent, eventQueue);
			}
		}
		if (seg_SweepLine_Index < sweepLine.size() - 1) {
			if (intersectionTest(seg_SweepLine_Index, seg_SweepLine_Index + 1)) {
				EventPoint intersectionEvent = new EventPoint();
				double [] intersection = intersection(seg_SweepLine_Index, seg_SweepLine_Index + 1);
				intersectionEvent.x = intersection[0];
				intersectionEvent.y = intersection[1];
				intersectionEvent.type = EventType.Intersection;
				intersectionEvent.seg1_Data_Index = sweepLine.get(seg_SweepLine_Index);
				intersectionEvent.seg2_Data_Index = sweepLine.get(seg_SweepLine_Index + 1);
				addToSortedList(intersectionEvent, eventQueue);
			}
		}
	}
	
	public static void treatEndpoint(EventPoint event, SortedList<Integer> sweepLine, SortedList<EventPoint> eventQueue) {
		int seg_SweepLine_Index = -1;
		for (int i = 0; i < sweepLine.size(); i++) {
			if (event.seg1_Data_Index == sweepLine.get(i)) {
				seg_SweepLine_Index = sweepLine.get(i);
			}
		}

		if (seg_SweepLine_Index > 0 && seg_SweepLine_Index < sweepLine.size() - 1) {
			if (intersectionTest(sweepLine.get(seg_SweepLine_Index - 1), sweepLine.get(seg_SweepLine_Index + 1))) {
				EventPoint intersectionEvent = new EventPoint();
				double [] intersection = intersection(sweepLine.get(seg_SweepLine_Index - 1), sweepLine.get(seg_SweepLine_Index + 1));
				intersectionEvent.x = intersection[0];
				intersectionEvent.y = intersection[1];
				intersectionEvent.type = EventType.Intersection;
				intersectionEvent.seg1_Data_Index = sweepLine.get(seg_SweepLine_Index - 1);
				intersectionEvent.seg2_Data_Index = sweepLine.get(seg_SweepLine_Index + 1);
				addToSortedList(intersectionEvent, eventQueue);
			}
			sweepLine.remove(seg_SweepLine_Index);
		}
	}
	
	public static void treatIntersection(EventPoint event, SortedList<Integer> sweepLine, SortedList<EventPoint> eventQueue, int numCrossedLines) {
		numCrossedLines++;
		int seg1_SweepLine_Index = -1, seg2_SweepLine_Index = -1;
		for (int i = 0; i < sweepLine.size(); i++) {
			if (event.seg1_Data_Index == sweepLine.get(i)) {
				seg1_SweepLine_Index = sweepLine.get(i);
				if (seg2_SweepLine_Index != -1) break;
			} else if (event.seg2_Data_Index == sweepLine.get(i)) {
				seg2_SweepLine_Index = sweepLine.get(i);
				if (seg1_SweepLine_Index != -1) break;
			}
		}
		
		sweepLine.set(seg1_SweepLine_Index, event.seg2_Data_Index);
		sweepLine.set(seg2_SweepLine_Index, event.seg1_Data_Index);

		if (seg1_SweepLine_Index > 0) {
			if (intersectionTest(sweepLine.get(seg1_SweepLine_Index), sweepLine.get(seg1_SweepLine_Index - 1))) {
				EventPoint intersectionEvent = new EventPoint();
				double [] intersection = intersection(sweepLine.get(seg1_SweepLine_Index), sweepLine.get(seg1_SweepLine_Index - 1));
				intersectionEvent.x = intersection[0];
				intersectionEvent.y = intersection[1];
				intersectionEvent.type = EventType.Intersection;
				intersectionEvent.seg1_Data_Index = sweepLine.get(seg1_SweepLine_Index);
				intersectionEvent.seg2_Data_Index = sweepLine.get(seg1_SweepLine_Index - 1);
				addToSortedList(intersectionEvent, eventQueue);
			}
		}
		if (seg2_SweepLine_Index < sweepLine.size() - 1) {
			if (intersectionTest(sweepLine.get(seg2_SweepLine_Index), sweepLine.get(seg2_SweepLine_Index + 1))) {
				EventPoint intersectionEvent = new EventPoint();
				double [] intersection = intersection(sweepLine.get(seg2_SweepLine_Index), sweepLine.get(seg2_SweepLine_Index + 1));
				intersectionEvent.x = intersection[0];
				intersectionEvent.y = intersection[1];
				intersectionEvent.type = EventType.Intersection;
				intersectionEvent.seg1_Data_Index = sweepLine.get(seg2_SweepLine_Index);
				intersectionEvent.seg2_Data_Index = sweepLine.get(seg2_SweepLine_Index + 1);
				addToSortedList(intersectionEvent, eventQueue);
			}
		}
	}

	public static void main(String[] args) throws IOException {
		long timeStart = System.currentTimeMillis();
		ArrayList<double[]> data = parseFile(FILE1);
		SortedList<EventPoint> eventQueue = generateEventQueue(data);
		SortedList<Integer> sweepLine = new SortedList<Integer>(FXCollections.observableArrayList()); 
		int numCrossedLines = 0;
		
		while (!eventQueue.isEmpty()) {
			System.out.println(1);
			EventPoint event = eventQueue.get(0);
			if (event.type == EventType.Startpoint) {
				treatStartpoint(event, sweepLine, eventQueue);
			} else if (event.type == EventType.Endpoint) {
				treatEndpoint(event, sweepLine, eventQueue);
			} else if (event.type == EventType.Intersection) {
				treatIntersection(event, sweepLine, eventQueue, numCrossedLines);
			}
			eventQueue.remove(0);
		}
		
		long timeEnd = System.currentTimeMillis();
		System.out.println("Number of crossed lines: " + numCrossedLines);
		System.out.println("Verlaufszeit der Datei: " + (timeEnd - timeStart) + " Millisek.");
	}
}