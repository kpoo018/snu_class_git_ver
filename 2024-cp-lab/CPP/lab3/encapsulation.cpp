#include <iostream>
#include <string>

using namespace std;
/* class Person
{
    private:
        string name;
        int age;
    public:
        Person(string name, int age)
        {
            this->name = name;
            this->age = age;
        }

        void display()
        {
            cout << "Name: " << name << endl;
            cout << "Age: " << age << endl;
        }
        
};

class Student : public Person
{
    private:
        string major;
    public:
        Student(string name, int age, string major) : Person(name, age)
        {
            this->major = major;
        }

        void display()
        {
            Person::display();
            cout << "Major: " << major << endl;
        }
};

int main()
{
    Student s("John", 25, "Computer Science");
    s.display();
    return 0;
} */

class Parent
{
    public:
        virtual void display()
        {
            cout << "Parent" << endl;
        }
};

class Child : public Parent
{
    public:
        void display()
        {
            cout << "Child" << endl;
        }
};

int main()
{
    Parent a;
    Parent *p = new Child();
    p->display();
    return 0;
}