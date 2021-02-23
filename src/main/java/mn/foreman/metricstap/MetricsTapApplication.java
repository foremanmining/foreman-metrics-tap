package mn.foreman.metricstap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * A metric-tap provides a simple application that will capture stats from an
 * Antminer and write them to a CSV file.
 */
@SpringBootApplication
@EnableScheduling
public class MetricsTapApplication {

    /**
     * Application entry point.
     *
     * @param args The command line arguments.
     */
    public static void main(final String[] args) {
        SpringApplication.run(MetricsTapApplication.class, args);
    }
}