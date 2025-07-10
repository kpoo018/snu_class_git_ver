import sys
import re
from collections import defaultdict

def count_kmers(fasta_file, k):
    kmers = defaultdict(int)
    with open(fasta_file, 'r') as f:
        sequence = ''.join(line.strip() for line in f if not line.startswith('>'))
    for i in range(len(sequence) - k + 1):
        kmer = sequence[i:i+k]
        kmers[kmer] += 1
    return kmers

def filter_kmers(kmers, genome_size, k, alpha=5):
    threshold = alpha * (genome_size / (4**k))
    filtered_kmers = [kmer for kmer, count in kmers.items() if count >= threshold]
    return filtered_kmers

def find_kmer_positions(fasta_file, kmers):
    positions = defaultdict(list)
    with open(fasta_file, 'r') as f:
        for chunk in read_in_chunks(f):
            for kmer in kmers:
                for match in re.finditer(f'(?={re.escape(kmer)})', chunk):
                    positions[kmer].append(match.start())
    return positions

def read_in_chunks(file_object, chunk_size=1024*1024):
    while True:
        data = file_object.read(chunk_size)
        if not data:
            break
        yield data

def lcs(seq1, seq2):
    m, n = len(seq1), len(seq2)
    dp = [[0] * (n + 1) for _ in range(m + 1)]
    for i in range(1, m + 1):
        for j in range(1, n + 1):
            if seq1[i-1] == seq2[j-1]:
                dp[i][j] = dp[i-1][j-1] + 1
            else:
                dp[i][j] = max(dp[i-1][j], dp[i][j-1])
    return dp

def save_results(lcs_seq, positions1, positions2, student_id, k, genome1, genome2):
    # LCS 정보 저장
    with open(f"{student_id}_{k}_{genome1}_{genome2}_LCS.txt", "w") as f:
        f.write("-".join(lcs_seq))

    # genome1의 LCS 위치 정보 저장
    with open(f"{student_id}_{k}_{genome1}_LCS_positions.csv", "w") as f:
        f.write("LCS k-mer,Position\n")
        for kmer, pos in zip(lcs_seq, positions1):
            f.write(f"{kmer},{pos}\n")

    # genome2의 LCS 위치 정보 저장
    with open(f"{student_id}_{k}_{genome2}_LCS_positions.csv", "w") as f:
        f.write("LCS k-mer,Position\n")
        for kmer, pos in zip(lcs_seq, positions2):
            f.write(f"{kmer},{pos}\n")

def backtrack_lcs(dp, seq1, seq2):
    i, j = len(seq1), len(seq2)
    lcs = []
    while i > 0 and j > 0:
        if seq1[i-1] == seq2[j-1]:
            lcs.append(seq1[i-1])
            i -= 1
            j -= 1
        elif dp[i-1][j] > dp[i][j-1]:
            i -= 1
        else:
            j -= 1
    return lcs[::-1]

def get_genome_size(fasta_file):
    size = 0
    with open(fasta_file, 'r') as f:
        for line in f:
            if not line.startswith('>'):
                size += len(line.strip())
    return size

def main():
    # 1. 명령행 인자 처리
    k = int(sys.argv[1])
    genome1_file = sys.argv[2]
    genome2_file = sys.argv[3]

    # 2. k-mer 계산
    kmers1 = count_kmers(genome1_file, k)
    kmers2 = count_kmers(genome2_file, k)

    # 3. k-mer 필터링
    genome1_size = get_genome_size(genome1_file)
    genome2_size = get_genome_size(genome2_file)
    filtered_kmers1 = filter_kmers(kmers1, genome1_size, k)
    filtered_kmers2 = filter_kmers(kmers2, genome2_size, k)

    # 4. k-mer 위치 검색
    positions1 = find_kmer_positions(genome1_file, filtered_kmers1)
    positions2 = find_kmer_positions(genome2_file, filtered_kmers2)

    # 5. LCS 계산
    seq1 = [kmer for kmer in filtered_kmers1 if kmer in filtered_kmers2]
    seq2 = [kmer for kmer in filtered_kmers2 if kmer in filtered_kmers1]
    lcs_matrix = lcs(seq1, seq2)

    # 6. LCS 추출
    lcs_seq = backtrack_lcs(lcs_matrix, seq1, seq2)

    # 7. 결과 저장
    student_id = "2024-00001"  # 본인의 학번으로 변경
    save_results(lcs_seq, positions1, positions2, student_id, k, genome1_file, genome2_file)

if __name__ == "__main__":
    main()
