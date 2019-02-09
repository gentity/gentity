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

import com.github.gentity.test.test1a_many_to_one.Company;
import com.github.gentity.test.test1a_many_to_one.Employee;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author upachler
 */
public class Test1a_many_to_one extends AbstractGentityTest{
	
	@Test
	public void test() {
		
		Employee e11 = Employee.builder()
			.firstname("John")
			.surname("Doe")
			.build();
		Employee e12 = Employee.builder()
			.firstname("Mick")
			.surname("Miller")
			.build();
		
		Company c1 = Company.builder()
			.name("Acme")
			.build();
		e11.setCompany(c1);
		e12.setCompany(c1);
		em.persist(e11);
		em.persist(e12);
		em.persist(c1);
		
		Company c = em.createQuery("SELECT DISTINCT c FROM Company c JOIN c.employee e WHERE e.firstname = 'John'", Company.class)
			.getSingleResult();
		
		boolean mickWorksHere = c.getEmployee().stream()
			.anyMatch(e -> e.getFirstname().equals("Mick"));
		
		Assert.assertTrue(mickWorksHere);
	}
}
