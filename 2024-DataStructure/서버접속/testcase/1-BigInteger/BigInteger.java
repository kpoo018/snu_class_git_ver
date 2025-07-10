import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
  
  
public class BigInteger
{
    public static final String QUIT_COMMAND = "quit";
    public static final String MSG_INVALID_INPUT = "Wrong Input";
  
    // implement this

    private char sign = '+';
    private char[] num = new char[0];


    public char getSign(){
        return this.sign;
    }
    public void setSign(char c){
        this.sign = c;
    }
    public char[] getNum(){
        return this.num;
    }
    public void setNum(char[] chars){
        this.num = chars;
    }
    public BigInteger(int i)
    {
    }
  
    public BigInteger(int[] num1)
    {
    }
  
    public BigInteger(String s)
    {
        if(s.toCharArray()[0] == '-'){
            this.sign='-';
            this.num = new char[s.length()-1];
            for(int i=0; i<s.length()-1;i++){
                this.num[i]=s.toCharArray()[i+1];
            }
        }
        else this.num = s.toCharArray();

    }
  
    public BigInteger add(BigInteger big)
    {
        if(this.getSign()==big.getSign()){
            BigInteger result=new BigInteger("1");
            result.setSign(this.getSign());

            char[] largernum=(this.getNum().length>big.getNum().length?this.getNum():big.getNum());
            char[] smallernum=(this.getNum().length>big.getNum().length?big.getNum():this.getNum());

            char[] resultNums = new char[largernum.length+1];
            int sel = 0;
            int sum;
            for(int i=0;i<smallernum.length;i++){
                sum=largernum[largernum.length-1-i]-'0'+smallernum[smallernum.length-1-i]-'0'+sel;
                if(sum<10) {
                    sel = 0;
                    resultNums[resultNums.length-1 - i] = (char) (sum +'0');
                } else {
                    sel = 1;
                    resultNums[resultNums.length-1 - i] = (char) (sum +'0' - 10);
                }
            }
            for(int i=smallernum.length;i<largernum.length;i++){
                sum=largernum[largernum.length-1-i]-'0'+sel;
                if(sum<10) {
                    sel = 0;
                    resultNums[resultNums.length -1- i] = (char) (sum +'0');
                } else {
                    sel = 1;
                    resultNums[resultNums.length-1 - i] = (char) (sum +'0' - 10);
                }
            }
            if(sel == 1) {
                resultNums[0] = '1';
                result.setNum(resultNums);
            }
            else {
                char[] realResultNums=new char[resultNums.length-1];
                for(int i=0;i<resultNums.length-1;i++){
                    realResultNums[i]=resultNums[i+1];
                }
                result.setNum(realResultNums);
            }

            return result;



        } else {
            BigInteger result=new BigInteger("1");

            char[] largernum=(this.getNum().length>big.getNum().length ? this.getNum() : big.getNum());
            char largernumSign = (this.getNum().length>big.getNum().length ? this.getSign() : big.getSign());
            char[] smallernum=(this.getNum().length>big.getNum().length ? big.getNum() : this.getNum());
            char smallernumSign = (this.getNum().length>big.getNum().length ? big.getSign() :this.getSign() );
            if(largernum.length==smallernum.length){
                for(int i=0; i<largernum.length ; i++){
                    if(largernum[i]==smallernum[i]) continue;
                    if(largernum[i]>smallernum[i]) break;
                    if(largernum[i]<smallernum[i]){
                        char[] temp = largernum;
                        char tempSign = largernumSign;
                        largernum=smallernum;
                        smallernum=temp;
                        largernumSign=smallernumSign;
                        smallernumSign=tempSign;
                        break;
                    }
                }
            }

            result.setSign(largernumSign);

            char[] resultNums = new char[largernum.length];
            int sel = 0;
            int ans;
            for(int i=0;i<smallernum.length;i++){
                ans=(largernum[largernum.length-1-i]-'0') - sel -(smallernum[smallernum.length-1-i]-'0');
                if(ans>=0) {
                    sel = 0;
                    resultNums[resultNums.length-1 - i] = (char) (ans +'0');
                } else {
                    sel = 1;
                    resultNums[resultNums.length-1 - i] = (char) (10 + ans +'0' );
                }
            }
            for(int i=smallernum.length;i<largernum.length;i++){
                ans=largernum[largernum.length-1-i]-'0' - sel;
                if(ans>=0) {
                    sel = 0;
                    resultNums[resultNums.length -1- i] = (char) (ans +'0');
                } else {
                    sel = 1;
                    resultNums[resultNums.length-1 - i] = (char) (10+ans +'0');
                }
            }

            int n=0;
            for(int i=0;i<resultNums.length;i++){
                if(resultNums[i]!='0'){
                    n=resultNums.length-i;
                    break;
                }
            }
            char[] realResultNums=new char[n];
            for(int i=0;i<realResultNums.length;i++){
                realResultNums[i]=resultNums[resultNums.length-n+i];
            }
            result.setNum(realResultNums);
            return result;
        }

    }
  
