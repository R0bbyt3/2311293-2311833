package model;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.Before;


public class DiceRollTest {
    private DiceRoll roll;

    @Before
    public void setUp() {
        roll = new DiceRoll(); // injeta seed se houver suporte
    }

    @Test(timeout = 500)
    public void shouldSumDiceCorrectly() {
        DiceRoll r = new DiceRoll(3, 4); // usar construtor de teste, stub ou setter
        assertEquals("Sum of d1 + d2 must equal getSum", 7, r.getSum());
    }

    @Test(timeout = 500)
    public void shouldDetectDoubleOnlyWhenEqual() {
        DiceRoll r1 = new DiceRoll(5, 5);
        assertTrue("isDouble must be true only when d1 == d2", r1.isDouble());

        DiceRoll r2 = new DiceRoll(5, 4);
        assertFalse("isDouble must be false when d1 != d2", r2.isDouble());
    }

    @Test(timeout = 500)
    public void shouldKeepEachDieBetween1And6() {
        DiceRoll r = new DiceRoll();
        assertTrue("d1 must be between 1 and 6", r.getD1() >= 1 && r.getD1() <= 6);
        assertTrue("d2 must be between 1 and 6", r.getD2() >= 1 && r.getD2() <= 6);
    }

    @Test(timeout = 500)
    public void shouldProduceValidIntegerValues() {
        DiceRoll r = new DiceRoll();
        assertEquals("d1 must be integer", 0, r.getD1() % 1);
        assertEquals("d2 must be integer", 0, r.getD2() % 1);
    }

    @Test(timeout = 500)
    public void shouldBeReproducibleWithSameSeedWhenSupported() {
        DiceRoll r1 = new DiceRoll(123L);
        DiceRoll r2 = new DiceRoll(123L);
        assertEquals("Sequence must repeat with the same seed", r1.getSum(), r2.getSum());
    }
}