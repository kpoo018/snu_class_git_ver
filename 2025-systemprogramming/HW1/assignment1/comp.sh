#!/bin/bash
gcc800 -E ./src/decomment.c > ./src/decomment.i
gcc800 -S ./src/decomment.i > ./src/decomment.s
gcc800 -c ./src/decomment.s > ./src/decomment.o
gcc800 ./src/decomment.c -o ./src/decomment