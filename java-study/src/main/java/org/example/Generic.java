package org.example;

public class Generic<T> {
    T item;

    void setItem(T item) {
        this.item = item;
    }

    T getItem() {
        return this.item;
    }
}
