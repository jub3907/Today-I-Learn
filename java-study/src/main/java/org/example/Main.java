package org.example;

// https://github.com/castello/javajungsuk_basic/blob/master/javajungsuk_basic_%EC%9A%94%EC%95%BD%EC%A7%91.pdf
// java의 정석을 사용한 독학

import java.sql.Time;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // variable
/*        System.out.println("Hello world!");
        Variable v = new Variable();
        v.printCharVariable();
        v.changeType();*/

        // scanner
/*        Scan scan = new Scan();
        scan.scanner();*/

        // operator
//        int age = 20;
//        System.out.println(age == 3? "3" : "4");

        //array
/*        Array array = new Array();
        array.createRandomArray();*/

        // parent, child
/*        Car car = new Car();
        Car car2 = new Ambulance();

        Ambulance ambulance = new Ambulance();
        // 자식 타입을 부모 타입으로 다운캐스팅 하는건 불가능
        //Ambulance ambulance2 = new Car();

        if (ambulance instanceof Ambulance) {
            System.out.println("ambulance is instance of Ambulance");
        }

        if (ambulance instanceof Car) {
            System.out.println("ambulance is instance of Car");
        }

        if (car instanceof Ambulance) {
            System.out.println("car is instance of Ambulance");
        }

        if (car2 instanceof Ambulance) {
            System.out.println("car2 is instance of Ambulance");
        }*/

        // abstract
        // Player 클래스는 추상 클래스이므로, 기본 생성 불가
        // Player player = new Player();
/*        PlayerExtend player = new PlayerExtend();
        player.play(3);*/

        //Object
/*        Obj o = new Obj();
        try {
            o.getClassInfo();
        } catch (Exception e) {
            System.out.println("e = " + e);
        }*/

        //String
/*        int i = 100;
        String str1 = i + "";
        String str2 = String.valueOf(i);

        int j = Integer.parseInt("100");
        int j2 = Integer.valueOf("100");
        char c = "A".charAt(0);*/

        //Time
/*        TimeClass t = new TimeClass();
//        t.printCalendarTime();
        t.printDateTime();*/

        // formatiing
/*        Formatting f = new Formatting();
        f.printNumberFormat();*/

        // generic
//        Generic<String> box = new Generic<String>();
//        box.setItem("box");
//        String b = box.getItem();
//        System.out.println("b = " + b);


        // enum
//        Kind kind = Kind.CLOVER;
//        if (kind == Kind.CLOVER) {
//            System.out.println("kind is clover ");
//            System.out.println("kind.name() = " + kind.name());
//        }
//    }
//    enum Kind {CLOVER, HEART, DIAMOND, SPADE}
//
//    enum Direction {
//        EAST(1), SOUTH(2), WEST(-1);
//
//        private final int value;
//        Direction(int value) {
//            this.value = value;
//        }
//
//        public int getValue() {
//            return value;
//        }

        StreamExample se = new StreamExample();
        List<Integer> integers = se.filterStream1();
        System.out.println("integers = " + integers);

        List<String> strings = se.filterStream2();
        System.out.println("strings = " + strings);

        List<Integer> integers1 = se.filterStream3();
        System.out.println("integers1 = " + integers1);

        Integer reductionResult = se.getReductionResult();
        System.out.println("reductionResult = " + reductionResult);

        se.printList();


    }


}