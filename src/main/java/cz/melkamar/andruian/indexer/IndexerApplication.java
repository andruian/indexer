package cz.melkamar.andruian.indexer;

import cz.melkamar.andruian.indexer.service.PostStartupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class IndexerApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexerApplication.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(IndexerApplication.class, args);

        LOGGER.trace("trace");
        LOGGER.debug("debug");
        LOGGER.info("info");
        LOGGER.warn("warn");
        LOGGER.error("error");

        context.getBean(PostStartupService.class).postStartup();
        System.exit(0);
    }
}
