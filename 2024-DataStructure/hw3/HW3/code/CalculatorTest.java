import java.io.*;
import java.util.Stack;

public class CalculatorTest
{
	public static void main(String args[])
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		while (true)
		{
			try
			{
				String input = br.readLine();
				if (input.compareTo("q") == 0||input.compareTo("Q")==0)
					break;

				command(input);
			}
			catch (Exception e)
			{
				System.out.println("입력이 잘못되었습니다. 오류 : " + e.toString());
			}
		}
	}

	private static void command(String input)
	{
		try {
			String postfixInput = infix2postfix(input);

			long result = calculate(postfixInput);

			System.out.println(postfixInput);
			System.out.println(result);
		}
		catch (Exception e)
		{
			System.out.println("ERROR");
		}

	}

	private static String infix2postfix(String input) throws Exception{
		String postfix=new String();
		Stack<Character> operatorStack=new Stack<>();
		boolean digitPreviously = false;
		boolean spacePreviously= false;
		int brackercnt = 0;

		for(int i=0;i<input.length();i++){

			char ch = input.charAt(i);
			if(ch==' '||ch=='\t') {
				spacePreviously=true;
				continue;
			}
			if(Character.isDigit(ch)){
				if (digitPreviously){
					if (spacePreviously) throw new Exception();
					postfix=postfix+ch;
				}
				else {
					postfix=postfix+" "+ch;
					digitPreviously=true;
				}
				spacePreviously=false;
			}
			else if(isOperator(ch)){
				if(digitPreviously==false) {
					if(ch!='-') throw new Exception();
					ch='~';
					operatorStack.push(ch);
					continue;
				}
				if(operatorStack.isEmpty()){
					operatorStack.push(ch);
				}else {
					Character prevOp = operatorStack.peek();
					if (operatorPriority(ch)>operatorPriority(prevOp)) {
						operatorStack.push(ch);
					}
					else if(operatorPriority(ch)==operatorPriority(prevOp)){
						if (ch == '^') {
							operatorStack.push(ch);
						}else{
							postfix=postfix+" "+operatorStack.pop();
							operatorStack.push(ch);
						}
					}else {
						while (!operatorStack.isEmpty()&&operatorPriority(operatorStack.peek())>=operatorPriority(ch)) {
							postfix=postfix+" "+operatorStack.pop();
						}
						operatorStack.push(ch);
					}
				}
				digitPreviously = false;
				spacePreviously=false;
			}
			else if(ch=='('){
				if(digitPreviously) throw new Exception();
				brackercnt+=1;
				operatorStack.push(ch);
				digitPreviously=false;
				spacePreviously=false;
			}
			else if(ch==')'){
				if(!digitPreviously) throw new Exception();
				brackercnt-=1;
				if(brackercnt<0) throw new Exception();
				while (operatorStack.peek()!='(') {
					postfix=postfix+" "+operatorStack.pop();
				}
				operatorStack.pop();
				spacePreviously=false;
			}
			else throw new Exception();

		}

		if(brackercnt!=0) throw new Exception();

		while (!operatorStack.isEmpty()) {
			postfix=postfix+" "+operatorStack.pop();
		}

		return postfix.substring(1);
	}

	private static long calculate(String input) throws Exception{
		long result=0;
		String[] inputArray=input.split(" ");
		Stack<String> calculateStack=new Stack<>();
		for(int i=0;i<inputArray.length;i++){
			calculateStack.push(inputArray[i]);
			if(isOperator(calculateStack.peek().charAt(0))||calculateStack.peek().charAt(0)=='~'){
				char op=calculateStack.pop().charAt(0);
				long b= Long.parseLong(calculateStack.pop());
				long a = b;
				if(!calculateStack.isEmpty()&&op!='~') {
					a = Long.parseLong(calculateStack.pop());
				}
				calculateStack.push(Long.toString(operation(a,b,op)));
			}
			result=Long.parseLong(calculateStack.peek());
		}
		return result;
	}

	private static long operation(long a, long b, char op) throws Exception{
		long result = 0;
		switch (op){
			case '+':
				result=a+b;
				break;
			case '-':
				result=a-b;
				break;
			case '*':
				result=a*b;
				break;
			case '/':
				if(b==0) throw new Exception();
				result=a/b;
				break;
			case '%':
				if(b==0) throw new Exception();
				result=a%b;
				break;
			case '^':
				if(a==0&&b<0) throw new Exception();
				result=(long)Math.pow(a,b);
				break;
			case '~':
				result=-a;
				break;

		}
		return result;
	}

	private static boolean isOperator(Character ch){
		return ch=='+'||ch=='-'||ch=='/'||ch=='*'||ch=='%'||ch=='^';
	}

	private static int operatorPriority(Character op){
		int priority=0;
		switch (op){
			case '(':
				priority=-1;
				break;
			case '+':

			case '-':
				priority= 0;
				break;
			case '*':

			case '/':

			case '%':
				priority= 1;
				break;
			case '~':
				priority= 2;
				break;
			case '^':
				priority= 3;
				break;


		}
		return priority;
	}
}

