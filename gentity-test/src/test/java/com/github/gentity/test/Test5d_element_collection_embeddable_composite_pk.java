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

import com.github.gentity.test.test5d_element_collection_embeddable_composite_pk.Partyguest;
import java.util.Arrays;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author count
 */
public class Test5d_element_collection_embeddable_composite_pk extends AbstractGentityTest {
	@Test
	public void test() {
		Partyguest guest1 = Partyguest.builder()
			.firstname("Scarlett")
			.surname("Johansson")
			.primaryinvitee(true)
			.phoneNumber(Arrays.asList(
				"2222222",
				"666"
			))
			.build();
		em.persist(guest1);
		
		Partyguest result = em.createQuery("SELECT g FROM Partyguest g WHERE g.firstname='Scarlett'", Partyguest.class)
			.getSingleResult();
		
		assertEquals(2, result.getPhoneNumber().size());
		assertTrue(result.getPhoneNumber().contains("2222222"));
		assertTrue(result.getPhoneNumber().contains("666"));
			
	}
}
