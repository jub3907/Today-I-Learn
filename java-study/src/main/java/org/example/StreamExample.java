package org.example;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamExample {

    public Stream<String> stream1() {
        String[] arr = new String[]{"a", "b", "c"};
        Stream<String> stream = Arrays.stream(arr);

        return stream;
    }

    public Stream<String> stream2() {
        List<String> list = Arrays.asList("a", "b", "c");
        Stream<String> stream = list.stream();

        return stream;
    }

    public Stream<String> stream3() {
        Stream<String> builderStream = Stream.<String>builder()
                .add("a").add("b").add("c")
                .build();

        return builderStream;
    }

    List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

    // filter : 스트림 내 요소를 하나씩 평가해, 조건에 맞는 요소만 걸러낸다.
    public List<Integer> filterStream1() {
        Stream<Integer> integerStream = list.stream()
                .filter(item -> item > 3);

        return integerStream.collect(Collectors.toList());
    }

    // map : 스트림내 요소를 하나씩 특정 값으로 변환
    public List<String> filterStream2() {
        Stream<String> integerStream = list.stream()
                .map(item -> item + 10)
                .map(Object::toString);

        return integerStream.collect(Collectors.toList());
    }

    // distinct : 중복 제거
    // limt : 최대 크기 제한
    // skip : 앞에서부터 n개 스킵
    // sorted : 오름차순 정렬, Comparator.reverseOrder를 사용해 내림차순 정렬 가능
    public List<Integer> filterStream3() {
        List<Integer> list = Arrays.asList(1, 1, 2, 2, 2, 3, 4, 5, 5, 0);

        Stream<Integer> limit = list.stream()
                .distinct() // 1, 2, 3, 4, 5, 0
                .skip(2) // 3, 4, 5, 0
                .sorted(Comparator.reverseOrder()) // 5, 4, 3, 0
                .limit(3); // 5, 4, 3

        return limit.collect(Collectors.toList());
    }

    // count, min,
    public long getStreamCount() {
        return list.stream().count();

    }

    public Integer getReductionResult() {
        return list.stream()
                .reduce(100, Integer::sum);
    }

    public void printList() {
        list.stream().forEach(System.out::println);
    }



}
