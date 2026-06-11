import java.util.Locale;

public class ReplayStats {
    private long totalLinesRead;
    private long acceptedCommands;
    private long acceptedAdds;
    private long acceptedCancels;
    private long tradeCount;
    private long elapsedNanos;

    public void incrementTotalLinesRead() {
        totalLinesRead++;
    }

    public void recordAcceptedAdd(int tradesProduced) {
        acceptedCommands++;
        acceptedAdds++;
        tradeCount += tradesProduced;
    }

    public void recordAcceptedCancel() {
        acceptedCommands++;
        acceptedCancels++;
    }

    public void setElapsedNanos(long elapsedNanos) {
        this.elapsedNanos = elapsedNanos;
    }

    public long getTotalLinesRead() {
        return totalLinesRead;
    }

    public long getAcceptedCommands() {
        return acceptedCommands;
    }

    public long getAcceptedAdds() {
        return acceptedAdds;
    }

    public long getAcceptedCancels() {
        return acceptedCancels;
    }

    public long getTradeCount() {
        return tradeCount;
    }

    public long getElapsedNanos() {
        return elapsedNanos;
    }

    public double getElapsedMillis() {
        return elapsedNanos / 1_000_000.0;
    }

    public double getCommandsPerSecond() {
        if (elapsedNanos == 0L) {
            return acceptedCommands == 0L ? 0.0 : Double.POSITIVE_INFINITY;
        }
        return acceptedCommands * 1_000_000_000.0 / elapsedNanos;
    }

    public String toSummaryString() {
        return String.join(System.lineSeparator(),
            "totalLinesRead=" + totalLinesRead,
            "acceptedCommands=" + acceptedCommands,
            "acceptedAdds=" + acceptedAdds,
            "acceptedCancels=" + acceptedCancels,
            "tradeCount=" + tradeCount,
            "elapsedMillis=" + String.format(Locale.ROOT, "%.3f", getElapsedMillis()),
            "commandsPerSecond=" + String.format(Locale.ROOT, "%.3f", getCommandsPerSecond())
        );
    }
}
