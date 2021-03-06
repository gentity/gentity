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

import com.github.gentity.test.test1i_many_to_many_composite_pk.Partyguest;
import com.github.gentity.test.test1i_many_to_many_composite_pk.Choffeur;
import java.util.Arrays;
import org.junit.Test;

/**
 *
 * @author count
 */
public class Test1i_many_to_many_composite_pk extends AbstractGentityTest {
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
		
		Choffeur choffeur1 = Choffeur.builder()
			.licenseNumber("08/15")
			.buildWithId("Stan");
		em.persist(choffeur1);
		Choffeur choffeur2 = Choffeur.builder()
			.licenseNumber("4711")
			.buildWithId("Ollie");
		em.persist(choffeur2);
		
		choffeur1.getPartyguest().addAll(Arrays.asList(guest1, guest2));
		choffeur2.getPartyguest().addAll(Arrays.asList(guest2, guest3));
		
		Choffeur choffeur = em.createQuery("SELECT c FROM Choffeur c WHERE :guest MEMBER OF c.partyguest", Choffeur.class)
			.setParameter("guest", guest3)
			.getSingleResult();
	}
}
