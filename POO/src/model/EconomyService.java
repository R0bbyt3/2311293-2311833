/* ===========================================================
 * EconomyService ; aplica regras financeiras e garante liquidez/falência.
 * Delegação de execução ($) para o Bank.
 * =========================================================== */

package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class EconomyService {

    private final Bank bank;
    private static final double BANK_BUYBACK_RATE = 0.90;

    EconomyService(final Bank bank) {
        this.bank = Objects.requireNonNull(bank, "bank");
    }

    /* ===========================================================
     * Aluguel: calcula e cobra do visitante para o dono.
     * =========================================================== */
    void chargeRent(final Player visitor, final OwnableSquare property, final GameEngine engine) {

        if (!property.hasOwner()) return; // sem dono = sem cobrança
        final Player owner = property.getOwner();
        if (owner == visitor) return;     // própria = sem cobrança

        final int rent = property.calcRent(engine);
        if (rent <= 0) return;

        // Garante liquidez do pagador
        if (!liquidateOrBankruptIfNeeded(visitor, rent)) {
        	 return; 
        }

        // Executa a transferência Player -> Player
        bank.transfer(visitor, owner, rent);
    }

    /* ===========================================================
     * Compra de propriedade do banco para o jogador.
     * =========================================================== */
    boolean attemptBuy(final Player player, final OwnableSquare property) {
    	
        if (property.hasOwner()) return false;
        
        final int price = property.getPrice();

        boolean canPay = player.canAfford(price);
        if (!canPay) return false;
        
        // Player -> BANK
        bank.transfer(player, null, price);

        // Transfere título
        property.setOwner(player);
        player.addProperty(property);
        return true;
    }

    /* ===========================================================
     * Construção em rua do próprio jogador (1 nível).
     * =========================================================== */
    boolean attemptBuild(final Player player, final StreetOwnableSquare street) {
    	
        if (!street.hasOwner() || street.getOwner() != player) return false;
        if (!street.canBuild()) return false;

        final int cost = street.getBuildCost();

        boolean canPay = player.canAfford(cost);
        if (!canPay) return false;
        
        bank.transfer(player, null, cost);
        street.buildOne();
        return true;
    }

    /* ===========================================================
     * Transferência direta entre jogadores (Player ↔ Player).
     * =========================================================== */
    void transfer(final Player from, final Player to, final int amount) {
        if (amount <= 0) return;

        // Garante liquidez do pagador
        if (!liquidateOrBankruptIfNeeded(from, amount)) {
        	 return; 
        }

        bank.transfer(from, to, amount);
    }
    
    /* ===========================================================
     * Aplica pagamento do jogador ao banco.
     * =========================================================== */
    void applyPayment(final Player player, final int amount) {
        if (amount <= 0) return;

        // Garante liquidez do pagador
        if (!liquidateOrBankruptIfNeeded(player, amount)) {
            return; // jogador já foi declarado falido
        }

        // Player → BANK
        bank.transfer(player, null, amount);
    }

    /* ===========================================================
     * Aplica pagamento do banco ao jogador
     * =========================================================== */
    
    void applyIncome(final Player player, final int amount) {
        if (amount <= 0) return;

        // Banco transfere dinheiro ao jogador (BANK -> Player)
        bank.transfer(null, player, amount);
    }

    /* ===========================================================
     * Se necessário liquida imóveis ou leva o jogador a falência
     * =========================================================== */
    boolean liquidateOrBankruptIfNeeded(final Player player, final int required) {
        Objects.requireNonNull(player, "player");
        
        // Verifica se o jogador tem saldo suficiente
        boolean canPay = player.canAfford(required);
       
        // Se já pode pagar, nada a fazer
        if (canPay) return true;

        // Faltando
        int missing = player.howMuchMissing(required);
        
        // Tenta vender propriedades para cobrir o valor faltante
        final List<OwnableSquare> owned = new ArrayList<>(player.getProperties());
        for (OwnableSquare prop : owned) {
        	
            final int gross = prop.getTotalInvestment();
            final int received = (int) Math.floor(gross * BANK_BUYBACK_RATE);
            
            // Banco recompra a propriedade
            bank.transfer(null, player, received);

            // Remove propriedade do jogador e limpa a posse
            player.removeProperty(prop);
            prop.removeOwner(player); 

            missing -= received;
            if (missing <= 0) return true;  
         
        }

        // Se ainda falta dinheiro → falência
        declareBankruptcy(player);
        return false;
    }

    /* ===========================================================
     * Falência: remove jogador do jogo e devolve seus títulos.
     * =========================================================== */
    void declareBankruptcy(final Player player) {

        // Devolve todos os títulos ao banco (sem pagamento adicional)
        for (OwnableSquare prop : new ArrayList<>(player.getProperties())) {
            player.removeProperty(prop);
            prop.removeOwner(player); 
        }
        player.setBankrupt();
    }
    
    

}

