package cz.melkamar.andruian.indexer.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
public class IndexerConfigurationTest {


    @Autowired
    private IndexerConfiguration indexerConfiguration;

    @Test
    public void getIndexingCron() throws Exception {
        assertEquals("0 0/5 * * * ?", indexerConfiguration.getIndexingCron());
    }

}