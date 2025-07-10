#include <iostream>
#include <fstream>
#include <string>
using namespace std;

int main()
{
    ifstream inputFile ("example1.txt");
    ofstream outputFile ("exercise1.txt");
    string s;

    if(inputFile.is_open()) {
        while(getline(inputFile, s))
        outputFile << s; 
    }

    inputFile.close();
    outputFile.close();

    return 0;
}