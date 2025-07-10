#include <iostream>
#include <fstream>
#include <string>
using namespace std;

int main()
{
    ofstream outputFile ("example3.txt");
    ifstream inputFile ;

    streampos begin, end;

    outputFile << "0123456789";
    outputFile.close();

    inputFile.open("example3.txt");

    begin = inputFile.tellg();
    inputFile.seekg(0, ios::end);
    end = inputFile.tellg();

    inputFile.close();

    cout << end - begin << endl;

    return 0;
}