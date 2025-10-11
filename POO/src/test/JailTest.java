package model;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

class JailTest {
    private GameEngine engine;
    private Player p;

    @Before
    public void setUp() {
        engine = TestHelpers.enginePadrao40Casas();
        p = engine.criarJogador("A");
    }

    @Test(timeout = 500)
    public void deveIrParaPrisaoAoCairNaCasaVaParaPrisao() {
        engine.moverParaVaParaPrisao(p);
        assertTrue("Jogador deve estar preso", p.isInJail());
        assertEquals("Posição deve ser a da prisão", engine.getCasaPrisao(), p.getPosition());
    }

    @Test(timeout = 500)
    public void deveSairDaPrisaoComDupla() {
        p.enviarParaPrisao();
        engine.jogarTurnoPreso(p, new DiceRoll(3, 3));
        assertFalse("Ao tirar dupla, sai da prisão", p.isInJail());
    }

    @Test(timeout = 500)
    public void deveSairDaPrisaoComCarta() {
        p.enviarParaPrisao();
        p.adicionarCartaSaidaLivre();
        engine.usarCartaSaidaLivre(p);
        assertFalse("Carta deve libertar o jogador", p.isInJail());
    }

    @Test(timeout = 500)
    public void devePermanecerNaPrisaoSemDuplaNemCarta() {
        p.enviarParaPrisao();
        engine.jogarTurnoPreso(p, new DiceRoll(2, 5));
        assertTrue("Sem dupla nem carta, permanece preso", p.isInJail());
    }

    @Test(timeout = 500)
    public void deveSairComPagamentoDeFiancaSeRegraSuportar() {
        p.enviarParaPrisao();
        engine.pagarFianca(p);
        assertFalse("Após pagar fiança, deve sair da prisão", p.isInJail());
    }

    @Test(timeout = 500)
    public void deveAplicarLimiteDeTurnosNaPrisaoSeRegraSuportar() {
        p.enviarParaPrisao();
        engine.marcarTurnoPreso(p);
        engine.marcarTurnoPreso(p);
        engine.marcarTurnoPreso(p); // no 3º força saída por regra
        assertFalse("Após limite de turnos, deve sair", p.isInJail());
    }
}