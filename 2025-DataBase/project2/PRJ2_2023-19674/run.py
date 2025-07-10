import mysql.connector
from mysql.connector import errorcode
from mysql.connector import connect
import pandas as pd
import numpy as np

# 전역 connection 및 cursor 객체
conn= None
cursor = None

def ensure_connection():
    global conn, cursor
    # 연결이 없거나 끊어진 경우 재연결
    if conn is None or not conn.is_connected():
            conn = connect(
                host="astronaut.snu.ac.kr",
                port=7001,
                user='DB2023_19674',
                password='MyDBPassword19674!',
                database='DB2023_19674',
                charset='utf8') 
            cursor = conn.cursor()

def close_connection():
    global conn, cursor
    if cursor:
        cursor.close()
    if conn and conn.is_connected():
        conn.close()

def initialize_database():

    ensure_connection()

    # 외래 키 제약 해제 후 기존 모든 테이블 삭제
    cursor.execute("SET FOREIGN_KEY_CHECKS = 0")
    cursor.execute("SHOW TABLES")
    for (table_name,) in cursor.fetchall():
        cursor.execute(f"DROP TABLE IF EXISTS `{table_name}`")
    cursor.execute("SET FOREIGN_KEY_CHECKS = 1")

    # 테이블 생성 로직
    # 테이블 생성
    cursor.execute("""
        CREATE TABLE `User` (
            u_id INT AUTO_INCREMENT PRIMARY KEY,
            u_name VARCHAR(30) NOT NULL CHECK (CHAR_LENGTH(u_name) BETWEEN 1 AND 30),
            u_age INT CHECK (u_age > 0),
            total_borrowed INT DEFAULT 0,
            borrow_count INT DEFAULT 0 CHECK (borrow_count BETWEEN 0 AND 2),
            late_score INT DEFAULT 0,
            penalty_left INT DEFAULT 0
        )
    """)
    cursor.execute("""
        CREATE TABLE `DVD` (
            d_id INT AUTO_INCREMENT PRIMARY KEY,
            d_title VARCHAR(50) NOT NULL CHECK (CHAR_LENGTH(d_title) BETWEEN 1 AND 50),
            d_name VARCHAR(32) NOT NULL CHECK (CHAR_LENGTH(d_name) BETWEEN 1 AND 32),
            age_limit INT CHECK (age_limit BETWEEN 0 AND 19),
            total_borrowed INT DEFAULT 0,
            remain_num INT DEFAULT 2,
            total_num INT DEFAULT 2,
            UNIQUE (d_title, d_name)
        )
    """)
    cursor.execute("""
        CREATE TABLE `Rating` (
            d_id INT,
            u_id INT,
            rating INT CHECK (rating BETWEEN 1 AND 5),
            PRIMARY KEY (d_id, u_id),
            FOREIGN KEY (d_id) REFERENCES DVD(d_id),
            FOREIGN KEY (u_id) REFERENCES User(u_id)
        )
    """)
    cursor.execute("""
        CREATE TABLE `Loan` (
            u_id INT,
            d_id INT,
            PRIMARY KEY (u_id, d_id),
            FOREIGN KEY (u_id) REFERENCES `User`(u_id),
            FOREIGN KEY (d_id) REFERENCES `DVD`(d_id)
        )
    """)

    # CSV 로딩 및 삽입
    df = pd.read_csv("data.csv")
    for _, row in df.iterrows():
        d_id = int(row["d_id"])
        d_title = row["d_title"]
        d_name = row["d_name"]
        age_limit = int(row["age_limit"])
        u_id = int(row["u_id"])
        u_name = row["u_name"]
        u_age = int(row["u_age"])
        rating = int(row["rating"])

        cursor.execute("""
            INSERT INTO DVD (d_id, d_title, d_name, age_limit, total_borrowed)
            VALUES (%s, %s, %s, %s, %s)
            ON DUPLICATE KEY UPDATE total_borrowed = total_borrowed+1
        """, (d_id, d_title, d_name, age_limit, 1))

        cursor.execute("""
            INSERT INTO User (u_id, u_name, u_age, total_borrowed)
            VALUES (%s, %s, %s, %s)
            ON DUPLICATE KEY UPDATE total_borrowed = total_borrowed+1
        """, (u_id, u_name, u_age, 1))

        cursor.execute("""
            INSERT INTO Rating (d_id, u_id, rating)
            VALUES (%s, %s, %s)
            ON DUPLICATE KEY UPDATE rating = VALUES(rating)
        """, (d_id, u_id, rating))



    conn.commit()
    
    print('Database successfully initialized')
    return

