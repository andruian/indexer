# Andruian Indexer

[![Build Status](https://travis-ci.org/andruian/indexer.svg?branch=master)](https://travis-ci.org/andruian/indexer)

## Installation
- The easiest way to set up the index server is using Docker. Have a look at the provided 
[docker-compose file](docker-compose.yml) which will get you up and running after just a few adjustments.
- You will want to configure data definition URLs, admin password and Solr/MongoDB/SPARQL URIs by supplying a 
`application.properties` file to the server. An example can be found [here](src/main/resources/application.properties).
The server will automatically use a property file when located in the working directory. Example:
```
/some/path$ ls
application.properties indexer-1.0.0.jar

/some/path$ java -jar indexer-1.0.0.jar 
```

TODO: provide release jar and application.properties

## REST API
#### Querying objects

- `GET /api/query`
- Query parameters:
    - `lat` 
        - 49.74468693637641
    - `long`
        - 13.37622390978595
    - `r`
        - Radius around the latlong position where to search for objects, in kilometers   
        - 1.011
    - `type`
        - Optional. Limit searched objects to those of this type
        - http-encoded-iri (IRI of an RDF class)
        
- Example: `http://localhost:8080/api/query?lat=49.74468693637641&long=13.37622390978595&r=1.011`
- Response: 
```json
[
  {
    "iri": "http://src.com/https%3A%2F%2Fruian.linked.opendata.cz%2Fzdroj%2Fadresní-místa%2F21909423",
    "type": "http://example.org/SourceObjectA",
    "locationObjectIri": "https://ruian.linked.opendata.cz/zdroj/adresní-místa/21909423",
    "label": "Adamovská 1",
    "properties": {
      "StreetName": "Adamovská",
      "PSC": "14000",
      "StreetNum": "1"
    },
    "longPos": 14.45133,
    "latPos": 50.058922
  },
  {
    "iri": "http://src.com/https%3A%2F%2Fruian.linked.opendata.cz%2Fzdroj%2Fadresní-místa%2F21908761",
    "type": "http://example.org/SourceObjectA",
    "locationObjectIri": "https://ruian.linked.opendata.cz/zdroj/adresní-místa/21908761",
    "label": "Adamovská 5",
    "properties": {
      "StreetName": "Adamovská",
      "PSC": "14000",
      "StreetNum": "5"
    },
    "longPos": 14.451405,
    "latPos": 50.05922
  }
]
```