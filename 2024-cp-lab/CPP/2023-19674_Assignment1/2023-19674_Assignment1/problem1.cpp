#include <iostream>
#include <string>

const int MAX_STUDENTS = 3;
enum ContestResult
{
    WIN,
    LOSE,
    TIE
};

// The Student class has the following common attributes:
// - name: string
// - social: int (default: 0)
// - energy: int (default: 100)
// However, each student has different attributes according to their type. This is called 'special attribute'
//   Scientist - knowledge: int (default: 0, can be negative)
//   Athlete - health: int (default: 0, can be negative)
//   Artist - creativity: int (default: 0, can be negative)
// These attributes are only accessible by the derived classes and itself.
// They are not accessible by any other function outside the class.
// Implement the getters if needed!

// Scientist: Study
    //  - If energy is greater than or equal to 15, incerase knowledge by 20 and decrease energy by 15
    //  - If energy is less than 15, display a message "${StudentName} is too tired to study."
    //  - Display the status of the student
    // Athlete: Exercise
    //  - If energy is greater than or equal to 15, increase health by 20 and decrease energy by 15
    //  - If energy is less than 15, display a message "${StudentName} is too tired to exercise."
    //  - Display the status of the student
    // Artist: Work on art
    //  - If energy is greater than or equal to 15, increase creativity by 20 and decrease energy by 15
    //  - If energy is less than 15, display a message "${StudentName} is too tired to work on art."
    //  - Display the status of the student
    // *There is no case of overflow


class Student
{
protected:
    std::string name;
    int social;
    int energy;
public:
    
    Student(std::string name) {
        this->name = name;
        this->social=0;
        this->energy=100;
    }
    virtual ~Student() {
    }

    void rest()
    {
        this->energy += 50;
        showStatus();
        // 1. Resting increases energy by 50
        // 2. Display the status of the student
    }

    void meetFriends()
    {
        if(this->energy >=15){
            this->social+=20;
            this->energy-=15;
        }
        else {
            std::cout << this->name <<" is too tired to meet friends." << std::endl;
        }
        showStatus();
        // 1. If energy is greater than or equal to 15, increase social by 20 and decrease energy by 15
        // 2. If energy is less than 15, display a message "${StudentName} is too tired to meet friends."
        // 3. Display the status of the student
        // *There is no case of overflow
    }

    // There are three types of students: Scientist, Athlete, and Artist
    // Each student has different activities to do and different effects on their stats
    // Override this method in the derived classes
    // Scientist: Study
    //  - If energy is greater than or equal to 15, incerase knowledge by 20 and decrease energy by 15
    //  - If energy is less than 15, display a message "${StudentName} is too tired to study."
    //  - Display the status of the student
    // Athlete: Exercise
    //  - If energy is greater than or equal to 15, increase health by 20 and decrease energy by 15
    //  - If energy is less than 15, display a message "${StudentName} is too tired to exercise."
    //  - Display the status of the student
    // Artist: Work on art
    //  - If energy is greater than or equal to 15, increase creativity by 20 and decrease energy by 15
    //  - If energy is less than 15, display a message "${StudentName} is too tired to work on art."
    //  - Display the status of the student
    // *There is no case of overflow
    virtual void doActivity() = 0;

    // Return the sum of the special attribute and social and energy
    virtual int getStats() = 0;

    // 1. If the result is WIN, increase special attribute by 10 and decrease energy by 10
    // 2. If the result is LOSE, decrease special attribute and energy by 10
    // 3. If the result is TIE, decrease energy by 10
    // 4. Display the status of the student
    // *There is no case of overflow/underflow
    virtual void updateAfterContest(ContestResult result) = 0;

    // Display the status of the student
    // For example, if the name is "John" and its type is Scientist, with knowledge: 0, social: 0, energy: 0,
    // print "Status of John: Knowledge: 0, Social: 0, Energy: 0"
    virtual void showStatus() = 0;

    std::string getName(){
        return this->name;
    }
    int getEnergy(){
        return this->energy;
    }
    int getSocial(){
        return this->social;
    }
};


class Scientist : public Student
{
protected:
    int knowledge;

public:
    Scientist(std::string name) : Student(name)
    {
        this->knowledge =0;
    } 

    void doActivity(){
        if(this->energy >= 15){
            this->knowledge += 20;
            this->energy -=15;
        }
        else {
            std::cout << this->name << " is to tired to study." << std::endl ;
        }
        showStatus();
    }

