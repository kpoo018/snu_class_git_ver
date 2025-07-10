#include <iostream>
#include <fstream>
using namespace std;

int main()
{
    ofstream myFile ("C:\\2024-CP-LAB\\lab5\\example1.txt");
    myFile << "Writing this to a file.\n";
    myFile.close();

    return 0;
}