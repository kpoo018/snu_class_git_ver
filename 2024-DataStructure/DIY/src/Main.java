//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) {
        String input = "   100    *-  200  ";
        String newInput = input.replaceAll(" ","");
        String operator = newInput;
        String[] num=new String[2];

        Pattern pattern = Pattern.compile("[-]?[0-9]+");
        Matcher m = pattern.matcher(newInput);

        int i=0;
        while (m.find()){
            operator=operator.replaceAll(m.group(),"");
            num[i]=m.group();
//            System.out.println(m.group());
            i++;
        }
        if(operator.isEmpty()){
            operator = "+";
        }

//        System.out.println(newInput);
//        System.out.println(operator);
//        System.out.println(num[0]);
//        System.out.println(num[1]);
//        String num1 = newInput.replaceAll("[-|+|*][-]?[0-9]+","");
//        String num2 = newInput.replaceAll("[-]?[0-9]+[-|+|*]","");
//
//        System.out.println(num1);
//        System.out.println(num2);
//
//
//        for(int i=0; i< splits.length;i++){
//            System.out.println(splits[i]);
//        }
    }
}