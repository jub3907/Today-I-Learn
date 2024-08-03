package org.example;

import java.util.Optional;

public class OptionalExample {

    String x = null;

    public String getOfNullableResult() {
        Optional<String> optional = Optional.ofNullable(x);

        return optional.orElse("is Null");
    }
}
