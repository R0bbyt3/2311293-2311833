package model;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

class BankruptcyTest {
    private GameEngine engine;
    private EconomyService economy;
    private Player devedor, credor;
    private StreetOwnableSquare ruaCara;

    @Before
    public void setUp() {
        engine = TestHelpers.enginePadrao40Casas();
        economy = engine.getEconomy();
        devedor = engine.criarJogador("Devedor", 200);
        credor = engine.criarJogador("Credor", 500);
        ruaCara = TestHelpers.ruaComAluguelElevado();
        economy.comprarPropriedade(credor, ruaCara);
        economy.construirCasa(credor, ruaCara);
    }

    @Test(timeout = 500)
    public void deveDeclararFalenciaQuandoNaoConseguePagarMesmoAposLiquidar() {
        engine.colocarJogadorNaCasa(devedor, ruaCara.getIndex());
        economy.processarParada(devedor, ruaCara);
        assertTrue("Jogador deve ser marcado como falido", devedor.isBankrupt());
        assertNull("Propriedades retornam ao banco após falência", TestHelpers.procurarAlgumaPropriedadeDo(devedor));
    }

    @Test(timeout = 500)
    public void deveLiquidarBensParaEvitarFalenciaQuandoPossivel() {
        PropertySquare p1 = TestHelpers.propriedadeDoJogador(devedor, 300);
        assertNotNull("Setup deve criar propriedade para o devedor", p1);
        engine.colocarJogadorNaCasa(devedor, ruaCara.getIndex());
        economy.processarParada(devedor, ruaCara);
        assertFalse("Se cobriu a dívida, não deve falir", devedor.isBankrupt());
    }

    @Test(timeout = 500)
    public void jogadorFalidoNaoDeveJogarMais() {
        devedor.marcarComoFalido();
        Player proximo = engine.proximoJogadorApos(devedor);
        assertNotEquals("Jogador falido deve ser pulado na ordem", devedor, proximo);
    }

    @Test(timeout = 500)
    public void deveTransferirPropriedadesAoBancoAoFalir() {
        PropertySquare p = TestHelpers.propriedadeDoJogador(devedor, 100);
        assertNotNull("Setup deve criar propriedade para o devedor", p);
        devedor.marcarComoFalido();
        engine.processarFalencia(devedor);
        assertNull("Dono deve ficar nulo após falência", p.getDono());
    }
}
