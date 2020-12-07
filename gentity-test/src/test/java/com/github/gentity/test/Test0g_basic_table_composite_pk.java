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

import com.github.gentity.test.test0g_basic_table_composite_pk.Party;
import com.github.gentity.test.test0g_basic_table_composite_pk.Partyguest;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import com.github.gentity.test.test0g_basic_table_composite_pk.PartyguestId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 *
 * @author count
 */
public class Test0g_basic_table_composite_pk extends AbstractGentityTest {
	@Test
	public void test() {
		// Party will get an IdClass generated
		Party p1 = Party.builder()
			.name("Beerfest")	// PK
			.town("SmallTown")	// PK
			.theme("Beer")
			.description("Celebrate the hops")
			.build();
		em.persist(p1);
		
		Party p2 = Party.builder()
			.name("Winefest")	// PK
			.town("BigCity")	// PK
			.theme("Wine")
			.description("Red Red Wine...")
			.build();
		em.persist(p2);
		
		Party.Id pid = new Party.Id("Beerfest", "SmallTown");
		Party p = em.find(Party.class, pid);
		assertEquals(pid, new Party.Id(p1.getName(), p1.getTown()));
		assertFalse(pid.equals(new Party.Id(p2.getName(), p2.getTown())));
		
		// Partyguest has the 'idClass' attibute set, so gentity is using
		// that class as user defined IdClass
		Partyguest guest1 = Partyguest.builder()
			.firstname("Mick")
			.surname("Tan")
			.build();
		em.persist(guest1);
		Partyguest guest2 = Partyguest.builder()
			.firstname("Michiko")
			.surname("Tan")
			.build();
		em.persist(guest2);
		
		Partyguest result;
		result = em.createQuery("SELECT g FROM Partyguest g WHERE g.firstname='Mick'", Partyguest.class)
			.getSingleResult();
		
		result = em.find(Partyguest.class, PartyguestId.of("Mick", "Tan"));
		assertNotNull(result);
		
	}
	
}
