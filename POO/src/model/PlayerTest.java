package model;

import static org.junit.Assert.*;

import org.junit.Test;

public class PlayerTest {

	@Test
	public void testCreditAndDebit() {
		Player p = new Player("p1", "Alice", "RED", 100);
		p.credit(50);
		assertEquals(150, p.getMoney());
		p.debit(40);
		assertEquals(110, p.getMoney());
	}

	@Test
	public void testJailEnterLeave() {
		Player p = new Player("p1", "Alice", "RED", 100);
		assertFalse(p.isInJail());
		p.setInJail(true);
		assertTrue(p.isInJail());
		p.setInJail(false);
		assertFalse(p.isInJail());
	}

	@Test
	public void testPropertyManagement() {
		Player p = new Player("p1", "Alice", "RED", 100);
		int[] rents = new int[] {0,10,20,30,40,50};
		StreetOwnableSquare s = new StreetOwnableSquare(0, "Rua 0", "R0", 200, rents, 100);
		s.setOwner(p);
		p.addProperty(s);
		assertTrue(p.getProperties().contains(s));
		p.removeProperty(s);
		assertFalse(p.getProperties().contains(s));
	}

	@Test
	public void testBankruptcyFlag() {
		Player p = new Player("p1", "Alice", "RED", 0);
		assertTrue(p.isAlive());
		p.setBankrupt();
		assertTrue(p.isBankrupt());
		assertFalse(p.isAlive());
	}
}