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

import com.github.gentity.test.test5b_element_collection_embeddable_inheritance.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author upachler
 */
public class Test5b_element_collection_embeddable_inheritance extends AbstractGentityTest{
	
	
	@Test
	public void test() {
		
		// make sure that PhoneNumber is generated as ebeddable, NOT as entity
		assertTrue(hasClassAnnotation(com.github.gentity.test.test5a_element_collection_embeddable.PhoneNumber.class, Embeddable.class));
		assertFalse(hasClassAnnotation(com.github.gentity.test.test5a_element_collection_embeddable.PhoneNumber.class, Entity.class));
		
		List<PhoneNumber> johnsPhoneNumbers = Arrays.asList(
			PhoneNumber.builder()
				.phoneNum("12345")
				.mobile(true)
				.build(),
			PhoneNumber.builder()
				.phoneNum("54321")
				.mobile(false)
				.build()
		);
		em.persist(Person.builder()
			.firstName("John")
			.lastName("Doe")
			.phoneNumber(new ArrayList<>(johnsPhoneNumbers))
			.build()
		);
		
		Person john = em.createQuery("SELECT p FROM Person p WHERE p.firstName='John'", Person.class)
			.getSingleResult();
		
		assertEquals(new HashSet<>(johnsPhoneNumbers), new HashSet<>(john.getPhoneNumber()));
		
		// john lost his mobiles..
		john.getPhoneNumber()
			.removeIf(p -> p.getMobile());
		
		john = em.createQuery("SELECT p FROM Person p WHERE p.firstName='John'", Person.class)
			.getSingleResult();
		
		assertEquals(1, john.getPhoneNumber().size());
		assertEquals("54321", john.getPhoneNumber().get(0).getPhoneNum());
		
		em.createQuery("SELECT p FROM Person p JOIN p.phoneNumber n WHERE n.phoneNum='54321'", Person.class)
			.getSingleResult()
			;
		
	}
}
