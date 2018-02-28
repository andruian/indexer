package cz.melkamar.andruian.indexer.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:properties/indexer-configuration-test.properties")
public class IndexerConfigurationTest {


    @Autowired
    private IndexerConfiguration indexerConfiguration;

    @Test
    public void getDataDefUris() throws Exception {
        assertArrayEquals(new String[]{"https://foo.bar/something", "http://example.org/rdf"},
                          indexerConfiguration.getDataDefUris());
    }
    
    @Test
    public void getIndexingCron() throws Exception {
        assertEquals("0 0/5 * * * ?", indexerConfiguration.getIndexingCron());
    }

}