def reset():
    # YOUR CODE GOES HERE
    answer=input("All tables and data will be reset. Continue? (y/n): ")
    if answer.lower() == 'y':
        initialize_database()
    return

def print_DVDs():
    ensure_connection()
    cursor.execute("SELECT * FROM DVD")
    rows = cursor.fetchall()
    rows = sorted(rows, key=lambda x: x[0])  # d_id 기준으로 
    
    print('--------------------------------------------------------------------------------')
    print('id title director age_limit avg.rating cumul_rent_cnt stock')
    print('--------------------------------------------------------------------------------')
    for row in rows:
        d_id, d_title, d_name, age_limit, total_borrowed, remain_num, total_num = row
        cursor.execute(
            "SELECT AVG(rating) FROM Rating WHERE d_id = %s",
            (d_id,)
        )
        avg_rating = cursor.fetchone()[0]
        avg_rating = round(avg_rating, 2) if avg_rating is not None else None
        
        print(f"{d_id} {d_title} {d_name} {age_limit} {avg_rating} {total_borrowed} {remain_num}")
    

    print('--------------------------------------------------------------------------------')
    conn.commit()
    return

def print_users():
    ensure_connection()
    cursor.execute("SELECT * FROM User")
    rows = cursor.fetchall()
    rows = sorted(rows, key=lambda x: x[0])  # u_id 기준으로 
    
    print('--------------------------------------------------------------------------------')
    print('id name age avg.rating cumul_rent_cnt')
    print('--------------------------------------------------------------------------------')
    for row in rows:
        u_id, u_name, u_age, total_borrowed, borrow_count, late_score, penalty_left = row
        cursor.execute(
            "SELECT AVG(rating) FROM Rating WHERE u_id = %s",
            (u_id,)
        )
        avg_rating = cursor.fetchone()[0]
        avg_rating = round(avg_rating, 2) if avg_rating is not None else None
        
        print(f"{u_id} {u_name} {u_age} {avg_rating} {total_borrowed}")
    

    print('--------------------------------------------------------------------------------')
    conn.commit()
    return

def insert_DVD():
    title = input('DVD title: ')
    if len(title) > 50:
        print("Title length should range from 1 to 50 characters")
        return
    director = input('DVD director: ')
    if len(director) > 30:
        print("Director length should range from 1 to 30 characters")
        return
    age_limit = input('Age limit: ')
    if not age_limit.isdigit:
        print("Age limit should be an integer from 0 to 19")
        return
    age_limit = int(age_limit)
    if age_limit < 0 or age_limit > 19:
        print("Age limit should be an integer from 0 to 19")
        return

    ensure_connection()

    try:
        cursor.execute(
            "INSERT INTO DVD (d_title, d_name, age_limit) VALUES (%s, %s, %s)",
            (title, director, age_limit)
        )
        conn.commit()

        print('DVD successfully inserted')

    except mysql.connector.IntegrityError as err:
        conn.rollback()  # 트랜잭션 롤백
        # 중복 키(UNIQUE 또는 PRIMARY) 에러 처리
        if err.errno == errorcode.ER_DUP_ENTRY:
            print(f'DVD ({title}, {director}) already exists')
    
    return

def remove_DVD():
    ensure_connection()

    DVD_id = input('DVD ID: ')
    
    cursor.execute(
        "SELECT remain_num, total_num FROM DVD WHERE d_id = %s",
        (DVD_id,)
    )
    row = cursor.fetchone()

    # DVD가 존재하는지 확인
    if row is None:
        print(f'DVD {DVD_id} does not exist')
        return

    # remain_num과 total_num을 비교하여 DVD가 대여 중인지 확인
    remain_num, total_num = row
    if remain_num != total_num:
        print('Cannot delete a DVD that is currently borrowed')
        return
        
    # Rating 테이블의 관련 리뷰 삭제
    cursor.execute(
        "DELETE FROM Rating WHERE d_id = %s",
        (DVD_id,)
    )
    # DVD 테이블에서 삭제
    cursor.execute(
        "DELETE FROM DVD WHERE d_id = %s",
        (DVD_id,)
    )
    
    conn.commit()
    print('DVD successfully removed')
       
    return

