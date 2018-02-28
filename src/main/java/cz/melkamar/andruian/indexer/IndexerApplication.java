package cz.melkamar.andruian.indexer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class IndexerApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexerApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(IndexerApplication.class, args);

		String value = "xyz";
        LOGGER.trace("trace");
        LOGGER.debug("debug");
        LOGGER.info("info");
        LOGGER.warn("warn");
        LOGGER.error("error");
	}
}
