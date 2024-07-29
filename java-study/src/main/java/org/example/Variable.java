package org.example;

public class Variable {
    //
    // 기본형 변수 타입
    // char
    private char aChar = 'a';

    // byte, short, int, long
    private byte aByte = 0;
    private short aShort = 0;
    private int anInt = 0;
    private long aLong = 0;

    // float, double
    private float aFloat = 0;
    private double aDouble = 0;

    // boolean
    private boolean aBoolean = true;

    // 형변환
    public void changeType() {
        anInt = (int) aChar;
        System.out.println("anInt = " + anInt);
    }

    public void printCharVariable() {
        aChar = 'b';

        System.out.println("aChar = " + aChar);
    }

    public void printCharVariable2() {
        aChar = 'c';

        System.out.println("aChar = " + aChar);
    }

}
