package model;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

class ConstructionTest {
    private GameEngine engine;
    private EconomyService economy;
    private Player p;
    private StreetOwnableSquare rua;

    @Before
    public void setUp() {
        engine = TestHelpers.enginePadrao40Casas();
        economy = engine.getEconomy();
        p = engine.criarJogador("A", 1000);
        rua = TestHelpers.ruaComPrecoCasa(150);
        economy.comprarPropriedade(p, rua);
        engine.setJogadorDaVez(p);
        p.setPosition(rua.getIndex());
    }

    @Test(timeout = 500)
    public void deveConstruirCasaComSaldoSuficienteNaPropriedadeOndeCaiu() {
        economy.construirCasa(p, rua);
        assertEquals("Saldo deve ser debitado", 850, p.getSaldo());
        assertEquals("Número de casas deve aumentar para 1", 1, rua.getCasas());
    }

    @Test(timeout = 500, expected = IllegalStateException.class)
    public void naoDeveConstruirSemSaldoSuficiente() {
        p.setSaldo(100);
        economy.construirCasa(p, rua);
    }

    @Test(timeout = 500, expected = IllegalStateException.class)
    public void naoDeveConstruirEmPropriedadeDeOutroJogador() {
        Player q = engine.criarJogador("B", 1000);
        StreetOwnableSquare rua2 = TestHelpers.ruaComPrecoCasa(150);
        engine.getEconomy().comprarPropriedade(q, rua2);
        engine.setJogadorDaVez(p);
        p.setPosition(rua2.getIndex());
        economy.construirCasa(p, rua2);
    }

    @Test(timeout = 500)
    public void deveRespeitarLimiteDeCasasEHotel() {
        for (int i = 0; i < 4; i++) economy.construirCasa(p, rua);
        assertEquals("Depois de 4 construções deve ter 4 casas", 4, rua.getCasas());
        economy.construirCasa(p, rua); // converte para hotel
        assertTrue("Deve ter hotel após 4 casas", rua.temHotel());
    }

    @Test(timeout = 500, expected = IllegalStateException.class)
    public void naoDeveConstruirAposHotel() {
        for (int i = 0; i < 5; i++) economy.construirCasa(p, rua); // até hotel
        economy.construirCasa(p, rua); // deve falhar
    }

    @Test(timeout = 500, expected = IllegalStateException.class)
    public void naoDeveConstruirQuandoNaoForPropriedadeOndeCaiu() {
        // força jogador da vez a estar em outra casa
        p.setPosition((rua.getIndex() + 1) % 40);
        economy.construirCasa(p, rua);
    }
}