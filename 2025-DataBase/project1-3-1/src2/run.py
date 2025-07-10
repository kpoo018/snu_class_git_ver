import lark
import os
import time
from berkeleydb import db

# prompt 출력 함수
def prompt():
    print("DB_2023-19674> ", end="") 

# 입력된 query가 ;으로 끝나는지 확인하는 함수. 
# 뒤쪽의 white space는 무시함.
def is_complete_query(query):
    if len(query.strip())>0 and query.strip()[-1] == ";":
        return True
    else:
        return False
    
# query 입력받는 함수
def get_query():
    prompt()
    query = input()

    # '\n'이 포함된 query를 처리
    while  not is_complete_query(query):
        prompt()
        query += (" " + input())
    return query

# query에서 오류가 발생하기 전 query 까지만 리턴
def get_valid_query(query, e):
    # 오류 위치를 기준으로 쿼리를 자름
    error_position = e.pos_in_stream  # 오류가 발생한 위치
    valid_query = " ; ".join(query[:error_position].split(";")[:-1])+" ; "  # 오류 이전까지의 쿼리

    return valid_query

class SQLBoolean:
    TRUE = True
    FALSE = False
    UNKNOWN = "UNKNOWN"

    @staticmethod
    def and_op(left, right):
        if left == SQLBoolean.FALSE or right == SQLBoolean.FALSE:
            return SQLBoolean.FALSE
        if left == SQLBoolean.UNKNOWN or right == SQLBoolean.UNKNOWN:
            return SQLBoolean.UNKNOWN
        return SQLBoolean.TRUE

    @staticmethod
    def or_op(left, right):
        if left == SQLBoolean.TRUE or right == SQLBoolean.TRUE:
            return SQLBoolean.TRUE
        if left == SQLBoolean.UNKNOWN or right == SQLBoolean.UNKNOWN:
            return SQLBoolean.UNKNOWN
        return SQLBoolean.FALSE

    @staticmethod
    def not_op(value):
        if value == SQLBoolean.TRUE:
            return SQLBoolean.FALSE
        if value == SQLBoolean.FALSE:
            return SQLBoolean.TRUE
        return SQLBoolean.UNKNOWN

class MyTransformerError(Exception):
    pass

class CharLengthError(Exception):
    pass

class OtherError(Exception):
    pass

class IncomparableError(Exception):
    pass


