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

import com.github.gentity.test.test1e_one_to_one_unidirectional.Companycar;
import com.github.gentity.test.test1e_one_to_one_unidirectional.Desk;
import com.github.gentity.test.test1e_one_to_one_unidirectional.Employee;
import org.junit.Assert;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author upachler
 */
public class Test1e_one_to_one_unidirectional extends AbstractGentityTest{
	
	
	@Test
	public void test() {
		
		assertTrue(hasClassDeclaredField(Employee.class, "companycar"));
		assertTrue(hasClassDeclaredField(Employee.class, "desk"));
		
		assertFalse(hasClassDeclaredField(Desk.class, "employee"));
		assertFalse(hasClassDeclaredField(Companycar.class, "employee"));
		
		Companycar cc = Companycar.builder()
			.registration("S-1234")
			.build();
		
		Desk d1 = Desk.builder()
			.model("NiceDesk")
			.buildWithId("A-123-567");
		Desk d2 = Desk.builder()
			.model("UglyDesk")
			.buildWithId("B-678-900");
		
		Employee e11 = Employee.builder()
			.firstname("John")
			.surname("Doe")
			.desk(d1)
			.build();
		
		Employee e12 = Employee.builder()
			.firstname("Mick")
			.surname("Miller")
			.desk(d2)
			.companycar(cc)
			.build();
		
		em.persist(cc);
		em.persist(d1);
		em.persist(e11);
		em.persist(d2);
		em.persist(e12);
		
		Employee john = em.createQuery("SELECT DISTINCT e FROM Employee e WHERE e.firstname = 'John'", Employee.class)
			.getSingleResult();
		Assert.assertNull(john.getCompanycar());
		Assert.assertEquals("NiceDesk", john.getDesk().getModel());
		
		Employee mick = em.createQuery("SELECT DISTINCT e FROM Employee e WHERE e.firstname = 'Mick'", Employee.class)
			.getSingleResult();
		Assert.assertNotNull(mick.getCompanycar());
		Assert.assertEquals("UglyDesk", mick.getDesk().getModel());
	}
}
