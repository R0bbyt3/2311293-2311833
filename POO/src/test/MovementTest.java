package model;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

class MovementTest {
    private GameEngine engine;
    private Player p;

    @Before
    public void setUp() {
        engine = TestHelpers.enginePadrao40Casas();
        p = engine.criarJogador("A");
    }

    @Test(timeout = 500)
    public void deveMoverQuantidadeCorretaDeCasasEmJogadaSimples() {
        p.setPosition(10);
        engine.moveBy(7);
        assertEquals("Posição deve avançar 7 casas", 17, p.getPosition());
    }

    @Test(timeout = 500)
    public void deveCalcularPassagemPeloInicioCorretamente() {
        p.setPosition(38);
        engine.moveBy(5);
        assertEquals("Em tabuleiro de 40 casas, 38 + 5 = 3", 3, p.getPosition());
    }

    @Test(timeout = 500)
    public void deveCairExatamenteNoInicioQuandoSomatorioIgualAoTamanho() {
        p.setPosition(35);
        engine.moveBy(5);
        assertEquals("35 + 5 deve resultar em 0", 0, p.getPosition());
    }

    @Test(timeout = 500)
    public void jogadorPresoNaoDeveMoverSemDupla() {
        p.enviarParaPrisao();
        engine.jogarTurnoPreso(p, new DiceRoll(3, 4));
        assertTrue("Jogador deve continuar preso", p.isInJail());
        assertEquals("Posição não deve mudar", engine.getCasaPrisao(), p.getPosition());
    }

    @Test(timeout = 500)
    public void jogadorPresoDeveSairComDupla() {
        p.enviarParaPrisao();
        engine.jogarTurnoPreso(p, new DiceRoll(2, 2));
        assertFalse("Jogador deve sair da prisão com dupla", p.isInJail());
    }

    @Test(timeout = 500, expected = IllegalArgumentException.class)
    public void deveLancarErroParaMovimentoInvalido() {
        engine.moveBy(-1);
    }
}