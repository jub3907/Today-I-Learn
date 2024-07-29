package org.example;

import java.text.DecimalFormat;

public class Formatting {
    double number = 1234.567;

    public void printNumberFormat() {
        DecimalFormat df = new DecimalFormat("#.#");
        String result = df.format(number);
        System.out.println("result = " + result);

        DecimalFormat df2 = new DecimalFormat("#.###");
        String result2 = df2.format(number);
        System.out.println("result2 = " + result2);


        DecimalFormat df3 = new DecimalFormat("#,###.##");
        try {
            Number num = df3.parse("1,234,567.89");
            System.out.println("num = " + num);
        } catch (Exception e) {

        }


    }

}
