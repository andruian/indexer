package cz.melkamar.andruian.indexer.service;

import cz.melkamar.andruian.indexer.config.IndexerConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = {
    "indexing.cron = * * * * * ?"
})
public class IndexCronTest {

    @MockBean private IndexService indexService;
    @Autowired private IndexCron indexCron;
    @Autowired private IndexerConfiguration configuration;

    @Test
    public void triggerReindex() throws InterruptedException {
        assertEquals("* * * * * ?", configuration.getIndexingCron());
        Thread.sleep(3000);
        Mockito.verify(indexService, Mockito.atLeast(1)).reindexAll(false);
    }
}