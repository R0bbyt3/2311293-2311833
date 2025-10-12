package model;

import static org.junit.Assert.*;

import org.junit.Test;

public class BankTest {

    @Test
    public void transfer_PlayerToBank_debitsPlayer() {
        Bank bank = new Bank(1_000);
        Player p = new Player("p1", "Alice", "RED", 200);

        bank.transfer(p, null, 150);

        assertEquals(50, p.getMoney());
    }

    @Test
    public void transfer_BankToPlayer_creditsPlayer() {
        Bank bank = new Bank(1_000);
        Player p = new Player("p1", "Alice", "RED", 0);

        bank.transfer(null, p, 300);

        assertEquals(300, p.getMoney());
    }

    @Test
    public void transfer_PlayerToPlayer_movesMoneyBetweenPlayers() {
        Bank bank = new Bank(1_000);
        Player a = new Player("a", "Alice", "RED", 500);
        Player b = new Player("b", "Bob", "BLUE", 100);

        bank.transfer(a, b, 200);

        assertEquals(300, a.getMoney());
        assertEquals(300, b.getMoney());
    }

    @Test(expected = IllegalArgumentException.class)
    public void transfer_InvalidBothNull_throws() {
        Bank bank = new Bank(1_000);
        bank.transfer(null, null, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void transfer_NegativeAmount_throws() {
        Bank bank = new Bank(1_000);
        Player p = new Player("p1", "Alice", "RED", 100);
        bank.transfer(p, null, -1);
    }
}