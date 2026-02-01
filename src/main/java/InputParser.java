public class InputParser {

    /**
     * Parses a line of text into an {@link Order} object.
     * Supports both limit and iceberg order formats.
     *
     * @param line The line of text to parse
     * @return an {@link Order} if parsing is successful, otherwise null
     */
    public static Order parseLine(String line) {
        if (line == null) return null;

        line = line.trim();

        if (line.isEmpty() || line.startsWith("#")) {
            return null;
        }

        String[] parts = line.split(",", -1);

        if (parts.length != 4 && parts.length != 5) {
            return null;
        }

        char side = parts[0].trim().charAt(0);
        if (side != 'B' && side != 'S') {
            return null;
        }

        try {
            int id = Integer.parseInt(parts[1].trim());
            int price = Integer.parseInt(parts[2].trim());
            int totalQuantity = Integer.parseInt(parts[3].trim());

            if (parts.length == 4) {
                // limit order has no peak size
                return new LimitOrder(side, id, price, totalQuantity);
            }
            int peakSize = Integer.parseInt(parts[4].trim());
            return new IcebergOrder(side, id, price, totalQuantity, peakSize);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