    int getStats(){
        return this->social + this->energy + this->knowledge;
    }

    void updateAfterContest(ContestResult result){
        
        switch (result)
        {
        case WIN:
            this->knowledge +=10;
            break;
        case LOSE:
            this->energy -=15;
            break;
        case TIE:
            this->energy -=10;
            break;
        }
        showStatus();
    }

    // print "Status of John: Knowledge: 0, Social: 0, Energy: 0"

    void showStatus() {
        std::cout << "Status of "<< this->name <<": Knowledge: "<<this->knowledge<<", Social: "<<this->social<<", Energy: "<< this->energy<<std::endl;
    }
};

class Athlete : public Student
{
protected:
    int health ;

public:
    Athlete(std::string name) : Student(name)
    {
        this->health =0;
    } 

    void doActivity(){
        if(this->energy >= 15){
            this->health += 20;
            this->energy -=15;
        }
        else {
            std::cout << this->name << " is to tired to exercise." << std::endl ;
        }
        showStatus();
    }

    int getStats(){
        return this->social + this->energy + this->health;
    }

    void updateAfterContest(ContestResult result){
        
        switch (result)
        {
        case WIN:
            this->health +=10;
            break;
        case LOSE:
            this->energy -=15;
            break;
        case TIE:
            this->energy -=10;
            break;
        }
        showStatus();
    }

    void showStatus() {
        std::cout << "Status of "<< this->name <<": Health: "<<this->health<<", Social: "<<this->social<<", Energy: "<< this->energy<<std::endl;
    }
};

class Artist : public Student
{
protected:
    int creativity ;   

public:
    Artist(std::string name) : Student(name)
    {
        this->creativity =0;
    } 

    void doActivity(){
        if(this->energy >= 15){
            this->creativity += 20;
            this->energy -=15;
        }
        else {
            std::cout << this->name << " is to tired to work on art." << std::endl ;
        }
        showStatus();
    }

    int getStats(){
        return this->social + this->energy + this->creativity;
    }

    void updateAfterContest(ContestResult result){
        
        switch (result)
        {
        case WIN:
            this->creativity +=10;
            break;
        case LOSE:
            this->energy -=15;
            break;
        case TIE:
            this->energy -=10;
            break;
        }
        showStatus();
    }

    void showStatus() {
        std::cout << "Status of "<< this->name <<": Creativity: "<<this->creativity<<", Social: "<<this->social<<", Energy: "<< this->energy<<std::endl;
    }
};

class Game
{
private:
    Student *students[MAX_STUDENTS];
    int studentCount;

public:
    Game()
    {
        studentCount = 0;
    }
    ~Game()
    {
    }

    Student *getStudent(std::string name)
    {
        for(int i=0; i<studentCount; i++){
            if(students[i]->getName() == name){
                return students[i];
            }
        }
        //std::cout<<"You're not my Student!!!"<<std::endl;
        //exit;
        // Return the student whose name is {name}
        // *There is no case where two students have the same name
        // *There is no case where the student with the given name does not exist
    }

    void trainStudent()
    {
        int choice;
        std::string name;
        std::cout<<"Enter student name: ";
        std::cin >> name;
        
        while(1){        
        std::cout<<"--- Training Menu ---"<<std::endl;
        std::cout<<"1. Do Activity"<<std::endl;
        std::cout<<"2. Meet Friends"<<std::endl;
        std::cout<<"3. Rest"<<std::endl;
        std::cout<<"4. Show Status"<<std::endl;
        std::cout<<"5. Exit"<<std::endl;
        std::cout<<"Enter your choice: ";
        std::cin>>choice;
        std::cout<<"----------------------"<<std::endl;

        switch (choice)
        {
        case 1:
            getStudent(name)->doActivity();
            break;
        case 2:
            getStudent(name)->meetFriends();
            break;
        case 3:
            getStudent(name)->rest();
            break;
        case 4:
            getStudent(name)->showStatus();
            break;
        case 5:
            return;
            break;
        default:
            break;
        }
        
        }



        /*
        --- Training Menu ---
        1. Do Activity
        2. Meet Friends
        3. Rest
        4. Show Status
        5. Exit
        Enter your choice: 1
        ----------------------
        */


        // 1. Ask the user to enter the student name
        // 2. Display the training menu and ask user to choose an activity
        // 3. Process the chosen activity (refer to example text file)
        // 4. Repeat until the user chooses to exit(5)
        // *There is no case where the student with the given name does not exist
        // *There is no case where the user enters an invalid choice
    }