def insert_user():
    name = input('User name: ')
    if len(name) > 30:
        print("Username length should range from 1 to 30 characters")
        return
    age = input('User age: ')
    if not age.isdigit():
        print("Age should be a positive integer")
        return
    age = int(age)
    if age <= 0:
        print("Age should be a positive integer")
        return
    ensure_connection()
    
    cursor.execute(
        "INSERT INTO User (u_name, u_age) VALUES (%s, %s)",
        (name, age)
    )
    conn.commit()

    print('One user successfully inserted')
   
    return

def remove_user():
    ensure_connection()

    user_id = input('User ID: ')
    
    cursor.execute(
        "SELECT borrow_count FROM User WHERE u_id = %s",
        (user_id,)
    )
    borrow_count = cursor.fetchone()

    # User가 존재하는지 확인
    if borrow_count is None:
        print(f'User {user_id} does not exist')
        return

    # User가 대여 중인지 확인
    borrow_count = borrow_count[0]
    if borrow_count > 0:
        print('Cannot delete a user with borrowed DVDs')
        return
        
    # Rating 테이블의 관련 리뷰 삭제
    cursor.execute(
        "DELETE FROM Rating WHERE u_id = %s",
        (user_id,)
    )
    # DVD 테이블에서 삭제
    cursor.execute(
        "DELETE FROM User WHERE u_id = %s",
        (user_id,)
    )
    
    conn.commit()
    print('One user successfully removed')
       
    return

def checkout_DVD():
    ensure_connection()

    DVD_id = input('DVD ID: ')

    cursor.execute(
        "SELECT age_limit, remain_num FROM DVD WHERE d_id = %s",
        (DVD_id,)
    )
    row = cursor.fetchone()

    # DVD가 존재하는지 확인
    if row is None:
        print(f'DVD {DVD_id} does not exist')
        return
        
    age_limit, remain_num = row


    user_id = input('User ID: ')

    cursor.execute(
        "SELECT u_age, borrow_count, late_score, penalty_left FROM User WHERE u_id = %s",
        (user_id,)
    )
    row = cursor.fetchone()

    # User가 존재하는지 확인
    if row is None:
        print(f'User {user_id} does not exist')
        return
    
    u_age, borrow_count, late_score, penalty_left = row

    # User가 최대 개수의 DVD를 대여했는지 확인
    if borrow_count >= 2:
        print(f'User {user_id} exceeded the maximum borrowing limit')
        return
    
    # User의 나이가 DVD의 age_limit보다 적은지 확인
    if u_age < age_limit:
        print(f'User {user_id} does not meet the age limit for this DVD')
        return
    
    # DVD의 재고가 없는지 확인
    if remain_num == 0:
        cursor.execute(
            "SELECT u_id FROM Rating WHERE d_id = %s",
            (DVD_id,)
        )
        users = cursor.fetchall()
        
        for u_id in users:
            cursor.execute(
                "UPDATE User SET late_score = late_score + 1 WHERE u_id = %s" ,
                (u_id[0],)
            )
            cursor.execute(
                "UPDATE User SET penalty_left = 2 WHERE u_id = %s AND late_score = 5",
                (u_id[0],)
            )

        conn.commit()
        print('Cannot check out a DVD that is out of stock')    
        return

    # DVD 대출 제한 상태인지 확인
    if penalty_left > 0:
        penalty_left -= 1
        cursor.execute(
            "UPDATE User SET penalty_left = penalty_left - 1 WHERE u_id = %s",
            (user_id,)
        )
        print(f'User {user_id} is currently restricted from borrowing DVDs ({penalty_left} attempts left)')
        conn.commit()
        return
    
    # DVD 대출 가능 상태
    if late_score >= 5:
        cursor.execute(
            "UPDATE User SET penalty_left = 0 WHERE u_id = %s",
            (user_id,)
        )
        
    cursor.execute(
        "UPDATE DVD SET remain_num = remain_num - 1, total_borrowed = total_borrowed + 1 WHERE d_id = %s",
        (DVD_id,)
    )
    cursor.execute(
        "UPDATE User SET borrow_count = borrow_count + 1, total_borrowed = total_borrowed + 1 WHERE u_id = %s",
        (user_id,)
    )
    cursor.execute(
        "INSERT INTO Loan (u_id, d_id) VALUES (%s, %s)",
        (user_id, DVD_id)
    )
    conn.commit()

    print('DVD successfully checked out')
    return
    

