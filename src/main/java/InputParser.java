public class InputParser {

    /**
     * Parses a line of text into a book command.
     *
     * @param line The line of text to parse
     * @return a {@link BookCommand} if parsing is successful, otherwise null
     */
    public static BookCommand parseLine(String line) {
        if (line == null) return null;

        line = line.trim();

        if (line.isEmpty() || line.startsWith("#")) {
            return null;
        }

        String[] parts = line.split(",", -1);

        String action = parts[0].trim();

        try {
            if ("ADD".equals(action)) {
                if (parts.length != 5 && parts.length != 6) {
                    return null;
                }

                char side = parts[1].trim().charAt(0);
                if (side != 'B' && side != 'S') {
                    return null;
                }

                int id = Integer.parseInt(parts[2].trim());
                int price = Integer.parseInt(parts[3].trim());
                int totalQuantity = Integer.parseInt(parts[4].trim());

                if (parts.length == 5) {
                    return new AddCommand(new LimitOrder(side, id, price, totalQuantity));
                }

                int peakSize = Integer.parseInt(parts[5].trim());
                return new AddCommand(new IcebergOrder(side, id, price, totalQuantity, peakSize));
            }

            if ("CANCEL".equals(action)) {
                if (parts.length != 2) {
                    return null;
                }

                int orderId = Integer.parseInt(parts[1].trim());
                return new CancelCommand(orderId);
            }

            return null;
        } catch (NumberFormatException e) {
            return null;
        } catch (StringIndexOutOfBoundsException e) {
            return null;
        }
    }
}
