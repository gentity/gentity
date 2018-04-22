# gentity [![Build Status](https://travis-ci.org/gentity/gentity.svg?branch=master)](https://travis-ci.org/gentity/gentity) ![Maven metadata URI](https://img.shields.io/maven-metadata/v/http/central.maven.org/maven2/com/github/gentity/gentity-maven-plugin/maven-metadata.xml.svg)
JPA Entity class generator from DBSchema (*.dbs) database models (see https://www.dbschema.com)

## Features
* Automatic entity generation from table schema
* Automatically derives:
  - Entity fields from column definitions
  - OneToMany relations from foreign key and index definitions
* Direct automatic entity generation by using a mapping definition file, which
defines:
  - Join tables to create @ManyToMany relationships
  - Inheritance hierarchies

## TODO

* Single Table and Per Class inheritance
* Non-Entity collections (@ElementCollection / @Embeddable)
* Exclude discriminator columns from being mapped in generated entities
* External (non-generated) superclasses for generated entities

Maven Central shield image from [shields.io](https://shields.io/)

