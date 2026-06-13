import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestReplayStats {
    @Test
    public void testLatencyPercentilesAreZeroWhenThereAreNoAcceptedCommands() {
        ReplayStats stats = new ReplayStats();

        assertEquals(0L, stats.getP50LatencyNanos());
        assertEquals(0L, stats.getP50AddLatencyNanos());
        assertEquals(0L, stats.getP50CancelLatencyNanos());
        assertEquals(0L, stats.getP95LatencyNanos());
        assertEquals(0L, stats.getP99LatencyNanos());
        assertEquals(0L, stats.getAcceptedLatencySampleCount());
    }

    @Test
    public void testLatencyPercentilesMatchSingleSample() {
        ReplayStats stats = new ReplayStats();

        stats.recordAcceptedAdd(0, 5_000L);

        assertEquals(5_000L, stats.getP50LatencyNanos());
        assertEquals(5_000L, stats.getP50AddLatencyNanos());
        assertEquals(0L, stats.getP50CancelLatencyNanos());
        assertEquals(5_000L, stats.getP95LatencyNanos());
        assertEquals(5_000L, stats.getP99LatencyNanos());
        assertEquals(1L, stats.getAcceptedLatencySampleCount());
    }

    @Test
    public void testLatencyPercentilesUseNearestRankForMultipleSamples() {
        ReplayStats stats = new ReplayStats();

        stats.recordAcceptedAdd(0, 1_000L);
        stats.recordAcceptedAdd(0, 5_000L);
        stats.recordAcceptedCancel(5_000L);
        stats.recordAcceptedCancel(7_000L);
        stats.recordAcceptedAdd(0, 9_000L);

        assertEquals(5_000L, stats.getP50LatencyNanos());
        assertEquals(5_000L, stats.getP50AddLatencyNanos());
        assertEquals(5_000L, stats.getP50CancelLatencyNanos());
        assertEquals(9_000L, stats.getP95LatencyNanos());
        assertEquals(9_000L, stats.getP99LatencyNanos());
        assertEquals(5L, stats.getAcceptedLatencySampleCount());
    }

    @Test
    public void testIgnoredCommandsDoNotAffectLatencyPercentiles() {
        ReplayStats stats = new ReplayStats();

        stats.recordAcceptedAdd(0, 2_000L);
        stats.recordIgnoredCommand();
        stats.recordIgnoredCommand();
        stats.recordAcceptedCancel(8_000L);

        assertEquals(2_000L, stats.getP50LatencyNanos());
        assertEquals(2_000L, stats.getP50AddLatencyNanos());
        assertEquals(8_000L, stats.getP50CancelLatencyNanos());
        assertEquals(8_000L, stats.getP95LatencyNanos());
        assertEquals(8_000L, stats.getP99LatencyNanos());
        assertEquals(2L, stats.getAcceptedLatencySampleCount());
        assertEquals(2L, stats.getIgnoredCommands());
    }
}
