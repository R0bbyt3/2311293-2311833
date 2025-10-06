/* ===========================================================
 * Bank ; executa débitos/créditos e operações com o banco como contraparte.
 * Mantém apenas o caixa; regras/validações vivem na EconomyService.
 * =========================================================== */

package model;

final class Bank {

    // --- Caixa do banco ---
    private int cash;

    Bank(final int initialCash) {
        if (initialCash < 0) throw new IllegalArgumentException("Caixa inicial inválido.");
        this.cash = initialCash;
    }

    /* ===========================================================
     * Transferência genérica de dinheiro.
     * Convenção: passar null indica a ponta "BANK".
     *  - from == null  => BANK paga
     *  - to   == null  => BANK recebe
     * ===========================================================
     */
    void transfer(final Player from, final Player to, final int amount) {
        if (amount < 0) throw new IllegalArgumentException("amount deve ser >= 0");

        if (from == null && to == null) {
            throw new IllegalArgumentException("Pelo menos uma ponta deve ser Player.");
        }

        if (from == null) { // BANK -> Player
            ensureBankHas(amount);
            to.credit(amount);
            cash -= amount;
            return;
        }

        if (to == null) { // Player -> BANK
            from.debit(amount); 
            cash += amount;
            return;
        }

        // Player -> Player
        from.debit(amount);
        to.credit(amount);
        // caixa do banco não muda
    }

    /* ===========================================================
     * Utilidades
     * ===========================================================
     */
    
    private void ensureBankHas(final long amount) {
        if (amount > Integer.MAX_VALUE) throw new IllegalArgumentException("Valor excessivo.");
        if (cash < amount) {
            throw new IllegalStateException("Banco sem caixa suficiente para a operação.");
        }
    }
}
