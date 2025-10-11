package model;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class DiceRollTest {
    private DiceRoll roll;

    @Before
    public void setUp() {
        roll = new DiceRoll(); // injeta seed se houver suporte
    }

    @Test(timeout = 500)
    public void deveSomarDadosCorretamente() {
        DiceRoll r = new DiceRoll(3, 4); // usar construtor de teste, stub ou setter
        assertEquals("Soma de d1 + d2 deve igualar sum", 7, r.getSum());
    }

    @Test(timeout = 500)
    public void deveIdentificarDuplaSomenteQuandoD1IgualD2() {
        DiceRoll r1 = new DiceRoll(5, 5);
        assertTrue("isDouble deve ser verdadeiro apenas quando d1 == d2", r1.isDouble());

        DiceRoll r2 = new DiceRoll(5, 4);
        assertFalse("isDouble deve ser falso quando d1 != d2", r2.isDouble());
    }

    @Test(timeout = 500)
    public void deveRespeitarLimitesDe1a6ParaCadaDado() {
        DiceRoll r = new DiceRoll();
        assertTrue("d1 deve estar entre 1 e 6", r.getD1() >= 1 && r.getD1() <= 6);
        assertTrue("d2 deve estar entre 1 e 6", r.getD2() >= 1 && r.getD2() <= 6);
    }

    @Test(timeout = 500)
    public void deveGerarValoresInteirosValidos() {
        DiceRoll r = new DiceRoll();
        assertEquals("d1 deve ser inteiro", 0, r.getD1() % 1);
        assertEquals("d2 deve ser inteiro", 0, r.getD2() % 1);
    }

    @Test(timeout = 500)
    public void deveSerReprodutivelComAMesmaSeedQuandoSuportado() {
        // se a classe não suportar seed, podes remover este teste
        DiceRoll r1 = new DiceRoll(123L);
        DiceRoll r2 = new DiceRoll(123L);
        assertEquals("Sequência deve repetir com a mesma seed", r1.getSum(), r2.getSum());
    }
}