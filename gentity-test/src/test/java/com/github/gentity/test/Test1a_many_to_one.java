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
		// I create a company with one employee and associate them...
		Employee e = Employee.builder()
			.firstname("John")
			.surname("Doe")
			.build();
		
		Company c = Company.builder()
			.name("Acme")
			.build();
		e.setCompany(c);
		
		em.persist(e);
		em.persist(c);
		
		// (0) make sure objects are in the database
		em.flush();

		// (1) I remove (DELETE) the employee
		em.remove(e);
		
		// (2) I change the association on the removed entity
		e.setCompany(null);
		
		// (3) The call to flush fails, complaining about a failed 
		// NOT NULL contstraint
		em.flush();
		
	}
}
