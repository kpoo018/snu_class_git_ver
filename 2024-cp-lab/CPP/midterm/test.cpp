#include <iostream>
using namespace std;

class Parent {
    public:
        virtual void PubPrint(){
            cout<<"A";
        }
    private:
        virtual void PrivPrint(){
            cout<<"B";
        }
    protected:
        virtual void ProtPrint(){
            cout<<"C";
        }
};

class Child : public Parent {

    public:
        void PubPrint(){
            cout<<"a";
        }
        void PrivPrint(){
            cout<<"b";
        }
        void ProtPrint(){
            cout<<"c";
        }
};

int main(){
    Parent* p = new Child;
    p->PubPrint();
    p->PrivPrint();
    p->ProtPrint();
    
    return 0;
}