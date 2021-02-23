package mn.foreman.metricstap;

import mn.foreman.antminer.AntminerFactory;
import mn.foreman.model.Miner;

import com.google.common.collect.ImmutableMap;
import com.opencsv.CSVWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

/** The configuration for metrics tap. */
@Configuration
public class Config {

    /**
     * Creates a writer for writing the output file.
     *
     * @param identifier The idenfier for this run.
     *
     * @return The writer for output.
     *
     * @throws IOException on failure.
     */
    @Bean
    public CSVWriter csvWriter(
            final String identifier,
            @Value("${fanCount}") final int fanCount,
            @Value("${tempCount}") final int tempCount) throws IOException {
        final CSVWriter csvWriter =
                new CSVWriter(
                        new FileWriter(
                                identifier.concat(".csv"),
                                true));

        // Write the header
        final List<String> headers = new LinkedList<>();
        headers.add("timestamp");
        headers.add("hash_rate_hs");
        IntStream
                .range(0, fanCount)
                .mapToObj(val -> "fan" + val)
                .forEach(headers::add);
        IntStream
                .range(0, tempCount)
                .mapToObj(val -> "temp" + val)
                .forEach(headers::add);
        csvWriter.writeNext(headers.toArray(new String[0]));
        csvWriter.flush();

        return csvWriter;
    }

    /**
     * Creates a random identifier for this run.
     *
     * @return The random identifier.
     */
    @Bean
    public String identifier() {
        return UUID.randomUUID().toString();
    }

    /**
     * Creates the miner that will be queried.
     *
     * @param ip       The ip.
     * @param username The username.
     * @param password The password.
     *
     * @return The miner that will be queried.
     */
    @Bean
    public Miner miner(
            @Value("${minerIp}") final String ip,
            @Value("${minerUsername}") final String username,
            @Value("${minerPassword}") final String password) {
        final AntminerFactory antminerFactory =
                new AntminerFactory(
                        BigDecimal.ONE);
        return antminerFactory.create(
                ImmutableMap.of(
                        "apiIp",
                        ip,
                        "apiPort",
                        "4028",
                        "username",
                        username,
                        "password",
                        password));
    }
}