    public BigInteger subtract(BigInteger big)
    {
        BigInteger newBig=big;
        if(newBig.getSign()=='+'){
            newBig.setSign('-');
        } else {
            newBig.setSign('+');
        }
        return this.add(newBig);

    }
  
    public BigInteger multiply(BigInteger big)
    {
        BigInteger result=new BigInteger("0");
        char sign = (this.getSign()==big.getSign()?'+':'-');

        char[] num2= big.getNum();

        this.setSign('+');
        big.setSign('+');

        BigInteger temp=this;
        for(int i =0; i<num2.length;i++){
            for(int j=0;j<(num2[num2.length-1-i]-'0');j++){
               result=result.add(temp);
            }
            temp.setNum((temp+"0").toCharArray());
        }

        result.setSign(sign);

        return result;
    }
  
    @Override
    public String toString()
    {
        String result;

        if(this.sign=='-'){
            char[] resultArray = new char[this.getNum().length + 1];
            resultArray[0]='-';
            for(int i=1; i<this.getNum().length + 1;i++){
                resultArray[i]=this.getNum()[i-1];
            }
            result = new String(resultArray);
        }else{
            result = new String(this.getNum());
        }
        return result;
    }
  
    static BigInteger evaluate(String input) throws IllegalArgumentException
    {
        // implement here
        // parse input
        // using regex is allowed


        String newInput = input.replaceAll(" ","");
        String operator = newInput;
        String[] arg=new String[2];

        Pattern pattern = Pattern.compile("([-]?[0-9]+)([-|+|*])([-]?[0-9]+)");
        Matcher m = pattern.matcher(newInput);

        while (m.find()) {
            arg[0] = m.group(1);
            operator = m.group(2);
            arg[1] = m.group(3);
        }


        BigInteger num1 = new BigInteger(arg[0]);
        BigInteger num2 = new BigInteger(arg[1]);
        BigInteger result = new BigInteger(0);

        switch (operator){
            case "+":
                result = num1.add(num2);
                break;
            case "-":
                result = num1.subtract(num2);
                break;
            case "*":
                result = num1.multiply(num2);
                break;
        }

        return result;

  
        // One possible implementation
        // BigInteger num1 = new BigInteger(arg1);
        // BigInteger num2 = new BigInteger(arg2);
        // BigInteger result = num1.add(num2);
        // return result;
    }
  
    public static void main(String[] args) throws Exception
    {
        try (InputStreamReader isr = new InputStreamReader(System.in))
        {
            try (BufferedReader reader = new BufferedReader(isr))
            {
                boolean done = false;
                while (!done)
                {
                    String input = reader.readLine();
  
                    try
                    {
                        done = processInput(input);
                    }
                    catch (IllegalArgumentException e)
                    {
                        System.err.println(MSG_INVALID_INPUT);
                    }
                }
            }
        }
    }
  
    static boolean processInput(String input) throws IllegalArgumentException
    {
        boolean quit = isQuitCmd(input);
  
        if (quit)
        {
            return true;
        }
        else
        {
            BigInteger result = evaluate(input);
            System.out.println(result.toString());
  
            return false;
        }
    }
  
    static boolean isQuitCmd(String input)
    {
        return input.equalsIgnoreCase(QUIT_COMMAND);
    }
}