def return_and_rate_DVD():
    ensure_connection()
    DVD_id = input('DVD ID: ')
    cursor.execute(
        "SELECT * FROM DVD WHERE d_id = %s",
        (DVD_id,)
    )
    row = cursor.fetchone()
    # DVD가 존재하는지 확인
    if row is None:
        print(f'DVD {DVD_id} does not exist')
        return
    
    user_id = input('User ID: ')
    cursor.execute(
        "SELECT * FROM User WHERE u_id = %s",
        (user_id,)
    )
    row = cursor.fetchone()
    # User가 존재하는지 확인
    if row is None:
        print(f'User {user_id} does not exist')
        return
    
    rating = input('Ratings (1~5): ')
    # 평점이 1~5 사이의 정수인지 확인
    if not rating.isdigit():
        print("Rating should be an integer from 1 to 5")
        return
    rating = int(rating)
    if rating < 1 or rating > 5:
        print("Rating should be an integer from 1 to 5")
        return


    cursor.execute(
        "SELECT * FROM Loan WHERE d_id = %s AND u_id = %s",
        (DVD_id, user_id)
    )
    row = cursor.fetchall()
    # 대출하고 있는지 확인
    if not row:
        print("Cannot return and rate a DVD that is not currently borrowed for this user")
        return
    
    # DVD 반납
    cursor.execute(
        "UPDATE DVD SET remain_num = remain_num + 1 WHERE d_id = %s",
        (DVD_id,)
    )
    cursor.execute(
        "UPDATE User SET borrow_count = borrow_count - 1 WHERE u_id = %s",
        (user_id,)
    )
    cursor.execute(
        "DELETE FROM Loan WHERE d_id = %s AND u_id = %s",
        (DVD_id, user_id)
    )
    # DVD 평점 업데이트
    cursor.execute(
        "INSERT INTO Rating (d_id, u_id, rating) VALUES (%s, %s, %s) "
        "ON DUPLICATE KEY UPDATE rating = %s",
        (DVD_id, user_id, rating, rating)
    )
    conn.commit()
    print('DVD successfully returned and rated')
    return
    

def print_borrowing_status_for_user():
    ensure_connection()
    user_id = input('User ID: ')
    cursor.execute(
        "SELECT * FROM User WHERE u_id = %s",
        (user_id,)
    )
    row = cursor.fetchone()
    # User가 존재하는지 확인
    if row is None:
        print(f'User {user_id} does not exist')
        return
    
    # 현재 대출 중인 DVD 목록 출력
    print('--------------------------------------------------------------------------------')
    print('id title director age_limit avg.rating')
    print('--------------------------------------------------------------------------------')
    cursor.execute(
        "SELECT d_id FROM Loan WHERE u_id = %s",
        (user_id,)
    )
    borrowed_DVDs = cursor.fetchall()

    for dvd in sorted(borrowed_DVDs):
        d_id = dvd[0]
        cursor.execute(
            "SELECT d_title, d_name, age_limit FROM DVD WHERE d_id = %s",
            (d_id,)
        )
        d_title, d_name, age_limit = cursor.fetchone()
        cursor.execute(
            "SELECT avg(rating) FROM Rating WHERE d_id = %s",
            (d_id,)
        )
        avg_rating = cursor.fetchone()[0]
        avg_rating = round(avg_rating, 2) if avg_rating is not None else None
        print(f'{d_id} {d_title} {d_name} {age_limit} {avg_rating}')
    print('--------------------------------------------------------------------------------') 
    
    conn.commit()
    return

def search():
    ensure_connection()
    query = input('Query: ')
    cursor.execute(
        "SELECT * FROM DVD WHERE d_title LIKE %s",
        ('%' + query + '%',)
    )
    rows = cursor.fetchall()
    rows = sorted(rows, key=lambda x: x[0])  # d_id 기준으로 
    
    print('--------------------------------------------------------------------------------')
    print('id title director age_limit avg.rating stock')
    print('--------------------------------------------------------------------------------')
    for row in rows:
        d_id, d_title, d_name, age_limit, total_borrowed, remain_num, total_num = row
        cursor.execute(
            "SELECT AVG(rating) FROM Rating WHERE d_id = %s",
            (d_id,)
        )
        avg_rating = cursor.fetchone()[0]
        avg_rating = round(avg_rating, 2) if avg_rating is not None else None
        
        print(f"{d_id} {d_title} {d_name} {age_limit} {avg_rating} {remain_num}")
    

    print('--------------------------------------------------------------------------------')
    conn.commit()
    return
    

