/*
 * Copyright 2019 The Gentity Project. All rights reserved.
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

import com.github.gentity.test.test6a_inheritance_basic_tables.Car;
import com.github.gentity.test.test6a_inheritance_basic_tables.Human;
import com.github.gentity.test.test6a_inheritance_basic_tables.Person;
import com.github.gentity.test.test6a_inheritance_basic_tables.Taxpayer;
import com.github.gentity.test.test6a_inheritance_basic_tables.Thing;
import com.github.gentity.test.test6a_inheritance_basic_tables.VehicleBase;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author count
 */
public class Test6a_inheritance_basic_tables  extends AbstractGentityTest {
	@Test
	public void test() {
		em.persist(Person.builder()
			.name("Einstein")
			.build()
		);
		
		em.persist(Person.builder()
			.name("Planck")
			.build()
		);
		
		// there isn't much to test here, as long as this compiles and 
		// doesn't throw exceptions at runtime we're good
		Person planck = em.createNamedQuery(Person.FIND_PERSON_WITH_NAME, Person.class)
			.setParameter("name", "Planck")
			.getSingleResult();
		assertFalse(planck instanceof Thing);
		assertTrue(planck instanceof Human);
		assertTrue(planck instanceof Taxpayer);
		
		// make sure that the given instance is an instance of Vehicle -
		// again, this is a compile time check
		VehicleBase vehicle = Car.builder()
			.manufacturer("Ford")
			.model("Fiesta")
			.buildWithId(1);
		assertTrue(vehicle instanceof Thing);
		assertFalse(vehicle instanceof Human);
		assertFalse(vehicle instanceof Taxpayer);
	}
		
	
}
