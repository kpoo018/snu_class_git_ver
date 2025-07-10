import java.awt .*;
public class Frame1 {
    public static void main(String [] args) {
        Frame myFrame = new Frame();
        myFrame.setTitle("Frame Test");
        myFrame.setSize(350, 350);
        myFrame.setVisible(true);
        myFrame.setLayout(null);
        Label myLabel = new Label("Welcome to Java");
        myLabel.setSize(140, 30);
        myLabel.setLocation(120, 100);
        myFrame.add(myLabel);
    }
}