# lark의 Transformer 클래스는 tree 형태로 파싱된 결과를 leaf부터 하나씩 읽으며 token의 이름과 동일한 이름의 함수를 호출해줌
class MyTransformer(lark.Transformer):
    def __init__(self):
        super().__init__()
        self.db_env = db.DBEnv()
        
        db_home = "MyDBs"
        if not os.path.exists(db_home):
            os.makedirs(db_home)

        # DB 환경을 생성
        try:
            self.db_env.open(db_home, db.DB_INIT_MPOOL | db.DB_CREATE | db.DB_INIT_TXN | db.DB_INIT_LOG | db.DB_INIT_LOCK)
        except db.DBError as e:
            try:
                self.db_env.close()
            except Exception:
                pass
            raise
         # __meta__ 데이터베이스 생성
        meta_db = db.DB(self.db_env)
        try:
            meta_db.open("__meta__", None, dbtype=db.DB_BTREE, flags=db.DB_CREATE)
            meta_db.close()
        except db.DBError as e:
            try:
                meta_db.close()
            except Exception:
                pass
            raise
        
    def create_table_query(self, items):

        # 트랜잭션 시작
        txn = self.db_env.txn_begin()
        try:
            # 테이블 생성 시 __meta__ 데이터베이스 열기
            meta_db = db.DB(self.db_env)
            meta_db.open("__meta__", None, dbtype=db.DB_BTREE, txn=txn)
            
            # 테이블 이름 추출
            table_name = items[2].children[0].lower()

            # 테이블 중복 확인
            self._check_table_existence(table_name, meta_db, txn)
            
            # 컬럼 정의 추출 및 검증
            schema_columns, not_null_columns = self._extract_and_validate_columns(items)
            
            # 테이블 제약 조건 추출 및 검증
            table_constraints = self._extract_and_validate_constraints(items, schema_columns, meta_db, txn)

            # 테이블 정보 저장
            table_id = self._save_table_info(table_name, schema_columns, not_null_columns, table_constraints, meta_db, txn)

            new_db = db.DB(self.db_env)

            # 테이블 데이터베이스 생성
            try:
                new_db.open(table_id, None, dbtype=db.DB_HASH, flags=db.DB_CREATE | db.DB_EXCL, txn=txn)
            except Exception as e:
                new_db.close()
                raise
            
            # 트랜잭션 커밋
            txn.commit()
            prompt()
            print(f"'{table_name}' table is created")
            new_db.close()
            meta_db.close()

        except MyTransformerError as e:
            # 사용자 정의 에러 처리
            txn.abort()
            meta_db.close()
            raise MyTransformerError(f"Create table has failed: {e}")
        except Exception as e:
            # 기타 에러 처리
            txn.abort()
            meta_db.close()
            raise

    # 테이블 ID 가져오기
    def _get_table_id(self, table_name, meta_db, txn):
        table_id_key= f"__table_id__:{table_name}".encode()
        if meta_db.get(table_id_key, txn=txn):
            return meta_db.get(table_id_key, txn=txn).decode()
        else: 
            raise MyTransformerError()
        
    # 테이블 중복 확인
    def _check_table_existence(self, table_name, meta_db, txn):
        schema_key = f"__table_id__:{table_name}".encode()
        if meta_db.get(schema_key,txn=txn):
            raise MyTransformerError("table with the same name already exists")

    # 컬럼 정의 추출 및 검증
    def _extract_and_validate_columns(self, items):
        column_definition_iter = items[3].find_data("column_definition")
        schema_columns = []  # 컬럼 이름 리스트
        not_null_columns = []  # NOT NULL 컬럼 리스트

        for column_definition in column_definition_iter:
            column_name = column_definition.children[0].children[0].lower()
            column_type = column_definition.children[1].children[0].lower()

            
            # 컬럼 이름 중복 확인
            if column_name in schema_columns:
                raise MyTransformerError("column definition is duplicated")

            # char 타입 길이 검증
            if column_type.startswith("char"):
                char_length = int(column_definition.children[1].children[2])
                if char_length < 1:
                    raise CharLengthError("Char length should be over 0")
                column_type = f"char({char_length})"

            # 컬럼 정보 저장
            schema_columns.append((f"{column_name}", f"{column_type}"))

            # NOT NULL 확인
            if (
                isinstance(column_definition.children[2], lark.Token)
                and column_definition.children[2].value.lower() == "not"
                and column_definition.children[3].value.lower() == "null"
            ):
                # NOT NULL 컬럼 리스트에 추가
                not_null_columns.append(f"{column_name}")


        return schema_columns, not_null_columns

    # 테이블 제약 조건 추출 및 검증
    def _extract_and_validate_constraints(self, items, schema_columns, meta_db, txn):
        table_constraints = []
        primary_key_columns = []
        table_constraint_iter = items[3].find_data("table_constraint_definition")

        for table_constraint_definition in table_constraint_iter:
            if table_constraint_definition.children[0].data == "primary_key_constraint":
                # PRIMARY KEY 처리
                if primary_key_columns:
                    raise MyTransformerError("primary key definition is duplicated")
                primary_key_columns = [
                    col.children[0].lower()
                    for col in table_constraint_definition.children[0].children[2].find_data("column_name")
                ]
                exist_columns = set([col[0] for col in schema_columns])
                for pk_col in primary_key_columns:
                    if pk_col not in exist_columns:
                        raise MyTransformerError(f"cannot define non-existing column '{pk_col}' as primary key")
                table_constraints.append(f"PRIMARY KEY:{','.join(primary_key_columns)}")

            elif table_constraint_definition.children[0].data == "referential_constraint":
                # FOREIGN KEY 처리
                foreign_key_columns, referenced_table, referenced_columns = self._extract_foreign_key(
                    table_constraint_definition
                )
                try:
                    referenced_table_id = self._get_table_id(referenced_table, meta_db, txn)
                except MyTransformerError as e:
                    raise MyTransformerError("foreign key references non existing table or column")
                self._validate_foreign_key(foreign_key_columns, referenced_table_id, referenced_columns, schema_columns, meta_db, txn)
                table_constraints.append(
                    f"FOREIGN KEY:{','.join(foreign_key_columns)}->{referenced_table_id}({','.join(referenced_columns)})"
                )
            
        return table_constraints

    # FOREIGN KEY 추출
    def _extract_foreign_key(self, table_constraint_definition):
        foreign_key_columns = [
            col.children[0].lower()
            for col in table_constraint_definition.children[0].children[2].find_data("column_name")
        ]
        referenced_table = table_constraint_definition.children[0].children[4].children[0].lower()
       
        referenced_columns = [
            col.children[0].lower()
            for col in table_constraint_definition.children[0].children[5].find_data("column_name")
        ]
        return foreign_key_columns, referenced_table, referenced_columns

    # FOREIGN KEY 검증
    def _validate_foreign_key(self, foreign_key_columns, referenced_table_id, referenced_columns, schema_columns, meta_db, txn):
        # Foreign key 컬럼 존재 여부 확인
        exist_columns = set([col[0] for col in schema_columns])
        for fk_col in foreign_key_columns:
            if fk_col not in exist_columns:
                raise MyTransformerError(f"cannot define non-existing column '{fk_col}' as foreign key")

        # 참조 테이블 및 컬럼 확인
        ref_schema_key = f"__schema__:{referenced_table_id}".encode()
        ref_schema = meta_db.get(ref_schema_key, txn=txn)
        if not ref_schema:
            raise MyTransformerError("Fatal Error: Table ID exist, but schema does not exist.")
        
        # 참조 컬럼 존재 여부 확인
        ref_columns = set([col.split(":")[0] for col in ref_schema.decode().split("|")])
        for ref_col in referenced_columns:
            if ref_col not in ref_columns:
                raise MyTransformerError("foreign key references non existing table or column")
        
        # 참조 컬럼과 데이터 타입 일치 여부 확인
        schema_column_types = {col[0]: col[1] for col in schema_columns}
        ref_column_types = {col.split(":")[0]: col.split(":")[1] for col in ref_schema.decode().split("|")}
        for fk_col, ref_col in zip(foreign_key_columns, referenced_columns):
            if schema_column_types[fk_col] != ref_column_types[ref_col]:
                raise MyTransformerError("foreign key references wrong type")



        # 참조 컬럼이 Primary Key인지 확인
        ref_constraints_key = f"__constraints__:{referenced_table_id}".encode()
        ref_constraints = meta_db.get(ref_constraints_key, txn=txn)
        if not ref_constraints:
            raise MyTransformerError("foreign key references non primary key column")

        ref_constraints = ref_constraints.decode()
        primary_key_def = next(
            (c for c in ref_constraints.split("|") if c.startswith("PRIMARY KEY:")), None
        )
        if not primary_key_def:
            raise MyTransformerError("foreign key references non primary key column")
    
        primary_key_columns = primary_key_def.split(":")[1].split(",")
        if set(primary_key_columns) != set(referenced_columns):
            raise MyTransformerError("foreign key references non primary key column")

       
    # 테이블 정보 저장
    def _save_table_info(self,  table_name, schema_columns, not_null_columns, table_constraints, meta_db, txn):
        # 테이블 ID 생성 및 저장
        table_id=self._save_table_id(table_name, meta_db, txn)
        # 테이블 스키마 저장
        self._save_schema(table_id, schema_columns, meta_db, txn)
         # 참조 관계 저장
        self._save_references(table_id, table_constraints, meta_db, txn)
        # 제약 조건 저장
        self._save_constraints(table_id, not_null_columns, table_constraints, meta_db, txn)

        return table_id
    # 테이블 ID 생성 및 저장
    def _save_table_id(self, table_name, meta_db, txn):
        table_id = str(hash(table_name+str(time.time()*1000)))+"_"+str(int(time.time()*1000))
        table_id_key = f"__table_id__:{table_name}".encode()
        meta_db.put(table_id_key, table_id.encode(), txn=txn)
        return table_id
    # 테이블 스키마 저장
    def _save_schema(self, table_id, schema_columns, meta_db, txn):
        schema_key = f"__schema__:{table_id}".encode()
        schema_value = "|".join([f"{col}:{col_type}" for col, col_type in schema_columns]).encode()

        # 스키마 저장
        meta_db.put(schema_key, schema_value, txn=txn)
    # 참조 관계 저장
    def _save_references(self, table_id, table_constraints, meta_db, txn):
        for constraint in table_constraints:
            if constraint.startswith("FOREIGN KEY:"):
                foreign_key_info = constraint.split(":")[1]
                foreign_key_columns, referenced_table_info = foreign_key_info.split("->")
                referenced_table_id = referenced_table_info.split("(")[0]

                # 참조 관계 업데이트
                ref_key = f"__references__:{referenced_table_id}".encode()
                ref_value = meta_db.get(ref_key, txn=txn)
                if ref_value:
                    ref_tables = ref_value.decode().split("|")
                    if table_id not in ref_tables:
                        ref_tables.append(table_id)
                        meta_db.put(ref_key, "|".join(ref_tables).encode())
                else:
                    meta_db.put(ref_key, table_id.encode(), txn=txn)
    # 테이블 제약 조건 저장
    def _save_constraints(self, table_id, not_null_columns, table_constraints, meta_db, txn):
        
        if not_null_columns:
            table_constraints.append(f"NOT NULL:{','.join(not_null_columns)}")

        if table_constraints:
            constraints_key = f"__constraints__:{table_id}".encode()
            constraints_value = "|".join(table_constraints).encode()

            # 제약 조건 저장
            meta_db.put(constraints_key, constraints_value, txn=txn)


    def drop_table_query(self, items):
        # 트랜잭션 시작
        txn = self.db_env.txn_begin()
        try:
            # 테이블 삭제 시 __meta__ 데이터베이스 열기
            meta_db = db.DB(self.db_env)
            meta_db.open("__meta__", None, dbtype=db.DB_BTREE, txn=txn)

            # 테이블 이름 추출
            table_name = items[2].children[0].lower()

            # 테이블 ID 가져오기
            try:
                table_id = self._get_table_id(table_name, meta_db, txn)
            except MyTransformerError as e:
                raise MyTransformerError("no such table")
            
            # 다른 테이블이 foreign key로 참조하고 있는지 확인
            if self._is_table_referenced(table_id, table_name, meta_db, txn):
                raise MyTransformerError(f"'{table_name}' is referenced by another table")
            
            # 테이블 스키마 및 제약 조건 삭제
            self._delete_table_info(table_name, table_id, meta_db, txn)

            # 테이블 데이터베이스 삭제
            self.db_env.dbremove(table_id, None, txn, 0)
            
            # 트랜잭션 커밋
            txn.commit()
            prompt()
            print(f"'{table_name}' table is dropped")
            meta_db.close()

        except MyTransformerError as e:
            # 사용자 정의 에러 처리
            txn.abort()
            meta_db.close()
            raise MyTransformerError(f"Drop table has failed: {e}")
        
        except Exception as e:
            # 기타 에러 처리
            txn.abort()
            meta_db.close()
            raise
        
        

    # 테이블 참조 여부 확인
    def _is_table_referenced(self, table_id, table_name, meta_db, txn):
        
        ref_key = f"__references__:{table_id}".encode()

        if not meta_db.get(ref_key, txn=txn):
            return False
        return True

    # 테이블 정보 삭제
    def _delete_table_info(self, table_name, table_id, meta_db, txn):
        # 테이블 ID 삭제
        table_id_key = f"__table_id__:{table_name}".encode()
        meta_db.delete(table_id_key, txn=txn)

        # 스키마 삭제
        schema_key = f"__schema__:{table_id}".encode()
        meta_db.delete(schema_key, txn=txn)

        # 참조 관계 삭제
        constraints_key = f"__constraints__:{table_id}".encode()
        constraints=meta_db.get(constraints_key, txn=txn)
        if constraints:
            constraints = constraints.decode()
            for constraint in constraints.split("|"):
                if constraint.startswith("FOREIGN KEY:"):
                    referenced_table_id = constraint.split("->")[1].split("(")[0]
                    ref_key = f"__references__:{referenced_table_id}".encode()
                    ref_value = meta_db.get(ref_key, txn=txn)
                    if not ref_value:
                        continue
                    ref_tables = ref_value.decode().split("|")
                    if table_id not in ref_tables: #발생할 가능성 없음
                        continue
                    ref_tables.remove(table_id)
                    if ref_tables:
                        meta_db.put(ref_key, "|".join(ref_tables).encode(), txn=txn)
                    else:
                        meta_db.delete(ref_key, txn=txn)
            
            # 제약 조건 삭제
            meta_db.delete(constraints_key, txn=txn)
        

    def explain_query(self, items):

        txn=self.db_env.txn_begin()
        try:
            meta_db = db.DB(self.db_env)
            meta_db.open("__meta__", None, dbtype=db.DB_BTREE, txn=txn)

            # 테이블 이름 추출
            table_name = items[1].children[0].lower()

            # 테이블 ID 가져오기
            try:
                table_id = self._get_table_id(table_name, meta_db, txn)
            except MyTransformerError as e:
                raise MyTransformerError("no such table")

            # 테이블 schema 가져오기 
            schema_key = f"__schema__:{table_id}".encode()
            schema_value = meta_db.get(schema_key, txn=txn)
            if not schema_value:
                raise MyTransformerError("Fatal Error: Table ID exist, but schema does not exist.")
            
            schema_column_types = schema_value.decode().split("|")
            schema_column_types = {col.split(":")[0]:col.split(":")[1] for col in schema_column_types}

            #table constraints 가져오기
            constraints_key = f"__constraints__:{table_id}".encode()
            constraints_value = meta_db.get(constraints_key, txn=txn)
            primary_key_columns = []
            foreign_key_columns = []
            not_null_columns = []
            if constraints_value:
                constraints_value = constraints_value.decode().split("|")
                #primary key 가져오기
                primary_key_columns = [
                    col 
                    for c in constraints_value if c.startswith("PRIMARY KEY:") 
                    for col in c.split(":")[1].split(",") 
                ]
                #foreign key 가져오기
                foreign_key_columns = [c.split(":")[1] for c in constraints_value if c.startswith("FOREIGN KEY:")]
                foreign_key_columns = [fk.split("->")[0] for fk in foreign_key_columns]
                foreign_key_columns = [col for fk in foreign_key_columns for col in fk.split(",")]
                #not null 가져오기
                not_null_columns =[
                    col 
                    for c in constraints_value if c.startswith("NOT NULL:") 
                    for col in c.split(":")[1].split(",")
                ]

            # schema 출력
            print(f"-"*66)
            print(f" {'column_name':<20} | {'type':<20} | {'null':<4} | {'key':<7} ")
            for column_name, column_type in schema_column_types.items():
                null_value = "N" if column_name in not_null_columns or column_name in primary_key_columns else "Y"
                key_value = []
                if column_name in primary_key_columns:
                    key_value.append("PRI")
                if column_name in foreign_key_columns:
                    key_value.append("FOR")
                key_value = "/".join(key_value)
                print(f" {column_name[:20]:<20} | {column_type[:20]:<20} | {null_value[:4]:<4} | {key_value[:7]:<7} ")
            print(f"-"*66)
            print(f"{len(schema_column_types)} rows in set")

            txn.commit()
            meta_db.close()
               
        except MyTransformerError as e:
            # 사용자 정의 에러 처리
            txn.abort()
            meta_db.close()
            raise MyTransformerError(f"{items[0].capitalize()} table has failed: {e}")
        
        except Exception as e:
            # 기타 에러 처리
            txn.abort()
            meta_db.close()
            raise
    

    def describe_query(self, items):
        self.explain_query(items)
    
    def desc_query(self, items):
        self.explain_query(items)
    
    def insert_query(self, items):

        txn = self.db_env.txn_begin()
        try:
            meta_db = db.DB(self.db_env)
            meta_db.open("__meta__", None, db.DB_BTREE, txn=txn)

            # 테이블 이름 추출
            table_name = items[2].children[0].lower()

            # 테이블 ID 가져오기
            try:
                table_id = self._get_table_id(table_name, meta_db, txn)
            except MyTransformerError as e:
                raise MyTransformerError("no such table")

            # 테이블 schema 가져오기
            schema_key = f"__schema__:{table_id}".encode()
            schema_value = meta_db.get(schema_key, txn=txn)
            if not schema_value:
                raise MyTransformerError("Fatal Error: Table ID exist, but schema does not exist.")
            
            # key: cloumn name, value: column type인 dictionary로 변환
            schema_column_types = schema_value.decode().split("|")
            schema_column_types = {col.split(":")[0]: col.split(":")[1] for col in schema_column_types}
            
            #table constraints 가져오기
            constraints_key = f"__constraints__:{table_id}".encode()
            constraints_value = meta_db.get(constraints_key, txn=txn)
            primary_key_columns = []
            foreign_key_columns = []
            not_null_columns = []
            if constraints_value:
                constraints_value = constraints_value.decode().split("|")
                #primary key 가져오기
                primary_key_columns = [
                    col 
                    for c in constraints_value if c.startswith("PRIMARY KEY:") 
                    for col in c.split(":")[1].split(",") 
                ]
                #foreign key 가져오기
                foreign_key_columns = [c.split(":")[1] for c in constraints_value if c.startswith("FOREIGN KEY:")]
                foreign_key_columns = [fk.split("->")[0] for fk in foreign_key_columns]
                foreign_key_columns = [col for fk in foreign_key_columns for col in fk.split(",")]
                #not null 가져오기
                not_null_columns =[
                    col 
                    for c in constraints_value if c.startswith("NOT NULL:") 
                    for col in c.split(":")[1].split(",")
                ]

            # 입력된 값 추출
            values = [child.children[0] for child in items[5].find_data("column_value")]
            
            # 명시적으로 입력된 컬럼 순서 확인
            if items[3]:
                # 명시적으로 입력된 컬럼 이름 추출
                new_schema_columns = items[3].find_data("column_name")
                new_schema_columns = [col.children[0].lower() for col in new_schema_columns]

                # 입력된 컬럼에 중복이 있는지 확인
                # 추가 구현
                if len(new_schema_columns) != len(set(new_schema_columns)):
                    raise MyTransformerError("column name is duplicated")
                
                # 입력된 컬럼이 스키마에 존재하는지 확인
                for col in new_schema_columns:
                    if col not in schema_column_types.keys():
                        raise MyTransformerError(f"'{col}' does not exist")

                # 컬럼 개수와 값 개수 확인
                if len(new_schema_columns) != len(values):
                    raise MyTransformerError("types are not matched")

                # 스키마 순서에 맞게 values 재정렬
                reordered_values = []
                for schema_col in schema_column_types.keys():
                    if schema_col in new_schema_columns:
                        # 명시적으로 입력된 컬럼의 값을 가져옴
                        index = new_schema_columns.index(schema_col)
                        reordered_values.append(values[index])
                    else:
                        # 명시적으로 입력되지 않은 컬럼은 NULL로 처리
                        reordered_values.append(lark.lexer.Token("NULL", "null"))
                values = reordered_values

            # 컬럼 개수와 값 개수 확인
            if len(schema_column_types) != len(values):
                raise MyTransformerError("types are not matched")
            
            # 값 검증 및 변환
            new_values = []
            for (column_name, column_type), value in zip(schema_column_types.items(), values):
                # NULL 처리
                if value.type == "NULL":
                    # NOT NULL 확인
                    if (column_name in not_null_columns or column_name in primary_key_columns):
                        raise MyTransformerError(f"'{column_name}' is not nullable")
                    else:
                        value = "null"
                else:
                    # 데이터 타입 확인 및 변환
                    if column_type.startswith("char"):
                        if value.type != 'STR':
                            raise MyTransformerError("types are not matched")
                        
                        max_length = int(column_type.split("(")[1].split(")")[0])

                        value = value.lower()[1:-1]  # 따옴표 제거
                        if len(value) > max_length:
                            value = value.lower()[:max_length]  # 문자열 자르기
                        value = f"'{value}'"
                        
                    
                    elif column_type == "int":
                        if value.type != 'INT':
                            raise MyTransformerError("types are not matched")
                        value = value.lower()
                    
                    elif column_type == "date":
                        if value.type != 'DATE':
                            raise MyTransformerError("types are not matched")
                        value = self._is_valid_date(value.lower())

                new_values.append(value)

            # 테이블 데이터베이스 열기
            table_db = db.DB(self.db_env)
            table_db.open(table_id, None, dbtype=db.DB_HASH, txn=txn)

            # 데이터 삽입
            table_db.put("|".join(new_values).encode(), "|".join(new_values).encode(), txn=txn)

            # 트랜잭션 커밋
            txn.commit()
            prompt()
            print("1 row inserted")
            table_db.close()

        except MyTransformerError as e:
            txn.abort()
            meta_db.close()
            raise MyTransformerError(f"Insert has failed: {e}")

        except Exception as e:
            txn.abort()
            meta_db.close()
            raise
    
    # 날짜 형식 검증
    #추가 구현
    def _is_valid_date(self, date_str):
        date = date_str.split("-")
        year = int(date[0])
        month = int(date[1])
        day = int(date[2])

        # 1. 년도 범위 체크 (2000 ~ 2030)
        if year < 2000 or year > 2030:
            raise MyTransformerError("Invalid Date")
        
        # 2. 월 범위 체크 (1 ~ 12)
        if month < 1 or month > 12:
            raise MyTransformerError("Invalid Date")
        
        # 3. 월별 일 수 검증
        # 월별로 최대 일수를 설정
        days_in_month = {
            1: 31, 2: 28, 3: 31, 4: 30, 5: 31, 6: 30,
            7: 31, 8: 31, 9: 30, 10: 31, 11: 30, 12: 31
        }
        
        # 윤년 체크 (2월 29일 처리)
        if year % 4 == 0 and (year % 100 != 0 or year % 400 == 0):
            days_in_month[2] = 29  # 윤년이면 2월 29일까지 존재
        
        # 3. 해당 월의 최대 일 수를 넘지 않으면 True, 그렇지 않으면 False
        if day < 1 or day > days_in_month[month]:
            raise MyTransformerError("Invalid Date")
        return date_str
        
    
    def delete_query(self, items):

        txn = self.db_env.txn_begin()
        try:
            meta_db = db.DB(self.db_env)
            meta_db.open("__meta__", None, db.DB_BTREE, txn=txn)

            # 테이블 이름 추출
            table_name = items[2].children[0].lower()

            # 테이블 ID 가져오기
            try:
                table_id = self._get_table_id(table_name, meta_db, txn)
            except MyTransformerError as e:
                raise MyTransformerError("no such table")

            # 테이블 schema 가져오기
            schema_key = f"__schema__:{table_id}".encode()
            schema_value = meta_db.get(schema_key, txn=txn)
            if not schema_value:
                raise MyTransformerError("Fatal Error: Table ID exist, but schema does not exist.")
            
            # key: cloumn name, value: column type인 dictionary로 변환
            schema_column_types = schema_value.decode().split("|")
            schema_column_types = {col.split(":")[0]: col.split(":")[1] for col in schema_column_types}
            schema_column_types = {f"{table_name}.{key}":schema_column_types[key] for key in schema_column_types.keys()}
            
            # 테이블 데이터베이스 열기
            table_db = db.DB(self.db_env)
            table_db.open(table_id, None, dbtype=db.DB_HASH, txn=txn)

            # 테이블 데이터 삭제
            cursor = table_db.cursor(txn=txn)
            cnt = 0
            flag = False
            if self._is_table_referenced(table_id, table_name, meta_db, txn):
                # reference되고 있으면 delete불가, flag True
                flag=True
            while record := cursor.next():
                # 이 redord가 삭제 대상인지 확인
                if self._check_where_clause(items[3], record[1].decode().split("|"), schema_column_types):
                    if not flag:
                        table_db.delete(record[0], txn=txn)
                    cnt += 1

            if flag:
                raise OtherError(f"{cnt} row(s) are not deleted due to referential integrity")

            cursor.close()
            table_db.close()
            txn.commit()
            meta_db.close()
            prompt()
            print(f"{cnt} row(s) deleted")
            

        except MyTransformerError as e:
            txn.abort()
            meta_db.close()
            raise MyTransformerError(f"Delete has failed: {e}")
        
        except OtherError as e:
            cursor.close()
            table_db.close()
            txn.abort()
            meta_db.close()
            raise OtherError(e)
        
        except Exception as e:
            txn.abort()
            meta_db.close()
            raise

    def _check_where_clause(self, where_clause, record, schema_column_types):
        if not where_clause:
            return True

        try:
            boolean_expr = where_clause.children[1]
    
            return self._solve_boolean_expr(boolean_expr, record, schema_column_types)

        except MyTransformerError as e:
            raise OtherError(e)
        

    def _solve_boolean_expr(self, boolean_expr, record, schema_column_types):
        # Or
        result = SQLBoolean.FALSE
        for term in boolean_expr.find_data("boolean_term"):
            term_result = self._solve_boolean_term(term, record, schema_column_types)
            result = SQLBoolean.or_op(result, term_result)
        return result

    def _solve_boolean_term(self, boolean_term, record, schema_column_types):
        # And
        result = SQLBoolean.TRUE
        for factor in boolean_term.find_data("boolean_factor"):
            factor_result = self._solve_boolean_factor(factor, record, schema_column_types)
            result = SQLBoolean.and_op(result, factor_result)
        return result
    
    def _solve_boolean_factor(self, boolean_factor, record, schema_column_types):
        # Not
        if boolean_factor.children[0] != None:  # Handle NOT operator
            return SQLBoolean.not_op(self._solve_boolean_test(boolean_factor.children[1], record, schema_column_types))
        return self._solve_boolean_test(boolean_factor.children[1], record, schema_column_types)

    def _solve_boolean_test(self, boolean_test, record, schema_column_types):
        # Handle parenthesized expressions
        if boolean_test.children[0].data == "parenthesized_boolean_expr":
            return self._solve_boolean_expr(boolean_test.children[0].children[1], record, schema_column_types)

        # Handle predicates
        return self._solve_predicate(boolean_test.children[0], record, schema_column_types)


    def _solve_predicate(self, predicate, record, schema_column_types):
        if predicate.children[0].data == "comparison_predicate":
            return self._solve_comparison_predicate(predicate.children[0], record, schema_column_types)
        
        elif predicate.children[0].data == "null_predicate":
            return self._solve_null_predicate(predicate.children[0], record, schema_column_types)
        
    def _solve_comparison_predicate(self, comparison_predicate, record, schema_column_types):
        left_operand = self._resolve_operand(comparison_predicate.children[0], record, schema_column_types)
        operator = comparison_predicate.children[1].children[0].lower()
        right_operand = self._resolve_operand(comparison_predicate.children[2], record, schema_column_types)
        
        # Handle NULL values
        if left_operand == "null" or right_operand == "null":
            return SQLBoolean.UNKNOWN

        # Check data type compatibility
        if left_operand.type != right_operand.type:
            raise MyTransformerError("Trying to compare incomparable columns or values")
        if left_operand.type == "STR" and operator not in ["=", "!="]:
            raise MyTransformerError("Trying to compare incomparable columns or values")

        # Evaluate the condition
        return self._evaluate_condition(left_operand, operator, right_operand)


    def _solve_null_predicate(self, null_predicate, record, schema_column_types):
        table_name = null_predicate.children[0]
        column_name = null_predicate.children[1]
        null_operation = null_predicate.children[2]

        if table_name:
            if table_name.children[0].lower() not in {col.split(".")[0] for col in schema_column_types.keys()}:
                raise MyTransformerError("Where clause trying to reference tables which are not specified")
            
            column_name = f"{table_name.children[0].lower()}.{column_name.children[0].lower()}"
            if column_name not in schema_column_types.keys():
                raise MyTransformerError("Where clause trying to reference non existing column")
            
            column_value = record[list(schema_column_types.keys()).index(column_name)]
            if null_operation.children[1] == None:
                # IS NULL
                return column_value == 'null'
            else:
                # IS NOT NULL
                return column_value != 'null'
            
        else:
            column_name = column_name.children[0].lower()

            try:
                column_name = self._check_ambiguous_column(column_name, schema_column_types)
            except MyTransformerError as e:
                raise MyTransformerError(f"Where {e}")
            
            column_value = record[list(schema_column_types.keys()).index(column_name)]
            if null_operation.children[1] == None:
                # IS NULL
                return column_value == 'null'
            else:
                # IS NOT NULL
                return column_value != 'null'

        
    def _resolve_operand(self, operand, record, schema_column_types):
            if operand.children[0] != None and operand.children[0].data == "comparable_value":
                if operand.children[0].children[0].type == "STR":
                    value = operand.children[0].children[0].value.lower()
                    return lark.lexer.Token("STR", value)
                return operand.children[0].children[0]
            
            else:
                table_name = operand.children[0]
                column_name = operand.children[1]

                if table_name:
                    if table_name.children[0].lower() not in {col.split(".")[0] for col in schema_column_types.keys()}:
                        raise MyTransformerError("Where clause trying to reference tables which are not specified")
                    column_name = f"{table_name.children[0].lower()}.{column_name.children[0].lower()}"
                    if column_name not in schema_column_types.keys():
                        raise MyTransformerError("Where clause trying to reference non existing column")
                    
                    type = schema_column_types[column_name]
                    type = type.split("(")[0].upper()
                    if type == "CHAR":
                        type = "STR"
                    value = record[list(schema_column_types.keys()).index(column_name)]

                    return lark.lexer.Token(type, value)
                else:
                    column_name = column_name.children[0].lower()
                    try:
                        column_name = self._check_ambiguous_column(column_name, schema_column_types)
                    except MyTransformerError as e:
                        raise MyTransformerError(f"Where {e}")
                    type = schema_column_types[column_name]
                    type = type.split("(")[0].upper()
                    if type == "CHAR":
                        type = "STR"
                    value = record[list(schema_column_types.keys()).index(column_name)]
                    return lark.lexer.Token(type, value)


    def _evaluate_condition(self, left_value, operator, right_value):
        if left_value.type == "INT":
            left_value = lark.lexer.Token("INT", int(left_value.value))
            right_value = lark.lexer.Token("INT", int(right_value.value))

        if operator == "=":
            return SQLBoolean.TRUE if left_value.value == right_value.value else SQLBoolean.FALSE
        elif operator == "!=":
            return SQLBoolean.TRUE if left_value.value != right_value.value else SQLBoolean.FALSE
        elif operator == ">":
            return SQLBoolean.TRUE if left_value.value > right_value.value else SQLBoolean.FALSE
        elif operator == "<":
            return SQLBoolean.TRUE if left_value.value < right_value.value else SQLBoolean.FALSE
        elif operator == ">=":
            return SQLBoolean.TRUE if left_value.value >= right_value.value else SQLBoolean.FALSE
        elif operator == "<=":
            return SQLBoolean.TRUE if left_value.value <= right_value.value else SQLBoolean.FALSE

    def _check_ambiguous_column(self, column_name, schema_column_types):

        possible_columns = [col for col in schema_column_types.keys() if column_name == col.split(".")[1]]
        
        if len(possible_columns) == 1:
            return possible_columns[0]
        elif len(possible_columns) > 1:
            raise MyTransformerError("clause contains ambiguous column reference")
        else:
            raise MyTransformerError("clause trying to reference non existing column")
        
    def select_query(self, items):
        txn = self.db_env.txn_begin()
        try:
            meta_db = db.DB(self.db_env)
            meta_db.open("__meta__", None, db.DB_BTREE, txn=txn)

            select_list = items[1]
            from_clause = items[2].children[0]
            where_clause = items[2].children[1]
            group_by_clause = items[3]
            order_by_clause = items[4]


            # JOIN 포함 테이블 스키마 및 레코드 불러오기 및 JOIN 수행
            joined_schema_column_types, joined_records = self._join_tables(from_clause, meta_db, txn)

            # where 구문 처리
            temp_records = []
            for record in joined_records:
                if self._check_where_clause(where_clause, record, joined_schema_column_types):
                    temp_records.append(record)
            joined_records = temp_records


            if len(select_list.children) > 1 and select_list.children[1].data == "aggregate_column":

                selected_columns = self._perform_aggregate_function(select_list, group_by_clause, joined_schema_column_types, joined_records)
                # aggregate function 실행
                # joined_schema_column_types, joined_records에 값을 추가하는 방식으로 구현?
                # ㄴㄴ 이게 기본적으로 현재 구현은 중복 제거가 안 됨. aggregate가 있는 경우 아예 새로운 schema와 record를 만들어야함.
                # case 1 : group by 없음, select 없음
                # -> schema, records는 1개
                # case 2 : group by 없음, select 있음
                # -> schema 2개, records는 기존과 동일한 갯수
                # case 3 : group by 있음, select 없음
                # -> schema 1개, records는 group by에 따라 중복 제거된 갯수
                # case 4 : group by 있음, select 있음
                # -> schema 2개, records는 group by에 따라 중복 제거된 갯수
                # selected_columns에는 그러면 자동으로 반영?
                # ㄴㄴ selected_columns = [selected_column, aggregate_column]으로 직접 반영

            else :
                # select된 column 추출
                selected_columns = self._select_columns(select_list, joined_schema_column_types)

            # ORDER BY 구문 처리
            self._perform_order_by(joined_schema_column_types, joined_records, order_by_clause)

            # SELECT 쿼리 결과 출력
            self._print_select(joined_schema_column_types, joined_records, selected_columns)

            txn.commit()
            meta_db.close()
        except MyTransformerError as e:
            txn.abort()
            meta_db.close()
            raise MyTransformerError(f"Select has failed: {e}")
        except OtherError as e:
            txn.abort()
            meta_db.close()
            raise
        except Exception as e:
            txn.abort()
            meta_db.close()
            raise

    def _join_tables(self, from_clause, meta_db, txn):
        join_clause = from_clause.children[1]

        table_name = join_clause.children[0].children[0].children[0].lower()
        schema_column_types, records = self._get_table(table_name, meta_db, txn)
        schema_column_types = {f"{table_name}.{col}":schema_column_types[col] for col in schema_column_types.keys()} 
        
        if len(join_clause.children) > 1:
            
            # 두번째 테이블 가져오기
            second_table_name = join_clause.children[2].children[0].children[0].lower()
            second_schema_column_types, second_records = self._get_table(second_table_name, meta_db, txn)
            second_schema_column_types = {f"{second_table_name}.{col}":second_schema_column_types[col] for col in second_schema_column_types.keys()}
            
            # JOIN 수행
            return self._perform_join(schema_column_types, second_schema_column_types, records, second_records, join_clause, 5, meta_db, txn)
            
        else :
            # JOIN이 없는 경우
            return schema_column_types, records
    
    
    def _perform_join(self, table1_schema, table2_schema, records, second_records, join_clause, n, meta_db, txn):

        table1_name = {key.split(".")[0] for key in table1_schema.keys()}
        table2_name = list(table2_schema.keys())[0].split(".")[0]

        join_condition = join_clause.children[n-1]
        left_table = join_condition.children[0].children[0].lower()
        left_column = join_condition.children[1].children[0].lower()
        right_table = join_condition.children[3].children[0].lower()
        right_column = join_condition.children[4].children[0].lower()

        if left_table == table2_name and right_table in table1_name:
            left_table, right_table = right_table, left_table
            left_column, right_column = right_column, left_column
        left_key = f"{left_table}.{left_column}"
        right_key = f"{right_table}.{right_column}"
        if left_table in table1_name and right_table == table2_name:
            if left_key not in table1_schema.keys() or right_key not in table2_schema.keys():
                raise OtherError("From clause trying to reference non existing column")
            if table1_schema[left_key].split('(')[0] != table2_schema[right_key].split('(')[0]:
                raise IncomparableError("Trying to join incomparable columns or values")
        else:
            raise OtherError("Wrong join condition")
        
        # JOIN 수행
        left_idx= list(table1_schema.keys()).index(left_key)
        right_idx= list(table2_schema.keys()).index(right_key)
        joined_records = []
        for left_record in records:
            for right_record in second_records:
                if left_record[left_idx] == right_record[right_idx]:
                    joined_record = left_record + right_record
                    joined_records.append(joined_record)

        # JOIN된 스키마 생성
        joined_schema_column_types = {**table1_schema, **table2_schema}
        
        if len(join_clause.children) <= n:
            # 마지막 JOIN인 경우
            return joined_schema_column_types, joined_records
        else:
            next_table_name = join_clause.children[n+1].children[0].children[0].lower()
            next_schema_column_types, next_records = self._get_table(next_table_name, meta_db, txn)
            next_schema_column_types = {f"{next_table_name}.{col}":next_schema_column_types[col] for col in next_schema_column_types.keys()}

            # 다음 JOIN 수행
            return self._perform_join(joined_schema_column_types, next_schema_column_types, joined_records, next_records, join_clause, n+4, meta_db, txn)

    # aggregate function 처리
    def _perform_aggregate_function(self, select_list, group_by_clause, schema, records):

        selected_columns = []

        aggregate_function = select_list.children[1].children[0].children[0].lower()
        aggregate_table = select_list.children[1].children[2]
        aggregate_column = select_list.children[1].children[3]
        aggregate_key = None

        # aggregate_column이 schema에 존재하는지 확인
        if aggregate_table:
            if aggregate_table.children[0].lower() not in {col.split(".")[0] for col in schema.keys()}:
                raise OtherError("Group by clause trying to reference tables which are not specified")
            aggregate_key = f"{aggregate_table.children[0].lower()}.{aggregate_column.children[0].lower()}"
            if aggregate_key not in schema.keys():
                raise OtherError("Group by clause trying to reference non existing column")
        else:
            aggregate_column = aggregate_column.children[0].lower()
            try:
                aggregate_key = self._check_ambiguous_column(aggregate_column, schema)
            except MyTransformerError as e:
                raise OtherError(f"Group by {e}")
            
        column_type = schema[aggregate_key]
        column_idx = list(schema.keys()).index(aggregate_key)

        group_by_table = None
        group_by_column = None
        group_by_key = None
        group_by_idx = None
        # group by 구문이 존재하는지 확인
        if group_by_clause:
            group_by_table = group_by_clause.children[2]
            group_by_column = group_by_clause.children[3]
            group_by_key = None
            table_name = group_by_table
            column_name = group_by_column
            if table_name:
                if table_name.children[0].lower() not in {col.split(".")[0] for col in schema.keys()}:
                    raise OtherError("Group by clause trying to reference tables which are not specified")
                group_by_key = f"{table_name.children[0].lower()}.{column_name.children[0].lower()}"
                if group_by_key not in schema.keys():
                    raise OtherError("Group by clause trying to reference non existing column")
            else:
                column_name = column_name.children[0].lower()
                try:
                    group_by_key = self._check_ambiguous_column(column_name, schema)
                except MyTransformerError as e:
                    raise OtherError(f"Group by {e}")
            
            column_type = schema[group_by_key]
            group_by_idx = list(schema.keys()).index(group_by_key)


        selected_column = select_list.children[0]
        selected_key = None
        # selected_column이 schema에 존재하는지 확인
        if selected_column:
            if selected_column.children[0] == None:
                column_name = selected_column.children[1].children[0].lower()
                try:
                    selected_key = self._check_ambiguous_column(column_name, schema)
                except MyTransformerError:
                    raise MyTransformerError(f"fail to resolve '{column_name}'")
            else:
                table_name = selected_column.children[0].children[0].lower()
                if table_name not in {col.split(".")[0] for col in schema.keys()}:
                    raise MyTransformerError(f"'{table_name}' does not exist")
                column_name = selected_column.children[1].children[0].lower()
                selected_key = f"{table_name}.{column_name}"
                if selected_key not in schema.keys():
                    raise MyTransformerError(f"fail to resolve '{selected_key.split('.')[1]}'")
            
            if selected_key != aggregate_key and selected_key != group_by_key:
                raise MyTransformerError(f"column {selected_key} must either be included in the GROUP BY clause or be used in an aggregate function")
            selected_columns.append(selected_key)
        
        result = 0
        # aggregate function 처리
        if group_by_clause:
            for record in records:
                comp_value = record[group_by_idx] 
                result = record[list(schema.keys()).index(aggregate_key)]
                if aggregate_function == "sum":
                    result = "0"
                for record2 in records:
                    if record2[group_by_idx] == comp_value:
                        if aggregate_function == "max":
                            result = max(result, record2[list(schema.keys()).index(aggregate_key)])
                            if column_type == "int":
                                result = str(max(int(result), int(record2[list(schema.keys()).index(aggregate_key)])))
                        elif aggregate_function == "min":
                            result = min(result, record2[list(schema.keys()).index(aggregate_key)])
                            if column_type == "int":
                                result = str(min(int(result), int(record2[list(schema.keys()).index(aggregate_key)])))
                        elif aggregate_function == "sum":
                            result = str(int(result)+int(record2[list(schema.keys()).index(aggregate_key)]))
                        else:
                            raise MyTransformerError(f"Unknown aggregate function '{aggregate_function}'")
                record.append(result)
        else:
            if aggregate_function == "max":
                result = max(record[list(schema.keys()).index(aggregate_key)] for record in records)
                if column_type == "int":
                    result = str(max(int(record[list(schema.keys()).index(aggregate_key)]) for record in records))
            elif aggregate_function == "min":
                result = min(record[list(schema.keys()).index(aggregate_key)] for record in records)
                if column_type == "int":
                    result = str(min(int(record[list(schema.keys()).index(aggregate_key)]) for record in records))
            elif aggregate_function == "sum":
                result = str(sum(int(record[list(schema.keys()).index(aggregate_key)]) for record in records))
            else:
                raise MyTransformerError(f"Unknown aggregate function '{aggregate_function}'")
            
            for record in records:
                record.append(result) 

        aggregate_key = f"{aggregate_function}({aggregate_key})"
        schema[aggregate_key] = column_type
        selected_columns.append(aggregate_key)
        return selected_columns
    # select된 column 추출
    def _select_columns(self, select_list, schema):
        if select_list.children == []:
            # SELECT *인 경우
            selected_columns = ["*"]
        else:
            selected_columns = []
            for selected_column in select_list.find_data("selected_column"):
                if selected_column.children[0] == None:
                    column_name = selected_column.children[1].children[0].lower()
                    try:
                        column_name = self._check_ambiguous_column(column_name, schema)
                    except MyTransformerError:
                        raise MyTransformerError(f"fail to resolve '{column_name}'")
                else:
                    table_name = selected_column.children[0].children[0].lower()
                    if table_name not in {col.split(".")[0] for col in schema.keys()}:
                        raise MyTransformerError(f"'{table_name}' does not exist")
                    column_name = selected_column.children[1].children[0].lower()
                    column_name = f"{table_name}.{column_name}"
                    if column_name not in schema.keys():
                        raise MyTransformerError(f"fail to resolve '{column_name.split('.')[1]}'")
                selected_columns.append(column_name)
        return selected_columns
      
    # ORDER BY 구문 처리
    def _perform_order_by(self, schema, records, order_by_clause):
        if order_by_clause:
            table_name = order_by_clause.children[2]
            column_name = order_by_clause.children[3]
            if table_name:
                if table_name.children[0].lower() not in {col.split(".")[0] for col in schema.keys()}:
                    raise OtherError("Order by clause trying to reference tables which are not specified")
                column_name = f"{table_name.children[0].lower()}.{column_name.children[0].lower()}"
                if column_name not in schema.keys():
                    raise OtherError("Order by clause trying to reference non existing column")
            else:
                column_name = column_name.children[0].lower()
                try:
                    column_name = self._check_ambiguous_column(column_name, schema)
                except MyTransformerError as e:
                    raise OtherError(f"Order by {e}")
            
            column_type = schema[column_name]
            column_idx = list(schema.keys()).index(column_name)

            reverse = False
            if order_by_clause.children[4]:
                order_token = order_by_clause.children[4].lower()
                reverse = (order_token == 'desc')

            # 정렬 수행
            if column_type == "int":
                records.sort(key=lambda x: int(x[column_idx]), reverse=reverse)
            else:
                records.sort(key=lambda x: x[column_idx], reverse=reverse)

    # SELECT 쿼리 결과 출력
    def _print_select(self, schema, records, selected_columns):
        
        if selected_columns == ["*"]:
            selected_columns = list(schema.keys())
        
        if not records:
            print("-" * len(schema) * 23)
            print("-" * len(schema) * 23)
            print("0 rows in set")
            return

        # 각 컬럼별 최대 너비 계산 (컬럼명과 값 중 가장 긴 것)
        col_widths = {col: len(col) for col in schema}
        for row in records:
            for col in selected_columns:
                if col in schema:
                    idx = list(schema.keys()).index(col)
                    col_widths[col] = max(col_widths[col], len(row[idx]))

        # 헤더 출력
        header = " | ".join(f"{col:<{col_widths[col]}}" for col in selected_columns)
        print("-" * len(header))
        print(header)

        # 레코드 출력
        for row in records:
            values = []
            for col in selected_columns:
                val = row[list(schema.keys()).index(col)]
                if val.startswith(("'", '"')) and val.endswith(("'", '"')):
                    val = val[1:-1]
                values.append(f"{val:<{col_widths[col]}}")
            print(" | ".join(values))

        print("-" * len(header))
        print(f"{len(records)} rows in set")

    def _get_table(self, table_name, meta_db, txn):
        # 테이블 ID 가져오기
        try:
            table_id = self._get_table_id(table_name, meta_db, txn)
        except MyTransformerError as e:
            raise MyTransformerError("no such table")

        # 테이블 존재 여부 확인
        schema_key = f"__schema__:{table_id}".encode()
        if not meta_db.get(schema_key, txn=txn):
            raise MyTransformerError("Fatal Error: Table ID exist, but schema does not exist.")

        # 테이블 schema 가져오기
        schema_value = meta_db.get(schema_key, txn=txn)
        if not schema_value:
            raise MyTransformerError("Fatal Error: Table ID exist, but schema does not exist.")
        
        # key: cloumn name, value: column type인 dictionary로 변환
        schema_column_types = schema_value.decode().split("|")
        schema_column_types = {col.split(":")[0]: col.split(":")[1] for col in schema_column_types}
        
        # 테이블 데이터베이스 열기
        table_db = db.DB(self.db_env)
        table_db.open(table_id, None, dbtype=db.DB_HASH, txn=txn)

        # 테이블 데이터 가져오기
        cursor = table_db.cursor(txn=txn)
        records = []
        while record := cursor.next():
            records.append(record[1].decode().split("|"))
        cursor.close()
        table_db.close()

        return schema_column_types, records
 

    def show_tables_query(self, items):

        txn = self.db_env.txn_begin()
        try:
            meta_db = db.DB(self.db_env)
            meta_db.open("__meta__", None, db.DB_BTREE, txn=txn)

            # 모든 테이블 이름 가져오기
            cursor = meta_db.cursor(txn=txn)
            table_names = []
            while record := cursor.next():
                key = record[0].decode()
                if key.startswith("__table_id__:"):
                    table_name = key.split(":")[1]
                    table_names.append(table_name)
            cursor.close()

            # 출력
            print(f"-"*22)
            if table_names:
                for table_name in table_names:
                    print(f" {table_name[:20]:<20} ")
            print(f"-"*22)
            print(f"{len(table_names)} rows in set")

            txn.commit()
            meta_db.close()

        except Exception as e:
            txn.abort()
            meta_db.close()
            
            raise
        
    def update_query(self, items):
        prompt()
        print("\'UPDATE\' requested")
        return
    
    def rename_query(self, items):

        txn = self.db_env.txn_begin()
        try:
            meta_db = db.DB(self.db_env)
            meta_db.open("__meta__", None, db.DB_BTREE, txn=txn)

            # 기존 테이블 이름과 새로운 테이블 이름 추출
            old_table_name = items[1].children[0].children[0].children[0].lower()
            new_table_name = items[1].children[0].children[2].children[0].lower()

            # 기존 테이블 존재 여부 확인
            try:
                table_id = self._get_table_id(old_table_name, meta_db, txn)
            except MyTransformerError as e:
                raise MyTransformerError("no such table")

            # 새로운 테이블 이름 중복 확인
            self._check_table_existence(new_table_name, meta_db, txn)
           
            # 기존 테이블 ID 지우기
            old_table_id_key = f"__table_id__:{old_table_name}".encode()
            meta_db.delete(old_table_id_key, txn=txn)

            # 새로운 테이블 ID 저장
            new_table_id_key = f"__table_id__:{new_table_name}".encode()
            meta_db.put(new_table_id_key, table_id.encode(), txn=txn)

           
            # 트랜잭션 커밋
            txn.commit()
            prompt()
            print(f"'{new_table_name}' is renamed")
            meta_db.close()

        except MyTransformerError as e:
            txn.abort()
            meta_db.close()
            raise MyTransformerError(f"Rename table has failed: {e}")
        
        except Exception as e:
            txn.abort()
            meta_db.close()
            raise

    def truncate_query(self, items):

        txn = self.db_env.txn_begin()
        try:
            meta_db = db.DB(self.db_env)
            meta_db.open("__meta__", None, db.DB_BTREE, txn=txn)

            # 테이블 이름 추출
            table_name = items[1].children[0].lower()

            # 테이블 ID 가져오기
            try:
                table_id = self._get_table_id(table_name, meta_db, txn)
            except MyTransformerError as e:
                raise MyTransformerError("no such table")

            # 테이블 존재 여부 확인
            schema_key = f"__schema__:{table_id}".encode()
            if not meta_db.get(schema_key, txn=txn):
                raise MyTransformerError("Fatal Error: Table ID exist, but schema does not exist.")

            # 다른 테이블이 foreign key로 참조하고 있는지 확인
            if self._is_table_referenced(table_id, table_name, meta_db, txn):
                raise MyTransformerError(f"'{table_name}' is referenced by another table")
            
            # 테이블 데이터베이스 열기
            table_db = db.DB(self.db_env)
            table_db.open(table_id, None, dbtype=db.DB_HASH, txn=txn)

            # 테이블 데이터 삭제
            cursor = table_db.cursor(txn=txn)
            while record := cursor.next():
                table_db.delete(record[0], txn=txn)
            cursor.close()

            # 트랜잭션 커밋
            txn.commit()
            prompt()
            print(f"'{table_name}' is truncated")
            table_db.close()
            meta_db.close()

        except MyTransformerError as e:
            txn.abort()
            meta_db.close()
            raise MyTransformerError(f"Truncate table has failed: {e}")
        
        except Exception as e:
            txn.abort()
            meta_db.close()
            raise

    def EXIT(self, items):
        self.db_env.close()
        exit()
    



