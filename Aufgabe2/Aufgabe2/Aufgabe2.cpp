#include <rapidxml.hpp>

#include <iostream>
#include <fstream>
#include <sstream>

#include <vector>


#define FILE_PATH "DeutschlandMitStaedten.svg"

using namespace std;
using namespace rapidxml;

class Point {
public:
	double x;
	double y;
};

typedef vector<Point> points_t;
typedef vector<points_t> paths_t;

class State {
public:
	string name;
	paths_t paths;
	double area;
};

class City {
public:
	string name;
	Point point;
};

typedef vector<State> states_t;
typedef vector<City> cities_t;



void parseFile(const string& filePath, xml_document<>& doc);
states_t generateStates(const xml_document<>& doc);
void parsePaths(string value, paths_t& paths);
void calculateStateAreas(states_t& states);
cities_t generateCities(const xml_document<>& doc);
State findStateOfCity(City& city, states_t& states);
int calcCrossedLines(Point& j1, Point& j2, points_t& path);
double calcCcw(Point& p1, Point& p2, Point& p3);



int main() {
	// read xml file
	xml_document<> doc;
	parseFile(FILE_PATH, doc);
	// generate states with data from file
	states_t states = generateStates(doc);
	// calculate the sizes (areas) of the states
	calculateStateAreas(states);
	// print states sizes (areas)
	for (int i = 0; i < states.size(); i++)
	{
		State state = states[i];
		cout << "Die Fläche von " << state.name << " beträgt " << state.area << endl;
	}
	cout << endl;
	// generate cities with data from file
	cities_t cities = generateCities(doc);
	// find and print the states in which the cities lie
	for (int i = 0; i < cities.size(); i++)
	{
		City city = cities[i];
		State cityState = findStateOfCity(city, states);
		cout << city.name << " liegt in " << cityState.name << endl;
	}

	return 0;
}

void parseFile(const string& filePath, xml_document<>& doc) {
	ifstream file(filePath);

	stringstream buffer;
	buffer << file.rdbuf();
	file.close();
	static string content(buffer.str());
	doc.parse<0>(&content[0]);
}

states_t generateStates(const xml_document<>& doc) {
	vector<string> ids;
	states_t states;

	// scan states from xml nodes
	xml_node<> *pRoot = doc.first_node()->first_node();
	for (xml_node<> *pNode = pRoot->first_node(); pNode; pNode = pNode->next_sibling())
	{
		State state;
		xml_attribute<> *pAttrID = pNode->first_attribute("id");
		state.name = pAttrID->value();
		xml_attribute<> *pAttrD = pNode->first_attribute("d");
		parsePaths(pAttrD->value(), state.paths);
		states.push_back(state);
	}

	return states;
}

void parsePaths(string value, paths_t& paths) {

	// exception for a special line in the file, where 'H' isn't in a new line
	size_t found = value.find('H');
	if (found != string::npos) {
		value.replace(found, 1, "\nH");
	}

	stringstream stream(value);
	string line;
	points_t newPoints;
	// define points to recalculate relative values to absolute values
	Point lastPoint{ 0.0, 0.0 };
	Point newPoint{ 0.0, 0.0 };
	// define parsing rules
	while (getline(stream, line, '\n')) {
		switch (line.c_str()[0]) {
		case 'M':
			sscanf_s(line.c_str(), "M%lf,%lf", &newPoint.x, &newPoint.y);
			newPoints.push_back(newPoint);
			break;
		case 'l': {
			Point calcPoint{ 0.0, 0.0 };
			sscanf_s(line.c_str(), "l%lf,%lf", &calcPoint.x, &calcPoint.y);
			newPoint = { lastPoint.x + calcPoint.x, lastPoint.y + calcPoint.y };
			newPoints.push_back(newPoint);
			break;
		}
		case 'L':
			sscanf_s(line.c_str(), "L%lf,%lf", &newPoint.x, &newPoint.y);
			newPoints.push_back(newPoint);
			break;
		case 'H':
			sscanf_s(line.c_str(), "H%lf", &newPoint.x);
			newPoint.y = lastPoint.y;
			newPoints.push_back(newPoint);
			break;
		case 'z':
			paths.push_back(newPoints);
			newPoints.clear();
			newPoint = { 0.0, 0.0 };
			break;
		}
		lastPoint = newPoint;
	}
}

