package model;

import static org.junit.Assert.*;
import static model.api.dto.PlayerColor.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class EconomyServiceTest {
	
	private static final int DEFAULT_TIMEOUT = 2000;

    // Square sem efeito para compor o tabuleiro
    static class NoopSquare extends Square {
        NoopSquare(int index) { super(index, "S" + index); }
        @Override void onLand(Player player, GameEngine engine, EconomyService economy) { /* no-op */ }
    }

    private StreetOwnableSquare makeStreet(int index, int price) {
        // Tabela de aluguel simples (será pouco usada nestes testes)
        int[] rents = new int[] {0, 10, 20, 30, 40, 50};
        return new StreetOwnableSquare(index, "Rua " + index, "R" + index, price, rents, 100);
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

    @Test(timeout = DEFAULT_TIMEOUT)
    public void shouldBuyWhenHasSufficientBalance() {
        StreetOwnableSquare prop = makeStreet(0, 200);
        Board board = makeBoardWithPropertyAt0(prop);
        GameEngine engine = makeEngine(p1, p2, board);

        // p1 no index 0 por padrão; tenta comprar
        boolean bought = engine.chooseBuy();

        assertTrue("compra deve ser bem-sucedida com saldo suficiente", bought);
        assertEquals("saldo reduzido após compra", 300, p1.getMoney());
        assertTrue("propriedade deve ter dono", prop.hasOwner());
        assertEquals("dono deve ser o comprador", p1, prop.getOwner());
        assertTrue(p1.getProperties().contains(prop));
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void shouldNotBuyWhenInsufficientBalance() {
        StreetOwnableSquare prop = makeStreet(0, 800); // maior que o saldo do p1
        Board board = makeBoardWithPropertyAt0(prop);
        GameEngine engine = makeEngine(p1, p2, board);

        boolean bought = engine.chooseBuy();

        assertFalse("compra deve falhar sem saldo suficiente", bought);
        assertEquals("saldo permanece inalterado", 500, p1.getMoney());
        assertFalse("propriedade permanece sem dono", prop.hasOwner());
        assertFalse(p1.getProperties().contains(prop));
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void shouldNotBuyPropertyWithExistingOwner() {
        StreetOwnableSquare prop = makeStreet(0, 200);
        // Define dono prévio como p2
        prop.setOwner(p2);
        p2.addProperty(prop);

        Board board = makeBoardWithPropertyAt0(prop);
        GameEngine engine = makeEngine(p1, p2, board);

        boolean bought = engine.chooseBuy();

        
        assertFalse("não deve comprar propriedade já possuída", bought);
        assertEquals("saldo do comprador não muda", 500, p1.getMoney());
        assertTrue(prop.hasOwner());
        assertEquals("dono permanece o original", p2, prop.getOwner());
        assertFalse(p1.getProperties().contains(prop));
        assertTrue(p2.getProperties().contains(prop));
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void shouldPayRentWhenHasBalance() {
        StreetOwnableSquare rentProp = makeStreet(0, 200);
        // Propriedade pertence ao p2 e tem 1 casa
        rentProp.setOwner(p2);
        p2.addProperty(rentProp);
        rentProp.buildOne();

        Board board = makeBoardWithPropertyAt0(rentProp);
        GameEngine engine = makeEngine(p1, p2, board);

        // p1 "cai" na propriedade do p2 (está no índice 0) -> resolve efeito
        engine.onLand();

        // Aluguel com 1 casa = 10        
        assertEquals("pagador perde 10 ao cair em propriedade com 1 casa", 490, p1.getMoney());
        assertEquals("dono recebe 10", 510, p2.getMoney());
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void shouldLiquidateAssetsToPayRent() {
        // p1 com pouco dinheiro, mas com um bem para liquidar
        Player poor = new Player("pPoor", "Carol", GRAY, 500);
        poor.debit(495); // fica com 5

        // bem do p1 a ser liquidado (não precisa estar no tabuleiro)
        StreetOwnableSquare asset = makeStreet(5, 100);
        asset.setOwner(poor);
        poor.addProperty(asset);

        // Propriedade de p2 com 1 casa (aluguel = 10)
        StreetOwnableSquare rentProp = makeStreet(0, 200);
        rentProp.setOwner(p2);
        p2.addProperty(rentProp);
        rentProp.buildOne();

        Board board = makeBoardWithPropertyAt0(rentProp);
        GameEngine engine = makeEngine(poor, p2, board);

        // poor cai na propriedade do p2
        engine.onLand();

        // Liquidação: asset vale 100 investido -> banco paga 90; paga aluguel 10
        // Saldo final esperado do poor: 5 + 90 - 10 = 85
        assertEquals("saldo final após liquidação e pagamento", 85, poor.getMoney());
        assertEquals("dono recebe aluguel de 10", 510, p2.getMoney());
        // asset deve ter sido removido do patrimônio
        assertFalse(poor.getProperties().contains(asset));
        assertFalse(asset.hasHotel());
        assertEquals(0, asset.getHouses());
        assertFalse("propriedade liquidada não deve ter dono", asset.hasOwner());
        
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void shouldNotChargeRentWhenNoHouses() {
        StreetOwnableSquare rentProp = makeStreet(0, 200);
        rentProp.setOwner(p2);
        p2.addProperty(rentProp);
        // Sem casas

        Board board = makeBoardWithPropertyAt0(rentProp);
        GameEngine engine = makeEngine(p1, p2, board);

        engine.onLand();

        // Aluguel para 0 casas = 0 -> sem transferência
        assertEquals("saldo do jogador não deve mudar sem casas", 500, p1.getMoney());
        
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void shouldNotChargeRentOnOwnProperty() {
        StreetOwnableSquare ownProp = makeStreet(0, 200);
        ownProp.setOwner(p1);
        p1.addProperty(ownProp);
        ownProp.buildOne(); // mesmo com casa, não paga

        Board board = makeBoardWithPropertyAt0(ownProp);
        GameEngine engine = makeEngine(p1, p2, board);

        engine.onLand();

        assertEquals("jogador não paga aluguel na própria propriedade", 500, p1.getMoney());
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void shouldGoBankruptWhenUnableToPayRent() {
        // Poor player with low cash and some assets, but liquidation is insufficient
        Player poor = new Player("pPoor", "Carol", GRAY, 5);

        // Two low-value assets (buildCost small → liquidation small)
        int[] rents = new int[] {0, 10, 20, 30, 40, 50};
        StreetOwnableSquare low1 = new StreetOwnableSquare(7, "Low1", "L1", 50, rents, 10); // invested=10 → liquidation=9
        StreetOwnableSquare low2 = new StreetOwnableSquare(8, "Low2", "L2", 50, rents, 10); // invested=10 → liquidation=9
        low1.setOwner(poor); poor.addProperty(low1);
        low2.setOwner(poor); poor.addProperty(low2);

        // Other player's property with hotel (rent = 50)
        Player owner = new Player("pOwner", "Dave", YELLOW, 500);
        StreetOwnableSquare rentProp = makeStreet(0, 200);
        rentProp.setOwner(owner);
        owner.addProperty(rentProp);
        for (int i = 0; i < 5; i++) rentProp.buildOne();

        Board board = makeBoardWithPropertyAt0(rentProp);
        GameEngine engine = makeEngine(poor, owner, board);

        // poor lands on owner's property; even after liquidation (5 + 9 + 9 = 23) < 50 → bankruptcy
        engine.onLand();

        assertTrue("jogador deve falir", poor.isBankrupt());
        // Rent is not transferred when payer goes bankrupt during charge
        assertEquals("dono não recebe aluguel de jogador falido", 500, owner.getMoney());
        // Bankrupt player's properties were liquidated and ownership cleared
        assertTrue("patrimônio do falido deve ser limpo", poor.getProperties().isEmpty());
        assertFalse(low1.hasOwner());
        assertFalse(low2.hasOwner());
        
    }
}