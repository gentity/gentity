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

import com.github.gentity.test.test4b_polymorphism_single_table.*;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author upachler
 */
public class Test4b_polymorphism_single_table extends AbstractGentityTest{
	

	@Test
	public void test() {
		
		em.persist(Mammal.builder()
			.furry(true)
			.name("ginea pig")
			.weight(0.2)
			.build(1L)
		);
		
		em.persist(Mammal.builder()
			.furry(false)
			.name("elephant")
			.weight(1000.0)
			.buildWithId(2L)
		);
		
		em.persist(Insect.builder()
			.name("bee")
			.stingy(true)
			.weight(0.001)
			.buildWithId(3L)
		);
		
		Lifeform bee = em.createQuery("SELECT l FROM Lifeform l WHERE l.name='bee'", Lifeform.class)
			.getSingleResult();
		assertTrue(bee instanceof Insect);
		
		Lifeform elephant = em.createQuery("SELECT l FROM Lifeform l WHERE l.weight > 500", Lifeform.class)
			.getSingleResult();
		assertTrue(elephant instanceof Mammal);
		
		Lifeform gineaPig = em.createQuery("SELECT m FROM Mammal m WHERE m.furry=true", Mammal.class)
			.getSingleResult();
		assertEquals("ginea pig", gineaPig.getName());
	}
}
