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

import com.github.gentity.test.test1h_many_to_one_composite_pk.Cloakroomitem;
import com.github.gentity.test.test1h_many_to_one_composite_pk.Partyguest;
import org.junit.Test;

/**
 *
 * @author count
 */
public class Test1h_many_to_one_composite_pk extends AbstractGentityTest {
	@Test
	public void test() {
		Partyguest guest1 = Partyguest.builder()
			.primaryinvitee(true)
			.buildWithId("Fred", "Astaire");
		Partyguest guest2 = Partyguest.builder()
			.primaryinvitee(true)
			.buildWithId("Elizabeth", "Windsor");
		Partyguest guest3 = Partyguest.builder()
			.primaryinvitee(false)
			.buildWithId("Philipp", "Windsor");
		em.persist(guest1);
		em.persist(guest2);
		em.persist(guest3);
		
		Cloakroomitem item1 = Cloakroomitem.builder()
			.partyguest(guest2)
			.buildWithId("Crown of England");
		em.persist(item1);
		Cloakroomitem item2 = Cloakroomitem.builder()
			.partyguest(guest2)
			.buildWithId("Fancy Hat");
		em.persist(item2);
		
		Partyguest guest = em.createQuery("SELECT g FROM Partyguest g WHERE :item MEMBER OF g.cloakroomitem", Partyguest.class)
			.setParameter("item", item1)
			.getSingleResult();
	}
}
