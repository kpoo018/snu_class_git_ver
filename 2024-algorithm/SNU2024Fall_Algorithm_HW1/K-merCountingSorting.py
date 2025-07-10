# 'Nonpathogenic_EscherichiacoliATCC25922.fna'
# 'Pathogenic_EscherichiacoliO104H4.fna'
import sys
import re

k=int(sys.argv[1])
filename = sys.argv[2]

chromosomeslist=[]
kmercntlist=[]

with open(filename, 'r') as f:
    file_read = f.read()

chromosomeslist=file_read.split('>')[1:]

for chromosome in chromosomeslist:
    chromosome = chromosome.split("\n",maxsplit=1)[1]
    chromosome = re.sub("[^ATCG]","",chromosome)
    
    kmercnt={}

    for i in range (0,len(chromosome)-k+1):
        kmer=chromosome[i:i+k]
        if kmer not in kmercnt:
            kmercnt[kmer]=1
        else:
            kmercnt[kmer]+=1
    kmercntlist.append(kmercnt)

kmercnt=kmercntlist[0]

for i in range (1,len(kmercntlist)):
    for kmer in kmercntlist[i]:
        if kmer in kmercnt:
            kmercnt[kmer]+=kmercntlist[i][kmer]
        else:
            kmercnt[kmer]=kmercntlist[i][kmer]

with open("202319674",'w') as g:
    first_line = True
    n=0
    kmerlist=[]
    prevcnt=0
    for kmer in sorted(kmercnt,key=lambda i:-kmercnt[i]):
        if prevcnt != kmercnt[kmer]:
            for pkmer in sorted(kmerlist):
                if not first_line:
                    g.write("\n")
                g.write(f"{pkmer},{kmercnt[pkmer]}")
                first_line = False 
                n+=1  
            kmerlist=[]
        kmerlist.append(kmer)
        prevcnt=kmercnt[kmer]
        if n>=100:break