def recommend_popularity():
    ensure_connection()

    user_id= input('User ID: ')
    
    # 사용자 존재 여부 및 나이 조회
    cursor.execute(
        "SELECT u_age FROM `User` WHERE u_id = %s",
        (user_id,)
    )
    row = cursor.fetchone()
    if not row:
        print(f'User {user_id} does not exist')
        return
    u_age = row[0]

    # 추천 대상 후보 존재 여부 확인
    cursor.execute(
        """
        SELECT COUNT(*)
        FROM `DVD` d
        WHERE d.age_limit <= %s
          AND d.d_id NOT IN (SELECT d_id FROM `Rating` WHERE u_id = %s)
        """,
        (u_age, user_id)
    )
    if cursor.fetchone()[0] == 0:
        print('No DVD can be recommended')
        return

    # 평점 기반 추천
    cursor.execute(
        """
        SELECT
          d.d_id,
          d.d_title,
          d.d_name,
          d.age_limit,
          d.remain_num,
          ROUND(AVG(r.rating), 1) AS avg_rating
        FROM `DVD` d
        LEFT JOIN `Rating` r ON d.d_id = r.d_id
        WHERE d.age_limit <= %s
          AND d.d_id NOT IN (SELECT d_id FROM `Rating` WHERE u_id = %s)
        GROUP BY d.d_id
        ORDER BY avg_rating DESC, d.d_id ASC
        LIMIT 1
        """,
        (u_age, user_id)
    )
    rating_rec = cursor.fetchone()

    # 대출 횟수 기반 추천
    cursor.execute(
        """
        SELECT
          d_id,
          d_title,
          d_name,
          age_limit,
          total_borrowed,
          remain_num
        FROM `DVD`
        WHERE age_limit <= %s
          AND d_id NOT IN (SELECT d_id FROM `Rating` WHERE u_id = %s)
        ORDER BY total_borrowed DESC, d_id ASC
        LIMIT 1
        """,
        (u_age, user_id)
    )
    pop_rec = cursor.fetchone()

    # 결과 출력
    print('--------------------------------------------------------------------------------')

    print('Rating-based')
    print('--------------------------------------------------------------------------------')
    print('id title director age_limit avg.rating stock')
    print('--------------------------------------------------------------------------------')
    d_id, d_title, d_name, age_limit, remain_num, avg_rating = rating_rec
    print(f'{d_id} {d_title} {d_name} {age_limit} {avg_rating} {remain_num}')
    print('--------------------------------------------------------------------------------')

    print('Popularity-based')
    print('--------------------------------------------------------------------------------')
    print('id title director age_limit cumul_rent_cnt stock')
    print('--------------------------------------------------------------------------------')
    d_id2, d_title2, d_name2, age_limit2, total_borrowed, remain_num2 = pop_rec
    print(f'{d_id2} {d_title2} {d_name2} {age_limit2} {total_borrowed} {remain_num2}')
    print('--------------------------------------------------------------------------------')

    conn.commit()
    return

