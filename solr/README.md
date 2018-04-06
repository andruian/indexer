# Solr related notes

This Solr configuration schema is used to initialize Solr to be able to handle Andruian data.

See [schema.xml](andruian_configset/conf/schema.xml) for details about the Solr schema.

## Create collection with andruian configset
```
.\bin\solr create -c collection_name -d /path/to/dir/andruian_configset
```

## Notes
- Solr behaves like a map in regards to uniquekey - adding a second item with the same key overwrites the first.
- Spring spatial queries: https://github.com/spring-projects/spring-data-solr/blob/master/README.md
- Spatial query in CrudRepository is argument-sensitive: https://jira.spring.io/browse/DATASOLR-451
- http://www.baeldung.com/spring-data-solr
