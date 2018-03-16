# Andruian Indexer

[![Build Status](https://travis-ci.org/andruian/indexer.svg?branch=master)](https://travis-ci.org/andruian/indexer)

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
        - http-encoded-uri (IRI of an RDF class)
        
- Example: `http://localhost:8080/api/query?lat=49.74468693637641&long=13.37622390978595&r=1.011`
- Response: 
```json
[
  {
    "latPos": 49.74118888947373,
    "longPos": 13.37026010049526,
    "uri": "http://example.org/linkedobject-24540838",
    "classUri": "http://example.org/MyObject",
    "locationObjectUri": "http://ruian.linked.opendata.cz/resource/adresni-mista/24540838",
    "properties": [
      {
        "name": "labelForIdProperty",
        "value": 24540838
      }
    ]
  },
  {
    "latPos": 49.73968750921973,
    "longPos": 13.367628108214324,
    "uri": "http://example.org/linkedobject-24541753",
    "classUri": "http://example.org/MyObject",
    "locationObjectUri": "http://ruian.linked.opendata.cz/resource/adresni-mista/24541753",
    "properties": [
      {
        "name": "labelForIdProperty",
        "value": 24541753
      }
    ]
  }
]
```