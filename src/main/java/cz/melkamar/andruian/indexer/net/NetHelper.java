package cz.melkamar.andruian.indexer.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * A helper class for network-related functionality - HTTP requests.
 */
@Component
public class NetHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(NetHelper.class);
    private final RestTemplate restTemplate;

    @Autowired
    public NetHelper(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String httpGet(String uri) {
        return httpGet(UriComponentsBuilder.fromUriString(uri).build().toUri());
    }

    public String httpGetFromEncodedUri(String uri) {
        return httpGet(UriComponentsBuilder.fromUriString(uri).build(true).toUri());
    }

    private String httpGet(URI uri) {
        LOGGER.info("HTTP GET {}", uri);

        String payload = restTemplate.getForObject(uri, String.class);
        LOGGER.trace("Downloaded payload:");
        LOGGER.trace(payload);
        return payload;
    }
}
