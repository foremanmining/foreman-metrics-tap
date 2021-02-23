package mn.foreman.metricstap;

import mn.foreman.model.Miner;
import mn.foreman.model.miners.MinerStats;
import mn.foreman.model.miners.asic.Asic;

import com.opencsv.CSVWriter;
import lombok.Builder;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * A {@link MetricsTask} provides a scheduled task that will periodically
 * capture stats from a miner and dump them to a file.
 */
@Component
public class MetricsTask {

    /** The logger for this class. */
    private static final Logger LOG =
            LoggerFactory.getLogger(MetricsTask.class);

    /** The writer for producing output. */
    private final CSVWriter csvWriter;

    /** The fan count. */
    private final int fanCount;

    /** The miner to query. */
    private final Miner miner;

    /** The stats that were captured and are to be written to disk. */
    private final BlockingQueue<AsicStats> stats = new LinkedBlockingQueue<>();

    /** The temp count. */
    private final int tempCount;

    /**
     * Constructor.
     *
     * @param fanCount  The number of fans.
     * @param tempCount The number of temps.
     * @param miner     The miner.
     * @param csvWriter The writer.
     */
    @Autowired
    public MetricsTask(
            @Value("${fanCount}") final int fanCount,
            @Value("${tempCount}") final int tempCount,
            final Miner miner,
            final CSVWriter csvWriter) {
        this.fanCount = fanCount;
        this.tempCount = tempCount;
        this.miner = miner;
        this.csvWriter = csvWriter;
    }

    /** Writes the captured stats to the CSV file. */
    @Scheduled(fixedRateString = "${dumpFrequencyInMillis}")
    public void dumpStats() {
        try {
            final List<AsicStats> asicStats =
                    new ArrayList<>(this.stats.size());
            this.stats.drainTo(asicStats);

            final List<String[]> rows =
                    asicStats
                            .stream()
                            .map(this::toArray)
                            .collect(Collectors.toList());

            this.csvWriter.writeAll(rows);
            this.csvWriter.flush();
        } catch (final Exception e) {
            LOG.warn("Failed to dump stats to file", e);
        }
    }

    /** Queries the miner for stats and adds them to the {@link #stats} queue. */
    @Scheduled(fixedRateString = "${queryFrequencyInMillis}")
    public void queryMiner() {
        try {
            final MinerStats stats = this.miner.getStats();
            final List<Asic> asics = stats.getAsics();
            if (!asics.isEmpty()) {
                final Asic asic = asics.get(0);
                LOG.debug("Obtained asic stats: {}", asic);
                this.stats.add(
                        AsicStats
                                .builder()
                                .timestamp(Instant.now())
                                .asic(asic)
                                .build());
            } else {
                LOG.warn("Failed to find any ASICs");
            }
        } catch (final Exception e) {
            LOG.warn("Failed to obtain metrics from miner", e);
        }
    }

    /**
     * Pads a list with 0s to align the CSV.
     *
     * @param toPad The list to pad.
     * @param count The desired size.
     *
     * @return The padded list.
     */
    private static List<Integer> pad(
            final List<Integer> toPad,
            final int count) {
        final List<Integer> result = new ArrayList<>(count);
        result.addAll(toPad);
        for (int i = 0; i < (count - toPad.size()); i++) {
            result.add(0);
        }
        return result;
    }

    /**
     * Converts the provided stats to a CSV array that can be written.
     *
     * @param stats The stats.
     *
     * @return The array of stats.
     */
    private String[] toArray(final AsicStats stats) {
        final Asic asic = stats.asic;
        final List<String> metrics = new LinkedList<>();
        metrics.add(stats.timestamp.toString());
        metrics.add(asic.getHashRate().toString());

        pad(asic.getFans().getSpeeds(), this.fanCount)
                .stream()
                .map(String::valueOf)
                .forEach(metrics::add);

        pad(asic.getTemps(), this.tempCount)
                .stream()
                .map(String::valueOf)
                .forEach(metrics::add);

        return metrics.toArray(new String[0]);
    }

    /** A simple POJO capturing stats and when they were obtained. */
    @Data
    @Builder
    private static class AsicStats {

        /** The stats. */
        private final Asic asic;

        /** When the metric was captured. */
        private final Instant timestamp;
    }
}
