import java.util.InputMismatchException;
import java.util.Scanner;

public class division {
    public static void main(String[] args) {
        try{
            Scanner scanner = new Scanner((System.in));
            System.out.printf("Enter the First number:");
            int num1 = scanner.nextInt();
            scanner.nextLine();
            System.out.printf("Enter the second number:");
            int num2 = scanner.nextInt();
            int num3 = num1/num2;
            System.out.println("Result: "+num1+" / "+num2+" = "+num3);
        }
        catch(ArithmeticException a){
            System.out.printf("Error: Division by zero is not allowed.");
        }
        catch (InputMismatchException b){
            System.out.printf("Error: Please enter valid integers.");
        }
    }
}