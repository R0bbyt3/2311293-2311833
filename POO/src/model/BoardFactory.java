/* ===========================================================
 * BoardFactory ; cria o tabuleiro completo a partir de um CSV.
 * =========================================================== */

package model;

import java.nio.file.Path;
import java.util.*;

final class BoardFactory extends FactoryBase<Square> {

    private static final List<String> EXPECTED_HEADER = List.of(
        "index","type","name","price","rent0","rent1","rent2","rent3","rent4","hotel","multiplier","value"
    );

    static Board fromCSV(final Path csvPath) {
        BoardFactory factory = new BoardFactory();
        List<Square> squares = factory.readCSV(csvPath, EXPECTED_HEADER);

        // Identifica o índice da cadeia no tabuleiro
        int jailIndex = -1;
        for (int i = 0; i < squares.size(); i++) {
            if (squares.get(i) instanceof JailSquare) {
                jailIndex = i;
                break;
            }
        }

        if (jailIndex == -1) {
            throw new IllegalStateException("Nenhuma JailSquare encontrada no tabuleiro.");
        }
        
        return new Board(squares, jailIndex);
    }

    @Override
    protected Square parseLine(String[] p) {
        int index = parseInt(p[0]);
        String type = p[1].trim().toUpperCase(Locale.ROOT);
        String name = p[2].trim();
        int price = parseInt(p[3]);
        int[] rents = parseRentTable(p, 4, 6);
        int multiplier = parseInt(p[10]);
        int value = parseInt(p[11]);

        return switch (type) {
            case "START" -> new StartSquare(index, name, value);
            case "STREET" -> new StreetOwnableSquare(
                    index, name, name.toUpperCase(), price, rents, price);
            case "COMPANY" -> new CompanyOwnableSquare(
                    index, name, name.toUpperCase(), price, multiplier);
            case "MONEY" -> new MoneySquare(index, name, value);
            case "GOTOJAIL" -> new GoToJailSquare(index, name);
            case "CHANCE" -> new ChanceSquare(index, name);
            case "JAIL" -> new JailSquare(index, name);
            case "PARKING" -> new FreeParkingSquare(index, name);
		default -> throw new IllegalArgumentException("Valor inesperado ao criar quadrado de tabuleiro: " + type);
        };
    }
    
    // Extrai tabela de aluguel (ruas)
    protected int[] parseRentTable(String[] parts, int startIdx, int length) {
        int[] rents = new int[length];
        for (int i = 0; i < length; i++) rents[i] = parseInt(parts[startIdx + i]);
        return rents;
    }
}
