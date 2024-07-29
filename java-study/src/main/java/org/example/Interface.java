package org.example;


interface Movable {

    void move(int x, int y);
}

interface Attackable {
    void attack(int x, int y);
}

interface Fightable extends Movable, Attackable{
};

class Fighter implements Fightable {
    int x = 0;
    int y = 0;

    @Override
    public void move(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void attack(int x, int y) {
        this.x = 0;
        this.y = 0;
    }
}



