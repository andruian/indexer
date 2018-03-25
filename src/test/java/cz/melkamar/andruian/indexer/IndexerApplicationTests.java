package cz.melkamar.andruian.indexer;

import cz.melkamar.andruian.indexer.dao.SolrPlaceRepository;
import cz.melkamar.andruian.indexer.model.place.Place;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class IndexerApplicationTests {
    @Test
    public void contextLoads() {
    }
}
