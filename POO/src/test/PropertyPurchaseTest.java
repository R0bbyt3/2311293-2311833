package model;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

class PropertyPurchaseTest {
    private GameEngine engine;
    private EconomyService economy;
    private Player p;
    private PropertySquare prop;

    @Before
    public void setUp() {
        engine = TestHelpers.enginePadrao40Casas();
        economy = engine.getEconomy();
        p = engine.criarJogador("A", 1000);
        prop = TestHelpers.propriedadeSemDono(200);
    }

    @Test(timeout = 500)
    public void deveComprarPropriedadeComSaldoSuficiente() {
        economy.comprarPropriedade(p, prop);
        assertEquals("Saldo deve ser debitado", 800, p.getSaldo());
        assertEquals("Jogador deve ser o dono", p, prop.getDono());
    }

    @Test(timeout = 500, expected = IllegalStateException.class)
    public void naoDeveComprarComSaldoInsuficiente() {
        p.setSaldo(100);
        economy.comprarPropriedade(p, prop);
    }

    @Test(timeout = 500, expected = IllegalStateException.class)
    public void naoDeveComprarPropriedadeComDono() {
        Player q = engine.criarJogador("B", 1000);
        economy.comprarPropriedade(q, prop);
        economy.comprarPropriedade(p, prop);
    }

    @Test(timeout = 500, expected = IllegalStateException.class)
    public void naoDeveComprarForaDaVez() {
        engine.setJogadorDaVez(p);
        Player q = engine.criarJogador("B", 1000);
        economy.comprarPropriedade(q, prop);
    }

    @Test(timeout = 500, expected = IllegalArgumentException.class)
    public void naoDeveComprarCasaNaoCompravel() {
        Square naoCompravel = TestHelpers.casaNaoCompravel();
        economy.comprarPropriedade(p, (PropertySquare) naoCompravel);
    }
}