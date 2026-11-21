package ASCEND;
import javax.swing.*;

public class Menu {
    public static void main(String[] args) {
        JFrame samting = new JFrame();
        samting.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel Intro = new JLabel("Welcome To ASCEND");
        System.out.println("Welcome To ASCEND");
        samting.setSize(800,800);
        samting.getContentPane().setBackground(java.awt.Color.BLACK);
        samting.setVisible(true);

        Intro.setBackground(java.awt.Color.BLACK);
        Intro.setVisible(true);
        Intro.setSize(800,800);
        
    }
}
