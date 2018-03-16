package cz.melkamar.andruian.indexer.dao;

import cz.melkamar.andruian.ddfparser.model.DataDef;
import cz.melkamar.andruian.indexer.Util;
import cz.melkamar.andruian.indexer.net.DataDefFetcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DataDefFetcherTest {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DataDefFetcher dataDefFetcher;

    MockRestServiceServer mockServer;

    @Before
    public void setUp() throws Exception {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    /**
     * Set up a mock server and respond with the contents of rdf/test-parse-datadef.ttl test file.
     */
    @Test
    public void getDataDef() throws Exception {
        String testUri = "https://example.org/rdf";
        String payload = Util.readStringFromResource("rdf/test-parse-datadef.ttl", this.getClass());
        mockServer
                .expect(MockRestRequestMatchers.requestTo(testUri))
                .andRespond(MockRestResponseCreators.withSuccess(payload, MediaType.valueOf("text/plain")));

        List<DataDef> dataDef = dataDefFetcher.getDataDefsFromUri(testUri);
        assertNotNull(dataDef);
        assertEquals(1, dataDef.size());
        assertEquals("http://foo/dataDef", dataDef.get(0).getUri());
    }

}