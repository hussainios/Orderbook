import java.text.DecimalFormat;
import java.util.*;

/**
 * Utility class for formatting and displaying the state of the order book and executed trades.
 */
public class OrderOutputter {
    
    private static final int BUY_ID_WIDTH = 10;
    private static final int BUY_VOLUME_WIDTH = 13;
    private static final int BUY_PRICE_WIDTH = 7;
    private static final int SELL_PRICE_WIDTH = 7;
    private static final int SELL_VOLUME_WIDTH = 13;
    private static final int SELL_ID_WIDTH = 10;

    /**
     * Prints a list of trades to standard output in CSV format.
     *
     * @param trades the list of trades to print
     */
    public void printTrades(List<Trade> trades) {
        for (Trade trade : trades) {
            System.out.println(trade.toString());
        }
    }
    
    /**
     * Prints the current state of the order book in a tabular format.
     *
     * @param buyRows the list of buy orders to display
     * @param sellRows the list of sell orders to display
     */
    public void printBook(List<BookRow> buyRows, List<BookRow> sellRows) {
        System.out.println("+-----------------------------------------------------------------+");
        System.out.println("| BUY                            | SELL                           |");
        System.out.println("| Id       | Volume      | Price | Price | Volume      | Id       |");
        System.out.println("+----------+-------------+-------+-------+-------------+----------+");

        int maxRows = Math.max(buyRows.size(), sellRows.size());
        for (int i = 0; i < maxRows; i++) {
            BookRow buy = i < buyRows.size() ? buyRows.get(i) : null;
            BookRow sell = i < sellRows.size() ? sellRows.get(i) : null;
            System.out.println(formatRow(buy, sell));
        }

        System.out.println("+-----------------------------------------------------------------+");
    }
    
    private String formatRow(BookRow buy, BookRow sell) {
        StringBuilder sb = new StringBuilder("|");
        
        // Buy Id (10)
        sb.append(formatField(buy != null ? String.valueOf(buy.getId()) : "", BUY_ID_WIDTH, true));
        sb.append("|");
        // Buy Volume (13)
        sb.append(formatField(buy != null ? formatNumber(buy.getVolume()) : "", BUY_VOLUME_WIDTH, true));
        sb.append("|");
        // Buy Price (7)
        sb.append(formatField(buy != null ? formatNumber(buy.getPrice()) : "", BUY_PRICE_WIDTH, true));
        sb.append("|");
        // Sell Price (7)
        sb.append(formatField(sell != null ? formatNumber(sell.getPrice()) : "", SELL_PRICE_WIDTH, true));
        sb.append("|");
        // Sell Volume (13)
        sb.append(formatField(sell != null ? formatNumber(sell.getVolume()) : "", SELL_VOLUME_WIDTH, true));
        sb.append("|");
        // Sell Id (10)
        sb.append(formatField(sell != null ? String.valueOf(sell.getId()) : "", SELL_ID_WIDTH, true));
        sb.append("|");
        
        return sb.toString();
    }

    private String formatField(String value, int width, boolean rightJustify) {
        if (value.isEmpty()) {
            return " ".repeat(width);
        }
        if (rightJustify) {
            return String.format("%" + width + "s", value);
        } else {
            return String.format("%-" + width + "s", value);
        }
    }

    private String formatNumber(int number) {
        return new DecimalFormat("#,###").format(number);
    }
}

