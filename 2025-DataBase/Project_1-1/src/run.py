import lark

# prompt 출력 함수
def prompt():
    print("DB_2023-19674> ", end="")
    return

# 입력된 query가 ;으로 끝나는지 확인하는 함수. 
# 뒤쪽의 white space는 무시함.
def is_complete_query(query):
    if query.strip()[-1] == ";":
        return True
    else:
        return False
    
# query 입력받는 함수
def get_query():
    prompt()
    query = input()

    # '\n'이 포함된 query를 처리
    while  not is_complete_query(query):
        query += (" " + input())
    return query

# query에서 오류가 발생하기 전 query 까지만 리턴
def get_valid_query(query, e):
    # 오류 위치를 기준으로 쿼리를 자름
    error_position = e.pos_in_stream  # 오류가 발생한 위치
    valid_query = " ; ".join(query[:error_position].split(";")[:-1])+" ; "  # 오류 이전까지의 쿼리

    return valid_query

    
# 밑의 함수는 불필요해짐.
# 어차피 예약어는 입력이 되면 lark가 그 부분을 IDENTIFIER로 인식하지 않아서 Syntax error가 발생함.
# # 입력된 단어가 SQL 예약어인지 확인하는 함수. 
# # grammar_skeleton.lark를 기준으로 예약어 목록 만듦.
# def is_reserved_word(word):
#     reserved_words = [
#     "int", "char", "date", "exit", "create", "drop", "desc", "show", "table", "tables", "not", "null", 
#     "primary", "foreign", "key", "references", "select", "from", "where", "as", "is", "or", "and", 
#     "insert", "into", "values", "delete", "explain", "describe", "to", "set", "truncate", "rename", "update"
#     ]
   
#     return word in reserved_words


# lark의 Transformer 클래스는 tree 형태로 파싱된 결과를 leaf부터 하나씩 읽으며 token의 이름과 동일한 이름의 함수를 호출해줌
class MyTransformer(lark.Transformer):

    # 입력된 query의 파싱 결과에 따라 처리
    def create_table_query(self, items):
        prompt()
        print("\'CREATE TABLE\' requested")
        return
    def drop_table_query(self, items):
        prompt()
        print("\'DROP TABLE\' requested")
        return
    def explain_query(self, items):
        prompt()
        print("\'EXPLAIN\' requested")
        return
    def describe_query(self, items):
        prompt()
        print("\'DESCRIBE\' requested")
        return
    def desc_query(self, items):
        prompt()
        print("\'DESC\' requested")
        return
    def insert_query(self, items):
        prompt()
        print("\'INSERT\' requested")
        return
    def delete_query(self, items):
        prompt()
        print("\'DELETE\' requested")
        return
    def select_query(self, items):
        prompt()
        print("\'SELECT\' requested")
        return
    def show_tables_query(self, items):
        prompt()
        print("\'SHOW TABLE\' requested")
        return
    def update_query(self, items):
        prompt()
        print("\'UPDATE\' requested")
        return
    def rename_query(self, items):
        prompt()
        print("\'RENAME\' requested")
        return
    def truncate_query(self, items):
        prompt()
        print("\'TRUNCATE\' requested")
        return
    def EXIT(self, items):
        exit()
        return
    
    # 밑의 함수는 불필요해짐.
    # 어차피 예약어는 입력이 되면 lark가 그 부분을 IDENTIFIER로 인식하지 않아서 Syntax error가 발생함.
    #사용자 정의 식별자가 SQL 예약어를 사용하면 에러
    # def column_name(self, items):
    #     if is_reserved_word(items[0].lower()):
    #         raise lark.LarkError
    #     return
    # def table_name(self, items):
    #     if is_reserved_word(items[0].lower()):
    #         raise lark.LarkError
    #     return 



def main():

    #Lark parser 생성
    
    with open('grammar.lark') as file:
        sql_parser = lark.Lark(file.read(), start="command", lexer="basic")

    while True:

        query=get_query()

        try:
            # 전체 쿼리를 한 번에 파싱
            output = sql_parser.parse(query)
            MyTransformer().transform(output)

        except lark.exceptions.UnexpectedInput as e:
            
            valid_query=get_valid_query(query, e)
            
            try:
                # 오류 이전의 query만 실행
                output = sql_parser.parse(valid_query)
                MyTransformer().transform(output)

                # 오류가 발생한 query에 대한 Syntax error 출력
                prompt()
                print("Syntax error")

            # valid_query도 에러가 발생하는 경우
            # ex) 첫 번째 query가 에러가 발생하는 경우 valid_query가 ' ; '이므로 에러 발생
            except lark.LarkError as e:
                
                # Syntax error 출력
                prompt()
                print("Syntax error")
            
            
            
              
    return


main()

