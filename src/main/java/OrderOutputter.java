import java.text.DecimalFormat;
import java.util.*;

public class OrderOutputter {
    
    /**
     * Prints a list of trades
     */
    public void printTrades(List<Trade> trades) {
        for (Trade trade : trades) {
            System.out.println(trade.toString());
        }
    }
    
    /**
     * Prints the current state of the order book
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
        sb.append(formatField(buy != null ? String.valueOf(buy.getId()) : "", 10, true));
        sb.append("|");
        // Buy Volume (13)
        sb.append(formatField(buy != null ? formatNumber(buy.getVolume()) : "", 13, true));
        sb.append("|");
        // Buy Price (7)
        sb.append(formatField(buy != null ? formatNumber(buy.getPrice()) : "", 7, true));
        sb.append("|");
        // Sell Price (7)
        sb.append(formatField(sell != null ? formatNumber(sell.getPrice()) : "", 7, true));
        sb.append("|");
        // Sell Volume (13)
        sb.append(formatField(sell != null ? formatNumber(sell.getVolume()) : "", 13, true));
        sb.append("|");
        // Sell Id (10)
        sb.append(formatField(sell != null ? String.valueOf(sell.getId()) : "", 10, true));
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

