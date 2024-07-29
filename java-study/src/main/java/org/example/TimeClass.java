package org.example;

import java.util.Calendar;

public class TimeClass {
    // java.util.Date
    // 날짜와 시간을 다를 때 쓰는 클래스로, 대부분 deprecated.

    // java.util.Calendar
    // Date 클래스를 개선한 클래스

    // java.time
    // Date, Calendar의 단점을 개선한 새로운 클래스를 제공

    public void printCalendarTime() {
        Calendar c = Calendar.getInstance();

        int year = c.get(Calendar.YEAR);
        System.out.println("year = " + year);
        int day = c.get(Calendar.DATE);
        System.out.println("day = " + day);
        int time = c.get(Calendar.HOUR);
        System.out.println("time = " + time);

        int lastDayOfMonth = c.getActualMaximum(Calendar.DATE);
        System.out.println("lastDayOfMonth = " + lastDayOfMonth);
    }
}