void calculateStateAreas(states_t& states) {
	// initialize size (area) of states
	for (int i = 0; i < states.size(); i++) // iterate through states
	{
		states[i].area = 0.0;
	}
	// calculate sizes (areas) of states
	for (int i = 0; i < states.size(); i++) // iterate through states
	{
		State& state = states[i];
		double stateArea = 0.0; // initiate size (area) for path of state

		for (int j = 0; j < state.paths.size(); j++) // iterate through paths of state
		{
			points_t path = state.paths[j];
			int pathSize = path.size();
			double pathArea = 0.0; // initiate size (area) of path

			for (int k = 1; k <= pathSize; k++) // iterate through points in path
			{
				// set points for size of path calculation
				Point point_1 = path[(k - 1) % pathSize];
				Point point_2 = path[k % pathSize];
				Point point_3 = path[(k + 1) % pathSize];

				pathArea += point_2.y * (point_1.x - point_3.x) / 2; // calculate size of path
			}

			stateArea += abs(pathArea); // add size of path to size of state
			//stateArea *= 1.1805; // multiply with factor to compare with real sizes
		}
		state.area += stateArea;

		// subtract size of Berlin from Brandenburg & Bremen from Niedersachsen
		if ((state.name == "Berlin") || (state.name == "Bremen")) {
			for (int j = 0; j < states.size(); j++) // iterate through states
			{
				if ((state.name == "Berlin" && states[j].name == "Brandenburg") || (state.name == "Bremen" && states[j].name == "Niedersachsen")) { // if not the same state
					states[j].area -= stateArea;
				}
			}
		}
	}

	cities_t generateCities(const xml_document<>& doc) {
		vector<string> ids;
		cities_t cities;

		// scan cities from xml nodes
		xml_node<> *pRoot = doc.first_node();
		for (xml_node<> *pNode = pRoot->first_node("path"); pNode; pNode = pNode->next_sibling())
		{
			City city;
			xml_attribute<> *pAttrID = pNode->first_attribute("id");
			city.name = pAttrID->value();
			city.point.x = stod(pNode->first_attribute("sodipodi:cx")->value());
			city.point.y = stod(pNode->first_attribute("sodipodi:cy")->value());
			cities.push_back(city);
		}

		return cities;
	}

	int testPointInState(Point& point, State& state) {
		Point outOfPath = { -1, -1 }; // set point outside of path (P(-1,-1) doesn't lie in any of the states as coordinates are positive)

		for (int i = 0; i < state.paths.size(); i++) // iterate through paths of state
		{
			int numCrossedLines = calcCrossedLines(point, outOfPath, state.paths[i]); // calculate number of crosses

			if (numCrossedLines % 2 == 1)  // if number of crosses odd, point in state
				return i; // return number of path
		}
		return -1; // point not in state
	}

	State findStateOfCity(City& city, states_t& states) {
		State cityState;

		for (int j = 0; j < states.size(); j++) // iterate through states
		{
			State state = states[j];
			int path_id = -1;

			int testPath_id = testPointInState(city.point, state); // test if city lies in state and safe that path (if existing)
			if (testPath_id >= 0) { // if city lies in state
				if (path_id >= 0) {	// if city already lies in another state
					if (testPointInState(cityState.paths[path_id][0], state) >= 0) { // if new state of city lies in existing state of city
						// change state of city
						cityState = state;
						path_id = testPath_id;
					}
				}
				else {
					// set state of city
					cityState = state;
					path_id = testPath_id;
					break;
				}
			}
		}
		return cityState;
	}

	// calculates the number of crosses of the line between the points j1 & j2 and the lines on the path
	int calcCrossedLines(Point& j1, Point& j2, points_t& path) {
		int numCrosses = 0; // initiate number of crosses

		for (int i = 0; i < path.size(); i++) // iterate through points in path
		{
			// set points on the path to test the line between them against line between points j1 & j2
			Point i1 = path[i];
			Point i2 = path[(i + 1) % path.size()];

			// calculate ccw's between the 4 points
			double ccw1 = calcCcw(i1, i2, j1);
			double ccw2 = calcCcw(i1, i2, j2);
			double ccw3 = calcCcw(j1, j2, i1);
			double ccw4 = calcCcw(j1, j2, i2);

			// calculate if lines between the points cross
			if (ccw1 == 0 && ccw2 == 0) {
				double lambda1 = (j1.x - i1.x) / (i2.x - i1.x);
				double lambda2 = (j2.x - i1.x) / (i2.x - i1.x);
				if ((0 <= lambda1 && lambda1 <= 1) || (0 <= lambda2 && lambda2 <= 1)) {
					numCrosses++;
				}
			}
			else if (ccw1 * ccw2 <= 0 && ccw3 * ccw4 <= 0) {
				numCrosses++;
			}
		}

		return numCrosses;
	}

	double calcCcw(Point& p1, Point& p2, Point& p3) {
		return (p1.x * p2.y - p1.y * p2.x) + (p2.x * p3.y - p2.y * p3.x) + (p1.y * p3.x - p1.x * p3.y);
	}