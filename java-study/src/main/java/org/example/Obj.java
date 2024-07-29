package org.example;

public class Obj {
    String a = "123";
    String b = "123";

    public boolean eq() {
        return a.equals(b);
    }

    public void getClassInfo() throws ClassNotFoundException {
        Car c = new Car();

        Class cObj = c.getClass();
        Class cObj2 = Car.class;

        String className = cObj.getName();

//        Class cObj3 = Class.forName("Car");

        System.out.println("cObj = " + cObj);
        System.out.println("cObj2 = " + cObj2);
//        System.out.println("cObj3 = " + cObj3);
        System.out.println("className = " + className);


    }
}