def main():

    #Lark parser 생성
    
    with open('grammar.lark') as file:
        sql_parser = lark.Lark(file.read(), start="command", lexer="basic")

    sql_transformer = MyTransformer()

    while True:

        query=get_query()

        try:
            # 전체 쿼리를 한 번에 파싱
            output = sql_parser.parse(query)
            sql_transformer.transform(output)

        except lark.exceptions.UnexpectedInput as e:
            try:
                valid_query=get_valid_query(query, e)
                
                if valid_query != " ; ":
                    # valid_query가 존재하는 경우에만 실행
                    # 오류 이전의 query만 실행
                    output = sql_parser.parse(valid_query)
                    sql_transformer.transform(output)       
                    
                # 오류가 발생한 query에 대한 error message 출력
                prompt()
                print("Syntax Error")
                
            except lark.exceptions.LarkError as e:     
                # 오류가 발생한 query에 대한 error message 출력
                prompt()
                print(e.__context__)                
            except Exception as e:
                # 기타 에러 처리
                prompt()
                print(f"Error: {e}")

        except lark.exceptions.LarkError as e:     
            # 오류가 발생한 query에 대한 error message 출력
            prompt()
            print(e.__context__)
        
        
        except Exception as e:
            # 기타 에러 처리
            prompt()
            print(f"Error: {e}")



main()

