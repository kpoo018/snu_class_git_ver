#!/bin/bash

# Loop through test files from test0.c to test5.c
for i in {0..6}
do
  # Run the reference decomment program and save output and error to files
  ./reference/sampledecomment < ./test_files/test${i}.c > ./output/reference_output${i}.txt 2> ./output/reference_error${i}.txt
  ./reference/sampledecomment < ./src/decomment.c > ./output/reference_output_decomment.txt 2> ./output/reference_error_decomment.txt

  # Run the student's decomment program and save output and error to files
  ./src/decomment < ./test_files/test${i}.c > ./output/student_output${i}.txt 2> ./output/student_error${i}.txt
  ./src/decomment < ./src/decomment.c > ./output/student_output_decomment.txt 2> ./output/student_error_decomment.txt

  # Compare the outputs
  diff ./output/reference_output${i}.txt ./output/student_output${i}.txt > ./output/diff_output${i}.txt
  diff ./output/reference_error${i}.txt ./output/student_error${i}.txt > ./output/diff_error${i}.txt
  diff ./output/reference_output_decomment.txt ./output/student_output_decomment.txt > ./output/diff_output_decomment.txt
  diff ./output/reference_error_decomment.txt ./output/student_error_decomment.txt > ./output/diff_error_decomment.txt
done