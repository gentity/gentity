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

import com.github.gentity.test.test4d_polymorphism_single_table_relations.*;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author upachler
 */
public class Test4d_polymorphism_single_table_relations extends AbstractGentityTest{
	

	@Test
	public void test() {
		
		Kennel kennel = Kennel.builder()
			.name("dog2 kennel")
			.build();
		em.persist(kennel);
		
		em.persist(Mammal.builder()
			.furry(false)
			.name("elephant")
			.weight(1000.0)
			.build()
		);
		
		em.persist(Insect.builder()
			.name("bee")
			.stingy(true)
			.weight(0.001)
			.build()
		);
		
		Dog dog1 = Dog.builder()
			.name("dog1")
			.furry(Boolean.TRUE)
			.build();
		Dog dog2 = Dog.builder()
			.name("dog2")
			.furry(Boolean.TRUE)
			.build();
		
		dog1.setLivesInKennel(kennel);
		kennel.getDog().add(dog1);
		dog2.setLivesInKennel(kennel);
		kennel.getDog().add(dog2);
		
		dog2.getDogAccessories().add(kennel);
		kennel.setOwnedByDogLifeform(dog2);
		
		
		
		em.persist(dog1);
		em.persist(dog2);
		
		dog1 = null;
		dog2 = null;
		
		dog1 = em.createQuery("SELECT d FROM Dog d WHERE d.name='dog1'", Dog.class)
			.getSingleResult();
		
		assertTrue(dog1.getDogAccessories().isEmpty());
		assertEquals("dog2 kennel", dog1.getLivesInKennel().getName());
		
		dog2 = (Dog)em.createQuery("SELECT d FROM Lifeform d WHERE d.name='dog2'", Lifeform.class)
			.getSingleResult();
		
		assertEquals(1, dog2.getDogAccessories().size());
		assertEquals("dog2 kennel", dog1.getLivesInKennel().getName());
	}
}
