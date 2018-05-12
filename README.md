# gentity [![Build Status](https://travis-ci.org/gentity/gentity.svg?branch=master)](https://travis-ci.org/gentity/gentity) ![Maven metadata URI](https://img.shields.io/maven-metadata/v/http/central.maven.org/maven2/com/github/gentity/gentity-maven-plugin/maven-metadata.xml.svg)
JPA Entity class generator from DBSchema (*.dbs) database models (see https://www.dbschema.com)

## Features
* Automatic entity generation from table schema
* Automatically derives:
  - Entity fields from column definitions
  - OneToMany / OneToOne relations from foreign key and index definitions
* Direct automatic entity generation by using a mapping definition file, which
defines:
  - Join tables to create @ManyToMany relationships
  - Inheritance hierarchies (supported strategies are JOINED and SINGLE_TABLE)
* Bidirectional and unidirectional relationships are supported

## TODO

* Support for unidirectional OneToMany (this has issues attached, as standard
  JPA defaulting requires a join table - however, JPA 2.2 also supports them
  without one, but that has implications)
* Per Class inheritance
* Non-Entity collections (@ElementCollection / @Embeddable)
* External (non-generated) superclasses for generated entities
* Provide better error output, especially for the mapping config file explaining
  where in the file to look for errors. See this post on how to do this for
  our deserialized mapping config info:
  https://stackoverflow.com/questions/7079796/jaxb-location-in-file-for-unmarshalled-objects


Maven Central shield image from [shields.io](https://shields.io/)

