// backend.c
// do not modify this code!!!

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "snake.h"

#ifdef _WIN32
#include <winsock2.h>
#include <windows.h>
#include <ws2tcpip.h> 
#pragma comment(lib, "ws2_32.lib") 
#else
#include <unistd.h>
#include <netinet/in.h>
#include <pthread.h>
#endif
#include <json.h>


#define PORT 8080 // port number, 8080 is a common port number for backend
#define BUFFER_SIZE 1024
//#define SPEED 1.2 // higher is faster


// Cross-Origin Resource Sharing (CORS) is a mechanism that uses additional HTTP headers to tell
// browsers to give a web application running at one origin, access to selected resources from a different origin.
const char* optionResponse = "HTTP/1.1 204 No Content\n" // 204 is no content, options request, options request is preflight request, it is used to check if the server allows the request
                "Access-Control-Allow-Origin: *\n" // cors allowance // * is all
                "Access-Control-Allow-Methods: POST, GET, OPTIONS\n" // post, get, options are allowed
                "Access-Control-Allow-Headers: Content-Type\n" 
                "Access-Control-Max-Age: 86400\n"
                "Content-Length: 0\n"
                "Connection: keep-alive\n\n";

const char *postResponse = "HTTP/1.1 200 OK\n" // 200 is ok
                "Content-Type: application/json\n"
                "Access-Control-Allow-Origin: *\n\n" // cors allowance
                "{ \"status\": \"success\" }";
