import java.text.DecimalFormat;
import java.util.*;

public class OrderOutputter {
    
    private static final int BUY_ID_WIDTH = 10;
    private static final int BUY_VOLUME_WIDTH = 13;
    private static final int BUY_PRICE_WIDTH = 7;
    private static final int SELL_PRICE_WIDTH = 7;
    private static final int SELL_VOLUME_WIDTH = 13;
    private static final int SELL_ID_WIDTH = 10;


    public void printTrades(List<Trade> trades) {
        for (Trade trade : trades) {
            System.out.println(trade.toString());
        }
    }
    
    /**
     * Prints the current state of the order book in the required format
     *
     * @param buyRows the list of buy orders with their volume and price to display
     * @param sellRows the list of sell orders with their volume and price to display
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
    
    /**
     * Creates a single row of the order book in the required format
     *
     * @param buy the buy order with its volume and price
     * @param sell the sell order with its volume and price
     * @return the row of the order book in the required format
     */
    private String formatRow(BookRow buy, BookRow sell) {
        StringBuilder sb = new StringBuilder("|");
        

        sb.append(formatField(buy != null ? String.valueOf(buy.getId()) : "", BUY_ID_WIDTH));
        sb.append("|");
        sb.append(formatField(buy != null ? formatNumber(buy.getVolume()) : "", BUY_VOLUME_WIDTH));
        sb.append("|");
        sb.append(formatField(buy != null ? formatNumber(buy.getPrice()) : "", BUY_PRICE_WIDTH));
        sb.append("|");
        sb.append(formatField(sell != null ? formatNumber(sell.getPrice()) : "", SELL_PRICE_WIDTH));
        sb.append("|");
        sb.append(formatField(sell != null ? formatNumber(sell.getVolume()) : "", SELL_VOLUME_WIDTH));
        sb.append("|");
        sb.append(formatField(sell != null ? String.valueOf(sell.getId()) : "", SELL_ID_WIDTH));
        sb.append("|");
        
        return sb.toString();
    }


    /**
     * Formats the entries so that they are aligned correctly in the order book
     *
     * @param value the value to format
     * @param width the width of the field
     * Always right pads the field so that IDs/numbers align.
     * @return the formatted field
     */
    private String formatField(String value, int width) {
        if (value.isEmpty()) {
            return " ".repeat(width);
        }
        return String.format("%" + width + "s", value);
    }

    private String formatNumber(int number) {
        return new DecimalFormat("#,###").format(number);
    }
}

