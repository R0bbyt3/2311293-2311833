/* ===========================================================
 * DiceRoll — representa uma jogada de dois dados (1..6).
 * =========================================================== */

package model;

import java.util.concurrent.ThreadLocalRandom;


final class DiceRoll {

    private final int d1;
    private final int d2;
    private final int sum;
    private final boolean isDouble;

    DiceRoll() {
        this.d1 = ThreadLocalRandom.current().nextInt(1, 7);
        this.d2 = ThreadLocalRandom.current().nextInt(1, 7);
        this.sum = d1 + d2;
        this.isDouble = (d1 == d2);
    }

    // Getters imutáveis
    int d1() { return d1; }
    int d2() { return d2; }
    int sum() { return sum; }
    boolean isDouble() { return isDouble; }

    @Override
    public String toString() {
        return "DiceRoll{d1=" + d1 + ", d2=" + d2 + ", sum=" + sum + ", double=" + isDouble + "}";
    }
}