#ifdef _WIN32
DWORD WINAPI gameLoop(LPVOID lpParam) {
#else
void* gameLoop(void* arg) { // gameloop is a function that runs in a separate thread
#endif
    while (1) {
        updateGame();       // as snake moves continuously, updateGame() is called continuously every xx seconds by sleep() function to update the game
#ifdef WIN32
        Sleep(1000 / (SPEED )); // milliseconds
#else
        //sleep(1); 
        usleep(1000000 / (SPEED) ); // micro seconds, // sleep is a blocking(waiting) function, it means that it will wait until the time is up
#endif
    }
}
#ifdef _WIN32
void sendGameData(SOCKET socket) { // socket communication
#else
void sendGameData(int socket) {
#endif
    json_object *jobj = json_object_new_object();    // json description
    json_object *jgrid = json_object_new_array();    // json is a common file format, it is a string
                                                     // json is a common string format for data exchange, it is composed of key-value pairs
    for (int i = 0; i < GRID_HEIGHT; i++) {          // json object is a key-value pair, key is a string, value can be a string, number, boolean, array, object, null
        json_object *jrow = json_object_new_array(); // sample json string: {"name":"John", "age":30, "car":null}
        for (int j = 0; j < GRID_WIDTH; j++) {       // each value is separated by a comma and accessed by its key // end json description
            json_object_array_add(jrow, json_object_new_string_len(&grid[i][j], 1));
        }
        json_object_array_add(jgrid, jrow);
    }

    json_object_object_add(jobj, "grid", jgrid); // add grid to json object
    json_object_object_add(jobj, "snakeLength", json_object_new_int(snakeLength)); // add snakeLength to json object
    json_object_object_add(jobj, "gameOver", json_object_new_boolean(gameOver)); // add gameOver to json object


    const char *response = json_object_to_json_string(jobj); // convert json object to string
    char header[BUFFER_SIZE];
    sprintf(header,  // http header
        "HTTP/1.1 200 OK\n"
        "Content-Type: application/json\n"
        "Access-Control-Allow-Origin: *\n"
        "Content-Length: %ld\n\n", strlen(response));
    
    send(socket, header, strlen(header), 0); // send header
    send(socket, response, strlen(response), 0); // send body=game data

    json_object_put(jobj); // Clean up
}
#ifdef _WIN32
void handleRequest(SOCKET socket, char* buffer) {
#else
void handleRequest(int socket, char* buffer) { // socket communication, handle http request
#endif
    if (strstr(buffer, "OPTIONS /set-direction")) { // options request
        send(socket, optionResponse, strlen(optionResponse), 0);
    } 
    else if (strstr(buffer, "GET /game-data")) { // get request
        sendGameData(socket); // send game data
        //printf("Sent game data\n");
    }
    else if (strstr(buffer, "GET /health-check")) { // check if the server is running
        const char *healthCheckResponse = "HTTP/1.1 200 OK\n"
                                        "Content-Type: text/plain\n"
                                        "Access-Control-Allow-Origin: *\n\n"
                                        "Backend Running";
        send(socket, healthCheckResponse, strlen(healthCheckResponse), 0);
    }
    else if (strstr(buffer, "GET /leaderboard")) { // get request, get leaderboard data
        LeaderboardEntry *leaderboard = getLeaderboardData();
        int leaderboardSize = getLeaderboardSize();

        json_object *jarray = json_object_new_array();
        for (int i = 0; i < leaderboardSize; i++) {
            json_object *jentry = json_object_new_object();
            json_object_object_add(jentry, "name", json_object_new_string(leaderboard[i].name));
            json_object_object_add(jentry, "score", json_object_new_int(leaderboard[i].score));
            json_object_array_add(jarray, jentry);
        }

        const char *response = json_object_to_json_string(jarray);
        char header[BUFFER_SIZE];
        sprintf(header, 
            "HTTP/1.1 200 OK\n"
            "Content-Type: application/json\n"
            "Access-Control-Allow-Origin: *\n"
            "Content-Length: %ld\n\n", strlen(response));

        send(socket, header, strlen(header), 0);
        send(socket, response, strlen(response), 0);

        json_object_put(jarray); // Clean up
    }
    else if (strstr(buffer, "OPTIONS /new-game")) {
        send(socket, optionResponse, strlen(optionResponse), 0);
    } 
    else if (strstr(buffer, "POST /new-game")) { // post request
        initializeGame(); // Reset the game
        send(socket, postResponse, strlen(postResponse), 0); // Send response back
    }
    else if (strstr(buffer, "OPTIONS /game-over")) {
        send(socket, optionResponse, strlen(optionResponse), 0);
    } 
    else if (strstr(buffer, "POST /game-over")) {
        handleGameOver(); // Call the game over function
        send(socket, postResponse, strlen(postResponse), 0); // Send back a success response
    }
    else if (strstr(buffer, "OPTIONS /submit-score")) {
        send(socket, optionResponse, strlen(optionResponse), 0);
    } 
    else if (strstr(buffer, "POST /submit-score")) {
        char* body = strstr(buffer, "\r\n\r\n"); // Find the start of the body
        if (body) {
            body += 4; // Skip the "\r\n\r\n"
            json_object *jobj = json_tokener_parse(body); // Parse JSON body
            json_object *jname, *jscore;
            json_object_object_get_ex(jobj, "name", &jname);
            json_object_object_get_ex(jobj, "score", &jscore);

            const char *name = json_object_get_string(jname);
            int score = json_object_get_int(jscore);

            // Now we have the name and score
            appendToLeaderboard(name, score);

            json_object_put(jobj); // Free JSON object
        }

        // Send back a response
        const char *response = "HTTP/1.1 200 OK\n"
                            "Content-Type: text/plain\n"
                            "Access-Control-Allow-Origin: *\n\n"
                            "Score Submitted Successfully";
        send(socket, response, strlen(response), 0);
    }

    else if (strstr(buffer, "POST /set-direction")) { // post request from client
        char* content_length_start = strstr(buffer, "Content-Length: ");
        if (content_length_start) {
            const int content_length = atoi(content_length_start + 16); // 16 is the length of "Content-Length: "
            char* body = strstr(buffer, "\r\n\r\n"); // buffer is the http request, body is the json string
            if (body) {                              // \r\n\r\n is the end of the http header
                body += 4; // +4 means skip "\r\n\r\n", now body pointer is pointing to the data (json string)
                int already_read_length = strlen(body); 
                char content[content_length + 1]; 

                if (already_read_length > 0) {
                    // chrome
                    memcpy(content, body, already_read_length);
                    content[already_read_length] = '\0';
                }

                if (already_read_length < content_length) {
                    // safari
                    int remaining_length = content_length - already_read_length;
                    #ifdef _WIN32   
                    int bytes_read = recv(socket, content + already_read_length, remaining_length, 0);
                    #else
                    int bytes_read = read(socket, content + already_read_length, remaining_length);
                    #endif
                    if (bytes_read < 0) {
                        perror("Failed to read from socket");
                        return;
                    }
                    content[already_read_length + bytes_read] = '\0';
                }
                json_object *jobj = json_tokener_parse(content); // content is Content: {"direction":"UP"} it is a json string
                json_object *jdirection;
                json_object_object_get_ex(jobj, "direction", &jdirection);
                const char *direction = json_object_get_string(jdirection);
                setDirection(direction);
                //printf("Final content: %s\n", content); for debugging
            } else {
                printf("Body not found in the request\n");
            }
        } 
        else {
            printf("Content-Length not found in the request\n");
        }
        send(socket, postResponse, strlen(postResponse), 0);
        fflush(NULL); 
    }
    else if (strstr(buffer, "GET /student-info")) {
        json_object *jobj = json_object_new_object();
        json_object_object_add(jobj, "name", json_object_new_string(student.name));
        json_object_object_add(jobj, "id", json_object_new_string(student.id));

        const char *response = json_object_to_json_string(jobj);
        char header[BUFFER_SIZE];
        sprintf(header, 
            "HTTP/1.1 200 OK\n"
            "Content-Type: application/json\n"
            "Access-Control-Allow-Origin: *\n"
            "Content-Length: %ld\n\n", strlen(response));
    
        send(socket, header, strlen(header), 0);
        send(socket, response, strlen(response), 0);

        json_object_put(jobj); // Clean up
    }
    
}

int main() {
#ifdef _WIN32
    WSADATA wsaData;
    SOCKET server_fd, new_socket;
    if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0) {
        perror("WSAStartup failed");
        exit(EXIT_FAILURE);
    }
#else
    int server_fd, new_socket;
#endif
    struct sockaddr_in address;
    int addrlen = sizeof(address);

#ifdef _WIN32
    if ((server_fd = socket(AF_INET, SOCK_STREAM, 0)) == INVALID_SOCKET) {
        perror("Socket failed");
        exit(EXIT_FAILURE);
    }
#else
    if ((server_fd = socket(AF_INET, SOCK_STREAM, 0)) == 0) { // AF_INET is IPv4, SOCK_STREAM is TCP, 0 is IP
        perror("Socket failed");                              // socket is a stateful connection. It means that there is a connection between the client and the server and the connection is maintained until the client or the server closes it
        exit(EXIT_FAILURE);                                   // socket communication is a communication between two processes can be on the same computer or on different computers
    }                                                         // socket is bidirectional, it means that the client can send data to the server and the server can send data to the client
#endif

    address.sin_family = AF_INET; // AF_INET is IPv4
    address.sin_addr.s_addr = INADDR_ANY; // INADDR_ANY is all local interfaces
    address.sin_port = htons(PORT); // htons is host to network short, it converts the port number to network byte order

    if (bind(server_fd, (struct sockaddr *)&address, sizeof(address)) < 0) { // bind the socket to the port
        perror("Bind failed");
        exit(EXIT_FAILURE);
    }

    if (listen(server_fd, 10) < 0) { // listen for connections, 10 is the maximum number of connections
        perror("Listen failed");
        exit(EXIT_FAILURE);
    }

    initializeGame();

#ifdef _WIN32
    HANDLE game_thread;
    DWORD game_thread_id;
    if ((game_thread = CreateThread(NULL, 0, gameLoop, NULL, 0, &game_thread_id)) == NULL) {
        perror("Failed to create game thread");
        return EXIT_FAILURE;
    }
#else
    pthread_t game_thread; // thread is a sequence of instructions that can run concurrently with other threads
    if(pthread_create(&game_thread, NULL, gameLoop, NULL) != 0) {  // thread is a sequence of instructions that can run concurrently with other threads
        perror("Failed to create game thread");                    // gameLoop is the function pointer, it is the function that the thread will run
        return EXIT_FAILURE;
    }
#endif

    while (1) { // infinite loop, wait for connections, accept connections, handle connections
        if ((new_socket = accept(server_fd, (struct sockaddr *)&address, (socklen_t*)&addrlen)) < 0) {
            perror("Accept failed");
            exit(EXIT_FAILURE);
        }

        char buffer[BUFFER_SIZE] = {0};
#ifdef _WIN32
        recv(new_socket, buffer, BUFFER_SIZE, 0);
#else
        read(new_socket, buffer, BUFFER_SIZE); // read is a blocking function, it means that it will wait until it receives data
#endif
        handleRequest(new_socket, buffer); // handle http request
#ifdef _WIN32
        closesocket(new_socket);
#else
        close(new_socket); // close the socket
#endif
    }

#ifdef _WIN32
    closesocket(server_fd);
    WSACleanup();
#else
    close(server_fd);
#endif
    return 0;
}