    void addStudent()
    {   
        std::string type;
        std::string name;
        std::cout<<"Enter student type (Scientist, Athlete, Artist): ";
        std::cin >> type;
        std::cout<<"Enter student name: ";
        std::cin >> name;

        if(type=="Scientist"){
            Student *freshman = new Scientist (name);
            students[studentCount] = freshman;
            studentCount+=1;
        } else if (type=="Athlete"){
            Student *freshman = new Athlete (name);
            students[studentCount] = freshman;
            studentCount+=1;
        } else if (type=="Artist"){
            Student *freshman = new Artist (name);
            students[studentCount] = freshman;
            studentCount+=1;
        } else{
            return;
        }

        // 1. Ask the user to enter the student type and name
        // 2. Create a new student object according to the given type and add it to the students array
        // *There is no case where the user enters an invalid student type or the name of an existing student
        // *There is no case where the student count exceeds MAX_STUDENTS
    }

    void contestStudents()
    {
        std::string name_1;
        std::string name_2;
        std::cout<<"Enter first student name for the contest: ";
        std::cin>>name_1;
        std::cout<<"Enter second student name for the contest: ";
        std::cin>>name_2;

        if(getStudent(name_1)->getEnergy() < 15){
            std::cout<<name_1<<" is too tired to contest."<<std::endl;
            return;
        } 
        
        if(getStudent(name_2)->getEnergy() < 15){
            std::cout<<name_2<<" is too tired to contest."<<std::endl;
            return;
        }

        std::cout<<"Contesting "<<name_1<< " vs. "<<name_2<<std::endl;

        if(getStudent(name_1)->getStats()>getStudent(name_2)->getStats()){
            std::cout<<name_1<<" wins!"<<std::endl;
            getStudent(name_1)->updateAfterContest(WIN);
            getStudent(name_2)->updateAfterContest(LOSE);
            return;
        }
        if(getStudent(name_1)->getStats()==getStudent(name_2)->getStats()){
            std::cout<<"It's a tie! "<<std::endl;
            getStudent(name_1)->updateAfterContest(TIE);
            getStudent(name_2)->updateAfterContest(TIE);
            return;
        }
        if(getStudent(name_1)->getStats()<getStudent(name_2)->getStats()){
            std::cout<<name_2<<" wins!"<<std::endl;
            getStudent(name_2)->updateAfterContest(WIN);
            getStudent(name_1)->updateAfterContest(LOSE);
            return;
        }


        // 1. Ask the user to enter the names of two students
        // 2. If the energy of any student is less than 15, display a message "${StudentName} is too tired to contest."
        // 3. Contest the two students
        //  - The student with higher stats wins
        //  - If the stats are equal, it's a tie
        //  - Display the result
        //    - If it is not a tie, display "${StudentName} wins!"
        //    - If it is a tie, display "It's a tie!"
        // 4. Update the stats of the students according to the result
        // *There is no case where the user enters the name of a non-existing student or the same name for both student
    }

    void showStatus()
    {
        for(int i=0; i<studentCount;i++){
            students[i]->showStatus();
        }
        // Display the status of all students
    }

    void run()
    {
        int choice;

        while(1){

        std::cout<<"--- Main Menu ---"<<std::endl;
        std::cout<<"1. Add Student"<<std::endl;
        std::cout<<"2. Train Student"<<std::endl;
        std::cout<<"3. Contest Students"<<std::endl;
        std::cout<<"4. Students Status"<<std::endl;
        std::cout<<"5. Exit"<<std::endl;
        std::cout<<"Enter your choice: ";
        std::cin>>choice;
        std::cout<<"----------------------"<<std::endl;
        
        switch (choice)
        {
        case 1:
            addStudent();
            break;
        case 2:
            trainStudent();
            break;
        case 3:
            contestStudents();
            break;
        case 4:
            showStatus();
            break;
        case 5:
            return;
            break;
        default:
            exit;
            break;
        }
        }

        /*
        --- Main Menu ---
        1. Add Student
        2. Train Student
        3. Contest Students
        4. Students Status
        5. Exit
        Enter your choice:
        ----------------------
        */
        // 1. Display the main menu and ask user to choose an activity
        // 2. Process the chosen activity (refer to example text file)
        // 3. Repeat until the user chooses to exit(5)
    }
};

int main()
{
    // Good Luck! :)
    Game game;
    game.run();
    return 0;
}