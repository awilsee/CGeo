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

typedef vector<Point> punkte_t;
typedef vector<punkte_t> pfade_t;
typedef vector<pfade_t> laender_t;

void parseFile(const string& filePath, xml_document<>& doc);
void parseXML(const xml_document<>& doc);
void parsePaths(string value, laender_t& laender);


int main()
{
	// Read xml file
	xml_document<> doc;
	parseFile(FILE_PATH, doc);
	parseXML(doc);

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

void parseXML(const xml_document<>& doc) {
	vector<string> ids;
	laender_t laender;

	xml_node<> *pRoot = doc.first_node()->first_node();
	for (xml_node<> *pNode = pRoot->first_node(); pNode; pNode = pNode->next_sibling())
	{
		xml_attribute<> *pAttrID = pNode->first_attribute("id");
		string id = pAttrID->value();
		ids.push_back(id);
		xml_attribute<> *pAttrD = pNode->first_attribute("d");
		parsePaths(pAttrD->value(), laender);
	}
}

void parsePaths(string value, laender_t& laender) {

	//Ausnahme für 'H'
	size_t found = value.find('H');
	if (found != string::npos) {
		value.replace(found, 1, "\nH");
	}
	
	stringstream stream(value);
	string line;
	punkte_t newPunkte;
	pfade_t newPfad;
	Point lastPoint{ 0.0, 0.0 };
	Point newPoint{ 0.0, 0.0 };
	while (getline(stream, line, '\n')) {
		switch (line.c_str()[0]) {
		case 'M':
			sscanf_s(line.c_str(), "M%lf,%lf", &newPoint.x, &newPoint.y);
			newPunkte.push_back(newPoint);
			break;
		case 'l': {
			Point calcPoint{ 0.0, 0.0 };
			sscanf_s(line.c_str(), "l%lf,%lf", &calcPoint.x, &calcPoint.y);
			newPoint = { lastPoint.x + calcPoint.x, lastPoint.y + calcPoint.y };
			newPunkte.push_back(newPoint);
			break;
		}
		case 'L':
			sscanf_s(line.c_str(), "L%lf,%lf", &newPoint.x, &newPoint.y);
			newPunkte.push_back(newPoint);
			break;
		case 'H':
			sscanf_s(line.c_str(), "H%lf", &newPoint.x);
			newPoint.y = lastPoint.y;
			newPunkte.push_back(newPoint);
			break;
		case 'z':
			newPfad.push_back(newPunkte);
			newPoint = { 0.0, 0.0 };
			break;
		}
		lastPoint = newPoint;
	}
	laender.push_back(newPfad);
}