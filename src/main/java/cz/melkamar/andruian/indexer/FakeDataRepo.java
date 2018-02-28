package cz.melkamar.andruian.indexer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FakeDataRepo {
    @Value("${dataDefs}")
    private String[] dataDefs;

    public Property[] getData(){

        return new Property[]{
                new Property<String>("value", "prop1"),
                new Property<Integer>(1, "prop2"),
                new Property<Double>(3.14, "prop3"),
                new Property<Long>(666L, "prop4"),
                new Property<String>("value", "prop5"),
        };
    }

    public String[] getConfig(){
        return dataDefs;
    }
}
