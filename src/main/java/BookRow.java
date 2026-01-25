/**
 * Represents a simplified row for displaying order book state.
 */
public class BookRow {
    private final int id;
    private final int volume;
    private final int price;

    /**
     * Constructs a new BookRow.
     *
     * @param id the order identifier
     * @param volume the currently visible quantity
     * @param price the order price
     */
    public BookRow(int id, int volume, int price) {
        this.id = id;
        this.volume = volume;
        this.price = price;
    }

    public int getId() { return id; }
    public int getVolume() { return volume; }
    public int getPrice() { return price; }
}

