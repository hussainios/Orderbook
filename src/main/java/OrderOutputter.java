import java.text.DecimalFormat;
import java.util.*;

public class OrderOutputter {
    
    /**
     * Prints a trade execution
     */
    public void printTrade(Order aggressor, int passiveId, int price, int quantity) {
        if (quantity <= 0) return;
        
        int buyId, sellId;
        if (aggressor.getSide() == 'B') {
            buyId = aggressor.getId();
            sellId = passiveId;
        } else {
            buyId = passiveId;
            sellId = aggressor.getId();
        }
        
        System.out.println(buyId + "," + sellId + "," + price + "," + quantity);
    }
    
    /**
     * Prints the current state of the order book
     */
    public void printBook(TreeMap<Integer, ArrayDeque<Order>> buySide, 
                          TreeMap<Integer, ArrayDeque<Order>> sellSide) {
        System.out.println("+-----------------------------------------------------------------+");
        System.out.println("| BUY                            | SELL                           |");
        System.out.println("| Id       | Volume      | Price | Price | Volume      | Id       |");
        System.out.println("+----------+-------------+-------+-------+-------------+----------+");

        List<BookRow> buyRows = new ArrayList<>();
        for (Map.Entry<Integer, ArrayDeque<Order>> entry : buySide.entrySet()) {
            for (Order o : entry.getValue()) {
                buyRows.add(new BookRow(o.getId(), o.getVisibleQuantity(), entry.getKey()));
            }
        }

        List<BookRow> sellRows = new ArrayList<>();
        for (Map.Entry<Integer, ArrayDeque<Order>> entry : sellSide.entrySet()) {
            for (Order o : entry.getValue()) {
                sellRows.add(new BookRow(o.getId(), o.getVisibleQuantity(), entry.getKey()));
            }
        }

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
        sb.append(formatField(buy != null ? String.valueOf(buy.id) : "", 10, true));
        sb.append("|");
        // Buy Volume (13)
        sb.append(formatField(buy != null ? formatNumber(buy.volume) : "", 13, true));
        sb.append("|");
        // Buy Price (7)
        sb.append(formatField(buy != null ? formatNumber(buy.price) : "", 7, true));
        sb.append("|");
        // Sell Price (7)
        sb.append(formatField(sell != null ? formatNumber(sell.price) : "", 7, true));
        sb.append("|");
        // Sell Volume (13)
        sb.append(formatField(sell != null ? formatNumber(sell.volume) : "", 13, true));
        sb.append("|");
        // Sell Id (10)
        sb.append(formatField(sell != null ? String.valueOf(sell.id) : "", 10, true));
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

class BookRow {
    int id;
    int volume;
    int price;

    BookRow(int id, int volume, int price) {
        this.id = id;
        this.volume = volume;
        this.price = price;
    }
}

