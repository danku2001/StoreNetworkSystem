package client.ui;

import java.util.Scanner;

public class ConsoleUI {
    private final Scanner s = new Scanner(System.in);

    public String prompt(String text) {
        System.out.print(text);
        return s.nextLine().trim();
    }

    public void println(String text) {
        System.out.println(text);
    }
}
