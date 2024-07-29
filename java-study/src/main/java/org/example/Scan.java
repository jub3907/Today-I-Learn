package org.example;

import java.util.Scanner;

public class Scan {
    public void scanner() {
        Scanner scanner = new Scanner(System.in);

        int num = scanner.nextInt();
        System.out.println("num = " + num);

        String input = scanner.nextLine();
        String input2 = scanner.nextLine();
        System.out.println("input = " + input);
        System.out.println("input2 = " + input2);
    }
}
