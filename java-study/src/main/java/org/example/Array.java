package org.example;

public class Array {
    public void createRandomArray() {
        int[] arr = new int[45];

        for (int i = 0; i < 45; i++) {
            arr[i] = (int) (Math.random() * 45);
            System.out.println("i = " + arr[i]);
        }
    }

    public void twoDimensionArray() {
        int[][] score = new int[3][];
        score[0] = new int[1];
        score[1] = new int[2];
        score[2] = new int[3];
    }
}
