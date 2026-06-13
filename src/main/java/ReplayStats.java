import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ReplayStats {
    private long totalLinesRead;
    private long acceptedCommands;
    private long acceptedAdds;
    private long acceptedCancels;
    private long ignoredCommands;
    private long tradeCount;
    private long elapsedNanos;
    private final List<Long> acceptedCommandLatenciesNanos = new ArrayList<>();
    private final List<Long> acceptedAddLatenciesNanos = new ArrayList<>();
    private final List<Long> acceptedCancelLatenciesNanos = new ArrayList<>();

    public void incrementTotalLinesRead() {
        totalLinesRead++;
    }

    public void recordAcceptedAdd(int tradesProduced, long latencyNanos) {
        acceptedCommands++;
        acceptedAdds++;
        tradeCount += tradesProduced;
        acceptedCommandLatenciesNanos.add(latencyNanos);
        acceptedAddLatenciesNanos.add(latencyNanos);
    }

    public void recordAcceptedCancel(long latencyNanos) {
        acceptedCommands++;
        acceptedCancels++;
        acceptedCommandLatenciesNanos.add(latencyNanos);
        acceptedCancelLatenciesNanos.add(latencyNanos);
    }

    public void recordIgnoredCommand() {
        ignoredCommands++;
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

    public long getIgnoredCommands() {
        return ignoredCommands;
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
        return getOperationsPerSecond(acceptedCommands);
    }

    public long getP50LatencyNanos() {
        return getPercentileLatencyNanos(acceptedCommandLatenciesNanos, 0.50);
    }

    public long getP50AddLatencyNanos() {
        return getPercentileLatencyNanos(acceptedAddLatenciesNanos, 0.50);
    }

    public long getP50CancelLatencyNanos() {
        return getPercentileLatencyNanos(acceptedCancelLatenciesNanos, 0.50);
    }

    public long getP95LatencyNanos() {
        return getPercentileLatencyNanos(acceptedCommandLatenciesNanos, 0.95);
    }

    public long getP99LatencyNanos() {
        return getPercentileLatencyNanos(acceptedCommandLatenciesNanos, 0.99);
    }

    public long getAcceptedLatencySampleCount() {
        return acceptedCommandLatenciesNanos.size();
    }

    private double getOperationsPerSecond(long operationCount) {
        if (elapsedNanos == 0L) {
            return operationCount == 0L ? 0.0 : Double.POSITIVE_INFINITY;
        }
        return operationCount * 1_000_000_000.0 / elapsedNanos;
    }

    private static long getPercentileLatencyNanos(List<Long> latenciesNanos, double percentile) {
        if (latenciesNanos.isEmpty()) {
            return 0L;
        }

        List<Long> sortedLatenciesNanos = new ArrayList<>(latenciesNanos);
        Collections.sort(sortedLatenciesNanos);

        int rank = (int) Math.ceil(percentile * sortedLatenciesNanos.size());
        int index = Math.max(0, rank - 1);
        return sortedLatenciesNanos.get(index);
    }

    public String toSummaryString() {
        return String.join(System.lineSeparator(),
            "totalLinesRead=" + totalLinesRead,
            "acceptedCommands=" + acceptedCommands,
            "acceptedAdds=" + acceptedAdds,
            "acceptedCancels=" + acceptedCancels,
            "ignoredCommands=" + ignoredCommands,
            "tradeCount=" + tradeCount,
            "elapsedMillis=" + String.format(Locale.ROOT, "%.3f", getElapsedMillis()),
            "commandsPerSecond=" + String.format(Locale.ROOT, "%.3f", getCommandsPerSecond()),
            "p50LatencyNanos=" + getP50LatencyNanos(),
            "p50AddLatencyNanos=" + getP50AddLatencyNanos(),
            "p50CancelLatencyNanos=" + getP50CancelLatencyNanos(),
            "p95LatencyNanos=" + getP95LatencyNanos(),
            "p99LatencyNanos=" + getP99LatencyNanos()
        );
    }
}