def recommend_user_based():
    ensure_connection()

    user_id = int(input('User ID: '))

    # 회원 존재 여부 및 나이 조회
    cursor.execute("SELECT u_age FROM `User` WHERE u_id = %s", (user_id,))
    row = cursor.fetchone()
    if not row:
        print(f'User {user_id} does not exist')
        return
    u_age = row[0]

    # 추천 후보 DVD 조회 (나이 제한 만족)
    cursor.execute("SELECT d_id, d_title, d_name, age_limit FROM `DVD` WHERE age_limit <= %s", (u_age,))
    dvds = cursor.fetchall()
    if not dvds:
        print('No DVDs available for recommendation.')
        return

    # user-item 매트릭스 생성
    cursor.execute("SELECT u_id, d_id, rating FROM Rating")
    rows = cursor.fetchall()
    cols = [desc[0] for desc in cursor.description]
    ratings_df = pd.DataFrame(rows, columns=cols)

    if ratings_df.empty:
        # 모든 임시 평점 0 처리
        user_means = {}
    else:
        # 유저별 평균 평점
        user_means = ratings_df.groupby('u_id')['rating'].mean().to_dict()

    # User-Item 매트릭스 초기화
    user_ids = sorted(set(ratings_df['u_id'].tolist() + [user_id]))
    dvd_ids = sorted([dvd[0] for dvd in dvds])
    matrix = pd.DataFrame(index=user_ids, columns=dvd_ids, dtype=float)

    # 기존 평점값 채우기
    for _, r in ratings_df.iterrows():
        matrix.at[r['u_id'], r['d_id']] = r['rating']

    # 결측값 채우기 (임시 평점: 유저 평균 또는 0)
    for uid in matrix.index:
        mean_val = user_means.get(uid, 0.0)
        matrix.loc[uid] = matrix.loc[uid].fillna(mean_val)
    # Cosine similarity 계산
    mat = matrix.values
    norms = np.linalg.norm(mat, axis=1)
    target_idx = matrix.index.get_loc(user_id)
    target_vec = mat[target_idx]
    sims = {}
    for idx, uid in enumerate(matrix.index):
        vec = mat[idx]
        if norms[target_idx] > 0 and norms[idx] > 0:
            sims[uid] = np.dot(target_vec, vec) / (norms[target_idx] * norms[idx])
        else:
            sims[uid] = 0.0

    # 예측 평점 계산 (추천 후보: 평점 남기지 않은 DVD)
    rated_set = set(ratings_df[ratings_df['u_id'] == user_id]['d_id'].tolist())
    candidates = [d for d, _, _, _ in dvds if d not in rated_set]
    if not candidates:
        print('No DVDs available for recommendation.')
        return

    preds = {}
    for d_id in candidates:
        numer = 0.0
        denom = 0.0
        for uid, sim in sims.items():
            if uid == user_id:
                continue
            rating_val = matrix.at[uid, d_id]
            numer += sim * rating_val
            denom += abs(sim)
        preds[d_id] = numer / denom if denom != 0 else 0.0

    # 최고 예측 평점 DVD 선택 (예측평점, ID 기준)
    best_did, best_pred = sorted(preds.items(), key=lambda x: (-x[1], x[0]))[0]

    # 평균 평점 조회
    cursor.execute("SELECT ROUND(AVG(rating),1) FROM Rating WHERE d_id = %s", (best_did,))
    avg_rt = cursor.fetchone()[0]

    # DVD 정보 조회
    cursor.execute("SELECT d_title, d_name, age_limit FROM DVD WHERE d_id = %s", (best_did,))
    title, director, age_limit = cursor.fetchone()

    # 결과 출력
    print('--------------------------------------------------------------------------------')
    print('id title director age_limit avg.rating exp.rating')
    print('--------------------------------------------------------------------------------')
    avg_display = 'None' if avg_rt is None else f'{avg_rt:.1f}'
    print(f'{best_did} {title} {director} {age_limit} {avg_display} {best_pred:.2f}')
    print('--------------------------------------------------------------------------------')
    return


def main():
    
    
    while True:
        print('============================================================')
        print('1. initialize database')
        print('2. print all DVDs')
        print('3. print all users')
        print('4. insert a new DVD')
        print('5. remove a DVD')
        print('6. insert a new user')
        print('7. remove a user')
        print('8. check out a DVD')
        print('9. return and rate a DVD')
        print('10. print borrowing status of a user')
        print('11. search DVDs')
        print('12. recommend a DVD for a user using popularity-based method')
        print('13. recommend a DVD for a user using user-based collaborative filtering')
        print('14. exit')
        print('15. reset database')
        print('============================================================')
        menu = int(input('Select your action: '))

        if menu == 1:
            initialize_database()
        elif menu == 2:
            print_DVDs()
        elif menu == 3:
            print_users()
        elif menu == 4:
            insert_DVD()
        elif menu == 5:
            remove_DVD()
        elif menu == 6:
            insert_user()
        elif menu == 7:
            remove_user()
        elif menu == 8:
            checkout_DVD()
        elif menu == 9:
            return_and_rate_DVD()
        elif menu == 10:
            print_borrowing_status_for_user()
        elif menu == 11:
            search()
        elif menu == 12:
            recommend_popularity()
        elif menu == 13:
            recommend_user_based()
        elif menu == 14:
            print('Bye!')
            break
        elif menu == 15:
            reset()
        else:
            print('Invalid action')

    close_connection()

if __name__ == "__main__":
    main()
