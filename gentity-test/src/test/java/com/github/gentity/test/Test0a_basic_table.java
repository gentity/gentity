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

import com.github.gentity.test.test0a_basic_table.Person;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author upachler
 */
public class Test0a_basic_table extends AbstractGentityTest {

	
	@Test
	public void test() {
		em.persist(
			Person.builder()
			.id(1)
			.firstname("Albert")
			.surname("Einstein")
			.build()
		);
		em.persist(
			Person.builder()
			.id(2)
			.firstname("Max")
			.surname("Planck")
			.build()
		);
		em.persist(
			Person.builder()
			.id(3)
			.firstname("Nils")
			.surname("Bohr")
			.build()
		);
		
		em.flush();
		
		Person p = em.createQuery("SELECT p FROM Person p WHERE p.firstname='Max'", Person.class)
			.getSingleResult();
		
		Assert.assertEquals("Planck", p.getSurname());
			
	}
}
