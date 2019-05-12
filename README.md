# gentity [![Build Status](https://travis-ci.org/gentity/gentity.svg?branch=master)](https://travis-ci.org/gentity/gentity) ![Maven metadata URI](https://img.shields.io/maven-metadata/v/http/central.maven.org/maven2/com/github/gentity/gentity-maven-plugin/maven-metadata.xml.svg)

JPA Entity class generator from DBSchema (*.dbs) database models (see https://www.dbschema.com). 

The Workflow:
1. Create/modify database model in modeller took (DbSchema)
2. Run gentity from Maven or directly from command line to generate entity classes

So, Gentity doesn't need a running database; entity classes are generated directly from the modeller file.

## Features
* Automatic entity generation from table schema
* Automatically derives:
  - Entity fields from column definitions
  - OneToMany / OneToOne / ManyToMany relations from foreign key and index definitions
  - Non-Entity collections (@ElementCollection / @Embeddable)
* Additional mapping definition file, to
  - Override derived definitions from database modeller file
  - Define inheritance hierarchies (supported strategies are JOINED and SINGLE_TABLE)
* Bidirectional and unidirectional relationships are supported

## Quick Start

Add this to your `pom.xml`'s `<build>`/`<plugins>` section:

```xml
<plugin>
    <groupId>com.github.gentity</groupId>
    <artifactId>gentity-maven-plugin</artifactId>
    <version>0.17</version>  <!-- replace with newest release version -->
    <executions>
        <execution>
            <goals>
                <goal>generate</goal>
            </goals>
            <configuration>
                <inputDbsFile>my-db-model.dbs</inputDbsFile>
            </configuration>
        </execution>
    </executions>
</plugin>
```

You'll also need the gentity library (`gentity-lib`):

```xml
<dependency>
    <groupId>com.github.gentity</groupId>
    <artifactId>gentity-lib</artifactId>
    <version>0.17</version>  <!-- must match the gentity plugin version -->
</dependency>
```

Note that you'll also need your ORM's libraries (EclipseLink, Hibernate, etc.), or at least the JPA API:

## TODO

* Support for unidirectional OneToMany (this has issues attached, as standard
  JPA defaulting requires a join table - however, JPA 2.2 also supports them
  without one, but that has implications)
* Per Class inheritance
* External (non-generated) superclasses for generated entities
* Provide better error output, especially for the mapping config file explaining
  where in the file to look for errors. See this post on how to do this for
  our deserialized mapping config info:
  https://stackoverflow.com/questions/7079796/jaxb-location-in-file-for-unmarshalled-objects
* Mutual update of bidirectional relations (when one side is updated, the other is updated automagically)

Maven Central shield image from [shields.io](https://shields.io/)

