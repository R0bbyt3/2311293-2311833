/* ===========================================================
 * DeckFactory ; cria um baralho de cartas de Sorte/Revés.
 * =========================================================== */

package model;

import java.nio.file.Path;
import java.util.*;

final class DeckFactory extends FactoryBase<Card> {

    private static final List<String> EXPECTED_HEADER = List.of("index", "type", "value");

    static Deck fromCSV(final Path csvPath) {
        DeckFactory factory = new DeckFactory();
        List<Card> cards = factory.readCSV(csvPath, EXPECTED_HEADER);

        if (cards.isEmpty())
            throw new IllegalArgumentException("Deck vazio: " + csvPath);

        Deck deck = new Deck(cards);
        deck.shuffle();
        return deck;
    }

    @Override
    protected Card parseLine(String[] p) {
        String typeStr = p[1].trim().toUpperCase(Locale.ROOT);
        int value = parseInt(p[2]);
        Card.CardType type = Card.CardType.valueOf(typeStr);
        return new Card(type, value);
    }
}
