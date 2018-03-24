# Solr related notes

## Create collection with andruian configset
```
.\bin\solr create -c collection_name -d /path/to/dir/andruian_configset
```

## Notes
- Solr behaves like a map in regards to uniquekey - adding a second item with the same key overwrites the first.
- Spring spatial queries: https://github.com/spring-projects/spring-data-solr/blob/master/README.md
- Spatial query in CrudRepository is argument-sensitive: https://jira.spring.io/browse/DATASOLR-451
- http://www.baeldung.com/spring-data-solr


#### Geospatial indexing quickstart

- Download and extract Solr http://lucene.apache.org/solr/downloads.html

```bash
#
# Create Solr schema 
#
$ ./bin/solr create -c geostuff


#
# Add relevant fields to the schema
#   Field of type "location" is expected to be in "lat,long" format.
#
$ curl -X POST -H 'Content-type:application/json' --data-binary '{"add-field": {"name":"iri", "type":"text_general", "multiValued":false, "stored":true, "indexed":false}}' http://localhost:8983/solr/urisinglec/schema
$ curl -X POST -H 'Content-type:application/json' --data-binary '{"add-field": {"name":"location", "type":"location", "multiValued":false, "stored":true, "indexed":true}}' http://localhost:8983/solr/urisinglec/schema
$ curl -X POST -H 'Content-type:application/json' --data-binary '{"add-field": {"name":"type", "type":"location", "multiValued":false, "stored":true, "indexed":true}}'     http://localhost:8983/solr/urisinglec/schema


#
# Prepare and index data
#   Points are located randomly in Europe. One is near Pankrác, Prague.

$ cat << EOF > test-data.json
[
 {"iri":"https://melkamar.cz/example/1","location":"51.056491, 12.427080"},
 {"iri":"https://melkamar.cz/example/2","location":"52.056491, 14.427080"},
 {"iri":"https://melkamar.cz/example/3","location":"55.056491, 13.427080"},
 {"iri":"https://melkamar.cz/example/4","location":"50.056491, 11.427080"},
 {"iri":"https://melkamar.cz/example/5","location":"57.056491, 10.427080"},
 {"iri":"https://melkamar.cz/example/6","location":"50.056491, 24.427080"},
 {"iri":"https://melkamar.cz/example/7","location":"53.056491, 14.427080"},
 {"iri":"https://melkamar.cz/example/8","location":"53.056491, 21.427080"},
 {"iri":"https://melkamar.cz/example/9","location":"50.056491, 15.427080"},
 {"iri":"https://melkamar.cz/example/10","location":"51.056491, 15.427080"},
 {"iri":"https://melkamar.cz/example/11","location":"52.056491, 16.427080"},
 {"iri":"https://melkamar.cz/example/12","location":"40.056491, 13.427080"}
]
EOF

$ ./bin/post -c geostuff test-data.json

#
# Query data
#   Make a request: http://localhost:8983/solr/geostuff/select?d=200&fq={!bbox%20sfield=location}&pt=50.052828,14.439898&q=*:*
#   Below is curl command (with escaped url params)
#

$ curl -G http://localhost:8983/solr/geostuff/select --data-urlencode 'd=200' --data-urlencode 'fq={!bbox sfield=location}' --data-urlencode 'pt=50.052828,14.439898' --data-urlencode 'q=*:*'

{
  "responseHeader":{
    "zkConnected":true,
    "status":0,
    "QTime":3,
    "params":{
      "q":"*:*",
      "d":"200",
      "pt":"50.052828,14.439898",
      "fq":"{!bbox sfield=location}"}},
  "response":{"numFound":3,"start":0,"docs":[
      {
        "iri":"https://melkamar.cz/example/1",
        "location":"51.056491, 12.427080",
        "id":"38531f48-e8fc-4a41-95cc-451ed2190ab0",
        "_version_":1592835477462843392},
      {
        "iri":"https://melkamar.cz/example/9",
        "location":"50.056491, 15.427080",
        "id":"c6b690da-566e-433a-8330-8198abaac158",
        "_version_":1592835477471232000},
      {
        "iri":"https://melkamar.cz/example/10",
        "location":"51.056491, 15.427080",
        "id":"bafb39c1-78cb-419a-86c6-0fcb42d341e2",
        "_version_":1592835477471232001}]
  }}

```
