package model;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

class RentPaymentTest {
    private GameEngine engine;
    private EconomyService economy;
    private Player dono;
    private Player visitante;
    private StreetOwnableSquare rua;

    @Before
    public void setUp() {
        engine = TestHelpers.enginePadrao40Casas();
        economy = engine.getEconomy();
        dono = engine.criarJogador("Dono", 500);
        visitante = engine.criarJogador("Visitante", 500);
        rua = TestHelpers.ruaComAluguelBase(50, 100, 150, 200, 300);
        economy.comprarPropriedade(dono, rua);
    }

    @Test(timeout = 500)
    public void devePagarAluguelQuandoHaPeloMenosUmaCasa() {
        economy.construirCasa(dono, rua);
        int saldoD = dono.getSaldo();
        int saldoV = visitante.getSaldo();
        engine.colocarJogadorNaCasa(visitante, rua.getIndex());
        economy.processarParada(visitante, rua);
        assertTrue("Visitante deve ter saldo reduzido", visitante.getSaldo() < saldoV);
        assertTrue("Dono deve receber aluguel", dono.getSaldo() > saldoD);
    }

    @Test(timeout = 500)
    public void naoDeveCobrarAluguelSemConstrucao() {
        int saldoV = visitante.getSaldo();
        engine.colocarJogadorNaCasa(visitante, rua.getIndex());
        economy.processarParada(visitante, rua);
        assertEquals("Sem casas não cobra aluguel nesta iteração", saldoV, visitante.getSaldo());
    }

    @Test(timeout = 500)
    public void naoDevePagarAluguelNaPropriaPropriedade() {
        engine.colocarJogadorNaCasa(dono, rua.getIndex());
        int saldoAntes = dono.getSaldo();
        economy.processarParada(dono, rua);
        assertEquals("Dono não paga aluguel na própria propriedade", saldoAntes, dono.getSaldo());
    }

    @Test(timeout = 500)
    public void deveLiquidarBensQuandoSaldoInsuficiente() {
        economy.construirCasa(dono, rua);
        visitante.setSaldo(1);
        engine.colocarJogadorNaCasa(visitante, rua.getIndex());
        economy.processarParada(visitante, rua);
        assertTrue("Visitante deve ter tentado liquidar bens", visitante.getHistorico().contains("liquidacao"));
    }
}