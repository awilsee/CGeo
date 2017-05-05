#include <rapidxml.hpp>

#include <iostream>
#include <fstream>
#include <sstream>

#include <vector>


#define FILE_PATH "DeutschlandMitStaedten.svg"

class Point {
public:
	double x;
	double y;
};

using namespace std;
using namespace rapidxml;

typedef vector<Point> points_t;
typedef vector<points_t> paths_t;
typedef vector<paths_t> states_t;

void parseFile(const string& filePath, xml_document<>& doc);
states_t generateStates(const xml_document<>& doc);
void parsePaths(string value, states_t& states);
vector<double> calculateStateAreas(states_t& states);
points_t generateCities(const xml_document<>& doc);
states_t findStatesOfCities(points_t cities, states_t states);



int main() {
	// Read xml file
	xml_document<> doc;
	parseFile(FILE_PATH, doc);
	states_t states = generateStates(doc);
	vector<double> stateAreas = calculateStateAreas(states);
	points_t cities = generateCities(doc);
	states_t cityStates = findStatesOfCities(cities, states);

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

	xml_node<> *pRoot = doc.first_node()->first_node();
	for (xml_node<> *pNode = pRoot->first_node(); pNode; pNode = pNode->next_sibling())
	{
		xml_attribute<> *pAttrID = pNode->first_attribute("id");
		string id = pAttrID->value();
		ids.push_back(id);
		xml_attribute<> *pAttrD = pNode->first_attribute("d");
		parsePaths(pAttrD->value(), states);
	}

	return states;
}

void parsePaths(string value, states_t& states) {

	//Ausnahme für 'H'
	size_t found = value.find('H');
	if (found != string::npos) {
		value.replace(found, 1, "\nH");
	}

	stringstream stream(value);
	string line;
	points_t newPoints;
	paths_t newPath;
	Point lastPoint{ 0.0, 0.0 };
	Point newPoint{ 0.0, 0.0 };
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
			newPath.push_back(newPoints);
			newPoints.clear();
			newPoint = { 0.0, 0.0 };
			break;
		}
		lastPoint = newPoint;
	}
	states.push_back(newPath);
}

vector<double> calculateStateAreas(states_t& states) {
	vector<double> stateAreas;

	for (int i = 0; i < states.size(); i++)
	{
		paths_t state = states[i];
		double stateArea = 0.0;

		for (int j = 0; j < state.size(); j++)
		{
			points_t path = state[j];
			int pathSize = path.size();
			double pathArea = 0.0;

			for (int k = 1; k <= pathSize; k++)
			{
				Point point_1 = path[(k - 1) % pathSize];
				Point point_2 = path[k % pathSize];
				Point point_3 = path[(k + 1) % pathSize];

				pathArea += point_2.y * (point_1.x - point_3.x) / 2;
			}

			stateArea += abs(pathArea);
			//stateArea *= 1.1805; 	// multiply with factor to compare with real areas
		}
		stateAreas.push_back(stateArea);
	}

	return stateAreas;
}

points_t generateCities(const xml_document<>& doc) {
	vector<string> ids;
	points_t cities;

	xml_node<> *pRoot = doc.first_node();
	for (xml_node<> *pNode = pRoot->first_node("path"); pNode; pNode = pNode->next_sibling())
	{
		Point city;
		xml_attribute<> *pAttrID = pNode->first_attribute("id");
		string id = pAttrID->value();
		ids.push_back(id);
		city.x = stod(pNode->first_attribute("sodipodi:cx")->value());
		city.y = stod(pNode->first_attribute("sodipodi:cy")->value());
		cities.push_back(city);
	}

	return cities;
}


states_t findStatesOfCities(points_t cities, states_t states) {
	states_t cityStates;

	//implementation

	return cityStates;
}