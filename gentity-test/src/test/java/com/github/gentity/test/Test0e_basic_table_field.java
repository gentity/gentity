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
import com.github.gentity.test.test0e_basic_table_field.Person;
import java.util.List;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 *
 * @author count
 */
public class Test0e_basic_table_field extends AbstractGentityTest{
	
	@Test
	public void test() {
		em.persist(Person.builder()
			.firstName("Mickey")
			.surname("Mouse")
			.build()
		);
		
		List<Person> p = em.createQuery("SELECT p FROM Person p WHERE p.firstName='Mickey'")
			.getResultList();
		
		assertEquals(1, p.size());
	}
}
