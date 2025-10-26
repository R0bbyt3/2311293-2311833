package model;

import static org.junit.Assert.*;
import static model.api.dto.PlayerColor.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class StreetOwnableSquareTest {

    // No-op square to complete the board
    static class NoopSquare extends Square {
        NoopSquare(int index) { super(index, "S" + index); }
        @Override void onLand(Player player, GameEngine engine, EconomyService economy) { /* no-op */ }
    }

    private StreetOwnableSquare makeStreet(int index, int price, int buildCost) {
        int[] rents = new int[] {0, 10, 20, 30, 40, 50};
        return new StreetOwnableSquare(index, "Rua " + index, "R" + index, price, rents, buildCost);
    }

    private Board makeBoardWithPropertyAt0(StreetOwnableSquare prop) {
        List<Square> squares = new ArrayList<>();
        squares.add(prop); // index 0
        for (int i = 1; i < 8; i++) squares.add(new NoopSquare(i));
        return new Board(squares, 3);
    }

    private Deck makeDeck() {
        return new Deck(Arrays.asList(new Card(0, Card.CardType.RECEIVE_BANK, 0)));
    }

    private EconomyService makeEconomy() {
        return new EconomyService(new Bank(1_000_000));
    }

    private GameEngine makeEngine(Player p1, Player p2, Board board) {
        return new GameEngine(board, Arrays.asList(p1, p2), makeDeck(), makeEconomy(), 0);
    }

    private Player p1;
    private Player p2;

    @Before
    public void setUp() {
        p1 = new Player("p1", "Alice", RED, 500);
        p2 = new Player("p2", "Bob", BLUE, 500);
    }

    @Test
    public void shouldBuildWhenHasSufficientBalance() {
        StreetOwnableSquare prop = makeStreet(0, 200, 100);
        // p1 é dono da propriedade
        prop.setOwner(p1);
        p1.addProperty(prop);

        Board board = makeBoardWithPropertyAt0(prop);
        GameEngine engine = makeEngine(p1, p2, board);

        boolean built = engine.chooseBuild();

        assertTrue(built);
        assertEquals(400, p1.getMoney()); // 500 - 100 buildCost
        assertEquals(1, prop.getHouses());
        assertFalse(prop.hasHotel());
    }

    @Test
    public void shouldNotBuildWhenInsufficientBalance() {
        StreetOwnableSquare prop = makeStreet(0, 200, 600); // custo > saldo p1 (500)
        prop.setOwner(p1);
        p1.addProperty(prop);

        Board board = makeBoardWithPropertyAt0(prop);
        GameEngine engine = makeEngine(p1, p2, board);

        boolean built = engine.chooseBuild();

        assertFalse(built);
        assertEquals(500, p1.getMoney()); // saldo inalterado
        assertEquals(0, prop.getHouses());
        assertFalse(prop.hasHotel());
    }

    @Test
    public void shouldNotBuildOnOthersProperty() {
        StreetOwnableSquare prop = makeStreet(0, 200, 100);
        prop.setOwner(p2);
        p2.addProperty(prop);

        Board board = makeBoardWithPropertyAt0(prop);
        GameEngine engine = makeEngine(p1, p2, board);

        boolean built = engine.chooseBuild();

        assertFalse(built);
        assertEquals(500, p1.getMoney());
        assertEquals(0, prop.getHouses());
        assertFalse(prop.hasHotel());
    }

    @Test
    public void shouldReachMaxHousesAndHotelAndThenBlock() {
        // p1 com saldo alto para várias construções
        p1 = new Player("p1", "Alice", RED, 2000);
        StreetOwnableSquare prop = makeStreet(0, 200, 100);
        prop.setOwner(p1);
        p1.addProperty(prop);

        Board board = makeBoardWithPropertyAt0(prop);
        GameEngine engine = makeEngine(p1, p2, board);

        // Construir 4 casas
        for (int i = 1; i <= 4; i++) {
            assertTrue("Construção " + i + " deve ser permitida", engine.chooseBuild());
            assertEquals(i, prop.getHouses());
            assertFalse(prop.hasHotel());
        }
        assertEquals(2000 - 4 * 100, p1.getMoney());

        // 5ª construção vira hotel
        assertTrue(engine.chooseBuild());
        assertEquals(4, prop.getHouses());
        assertTrue(prop.hasHotel());
        assertEquals(2000 - 5 * 100, p1.getMoney());

        // 6ª tentativa não deve ser permitida
        assertFalse(engine.chooseBuild());
        assertEquals(4, prop.getHouses());
        assertTrue(prop.hasHotel());
        assertEquals(2000 - 5 * 100, p1.getMoney());
    }
}