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

import com.github.gentity.test.test0c_basic_table_autoincrement.Person;
import java.util.logging.Logger;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author upachler
 */
public class Test0c_basic_table_autoincrement extends AbstractGentityTest {

	Logger logger = Logger.getLogger(getClass().getName());
	
	@Test
	public void test() {
		logger.severe("test-severe");
		logger.fine("test-fine");
		
		assertTrue(hasClassDeclaredField(Person.class, Long.class, "id"));
		assertTrue(hasClassDeclaredMethod(Person.class, Long.class, "getId"));
		assertFalse(hasClassDeclaredMethod(Person.class, void.class, "setId", Long.class));

		em.persist(
			Person.builder()
			.firstname("Albert")
			.surname("Einstein")
			.build()
		);
		em.persist(
			Person.builder()
			.firstname("Max")
			.surname("Planck")
			.build()
		);
		em.persist(
			Person.builder()
			.firstname("Nils")
			.surname("Bohr")
			.build()
		);
		
		em.flush();
		
		Person p = em.createQuery("SELECT p FROM Person p WHERE p.firstname='Max'", Person.class)
			.getSingleResult();
		
		assertEquals("Planck", p.getSurname());
			
	}
}
