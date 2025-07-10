#!/bin/bash

# 입력 인자 확인
if [ $# -ne 3 ]; then
    echo "Usage: $0 <k> <genome1.fasta> <genome2.fasta>"
    exit 1
fi

# 입력 인자 저장
k=$1
genome1=$2
genome2=$3

# Python 스크립트 실행
python3 2023-19674.py $k $genome1 $genome2
