/*
 * Copyright 2018 The Gentity Project. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.gentity.test;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.hsqldb.jdbc.JDBCDriver;
import org.junit.After;
import org.junit.Before;

/**
 *
 * @author count
 */
public class AbstractGentityTest {
	
	/*
	If this is set to true, it will use the external hsqldb that is specfied
	in EXTERNAL_HSQLDB_URL. To run the tests from the external hsqldb, do this
	(but beware, rollback is disabled in this case):
	
	* download hsqldb-*.zip from http://hsqldb.org (alternatively, use the jar 
	  from your local maven repo, but you'll need to adjust the paths below)
	* unzip the file into a directory that we'll call $HSQLDIR.
	* cd into $HSQLDIR
	* In shell window 1, we run the server:
	  - 'cd $HSQLDIR/data' (the data directory unpacked from your zip file)
	  - run 'java -cp ../lib/hsqldb.jar org.hsqldb.Server -database.0 file:mydb -dbname.0 xdb'
	  - keep the server running as long as you want the database around;
	  - to clear the database, shutdown the server and delete files in $HSQLDIR/data
	* In shell window 2, we run the HSQLDB Manager tool:
	  - 'cd $HSQLDIR'
	  - 'java -cp lib/hsqldb.jar org.hsqldb.util.DatabaseManagerSwing'
	  - As JDBC URL, use 'jdbc:hsqldb:hsql://localhost/xdb;shutdown=false;autocommit=true'
	    (the same as the value in the member variable EXTERNAL_HSQLDB_URL)
	*/
	private static final boolean EXTERNAL_HSQLDB_ENABLED = false;
	
	private static final String EXTERNAL_HSQLDB_URL = "jdbc:hsqldb:hsql://localhost/xdb;shutdown=false;autocommit=true";
	
	private static final String INTERNAL_HSQLDB_URL_FORMAT = "jdbc:hsqldb:mem:%s;shutdown=false;autocommit=true";
	
	private final String persistenceUnitName;
	
	protected EntityManager em;
	protected EntityManagerFactory emf;

	public AbstractGentityTest() {
		this.persistenceUnitName = getClass().getSimpleName();
	}

	
	@Before
	public void beforeTest() {
		String jdbcUrl = EXTERNAL_HSQLDB_ENABLED ? EXTERNAL_HSQLDB_URL : String.format(INTERNAL_HSQLDB_URL_FORMAT, persistenceUnitName);
		Map<String, String> emProperties = new HashMap<String, String>() {
			{
				put("javax.persistence.jdbc.driver", JDBCDriver.class.getName());
				put("javax.persistence.jdbc.url", jdbcUrl);
				put("javax.persistence.jdbc.user", "SA");
				put("javax.persistence.jdbc.password", "");
				put("eclipselink.ddl-generation", "create-tables");
			}
		};
		emf = Persistence.createEntityManagerFactory(persistenceUnitName, emProperties);
		em = emf.createEntityManager();

		em.getTransaction().begin();
	}
	
	@After
	public void afterTest() {
		if(EXTERNAL_HSQLDB_ENABLED) {
			// commit transaction when configured for an external HSQLDB, so
			// the changes a test makes are visible
			em.getTransaction().commit();
		} else {
			em.getTransaction().rollback();
		}
		
		em.createNativeQuery("SHUTDOWN");
		em.clear();
		emf.close();
	}
	
	protected EntityManager em() {
		return em;
	}

	protected boolean hasClassDeclaredField(Class clazz, String fieldName) {
		try {
			clazz.getDeclaredField(fieldName);
			return true;
		} catch (NoSuchFieldException nsfx) {
			return false;
		}
	}
}
