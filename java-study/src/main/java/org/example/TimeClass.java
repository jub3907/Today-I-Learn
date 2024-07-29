package org.example;

import java.time.LocalDate;
import java.time.LocalTime;
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

    public void printDateTime() {
        // LocalDate : 날짜
        // LocalTime : 시간
        // LocalDateTime : 날짜 + 시간
        // ZonedDateTime : 시간대(time-zone) + LocalDateTime
        // 날짜와 날짜 사이엔 period, 시간과 시간 사이엔 Duration.

        // Temporal : 날짜와 시간을 표현하는 클래스가 구현됨
        // TemporalAmount : 날짜와 시간의 "차이"를 표현하는 클래스가 구현됨.

        LocalDate today = LocalDate.now();
        System.out.println("today = " + today);
        LocalTime now = LocalTime.now();
        System.out.println("now = " + now);

        LocalDate birth = LocalDate.of(1996, 3, 12);
        System.out.println("birth = " + birth);
        LocalTime birthTime = LocalTime.of(2, 0, 0);
        System.out.println("birthTime = " + birthTime);

        boolean todayIsAfter = today.isAfter(birth);
        System.out.println("todayIsAfter = " + todayIsAfter);

        int compare = today.compareTo(birth);
        System.out.println("compare = " + compare);

    }
}
