# 'Nonpathogenic_EscherichiacoliATCC25922.fna'
# 'Pathogenic_EscherichiacoliO104H4.fna'
import sys
import re
from collections import Counter

k=int(sys.argv[1])
filename = sys.argv[2]
f=open(filename,'r')
g=open("202319674",'w')

chromosomeslist=[]

file_read=f.read()
chromosomeslist=file_read.split('>')
kmercntlist=[]



for chromosome in chromosomeslist[1:]:
    chromosome = re.sub("[^ATCG]","",chromosome)
    
    kmercnt={}

    for i in range (0,len(chromosome)-k+1):
        kmer=chromosome[i:i+k]
        if kmer not in kmercnt:
            kmercnt[kmer]=1
        else:
            kmercnt[kmer]+=1
    kmercntlist.append(kmercnt)

kmercntcounter=Counter

for i in range (len(kmercntlist)):
    kmercntcounter+=Counter(kmercntlist[i])

kmercnt=dict(kmercntcounter)
    
for kmer in sorted(kmercnt):
    g.write("%s,%d\n" %(kmer, kmercnt[kmer]))
f.close()
# print('end')



