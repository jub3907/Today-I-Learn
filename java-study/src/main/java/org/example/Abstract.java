package org.example;

abstract class Player {

    int currentPosition;

    Player() {
        currentPosition = 0;
    }

    abstract void play(int pos);

    abstract void stop();

    void play() {
        play(currentPosition);
    }
}

class PlayerExtend extends Player {

    void play(int pos) {
        this.currentPosition = pos;
    }

    void stop() {
        this.currentPosition = 0;
    }
}
