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

	private final String persistenceUnitName;
	
	protected EntityManager em;
	protected EntityManagerFactory emf;

	public AbstractGentityTest() {
		this.persistenceUnitName = getClass().getSimpleName();
	}

	
	@Before
	public void beforeTest() {
		Map<String, String> emProperties = new HashMap<String, String>() {
			{
				put("javax.persistence.jdbc.driver", JDBCDriver.class.getName());
				put("javax.persistence.jdbc.url", "jdbc:hsqldb:mem:test;shutdown=false;autocommit=true");
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
		em.getTransaction().rollback();

		em.createNativeQuery("SHUTDOWN");
		em.clear();
		emf.close();
	}
	
	protected EntityManager em() {
		return em;
	}
}
