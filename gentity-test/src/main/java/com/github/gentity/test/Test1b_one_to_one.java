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

import com.github.gentity.test.test1b_one_to_one.Companycar;
import com.github.gentity.test.test1b_one_to_one.Desk;
import com.github.gentity.test.test1b_one_to_one.Employee;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author upachler
 */
public class Test1b_one_to_one extends AbstractGentityTest{
	
	@Test
	public void test() {
		
		Companycar cc = Companycar.builder()
			.registration("S-1234")
			.build();
		
		Desk d1 = Desk.builder()
			.invno("A-123-567")
			.model("NiceDesk")
			.build();
		Desk d2 = Desk.builder()
			.invno("B-678-900")
			.model("UglyDesk")
			.build();
		
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
