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
		
		// NOTE: hibernate is quite picky about the order entities are persisted
		// in our case here: In a relationship, the owning side entity (Employee)
		// must be persisted AFTER the inverse side (Company).
		// EclipseLink doesn't seem to care about that
		em.persist(c);
		em.persist(e);


		// NOTE: The original test code is left here to give a little bit of 
		// history: 
		// Initially, a problem was discovered when executing the following steps:
		//
		//		// (0) make sure objects are in the database
		//		em.flush();
		//
		//		// (1) I remove (DELETE) the employee
		//		em.remove(e);
		//		
		//		// (2) I change the association on the removed entity
		//		e.setCompany(null);
		//		
		//		// (3) The call to flush fails, complaining about a failed 
		//		// NOT NULL contstraint
		//		em.flush();
		//
		// The problem was solved, but the solution involves to execute the steps
		// a little bit. Generally it seems a bad idea to change a removed 
		// entity (the Employee we removed). 
		// In our particular case, we can't change the Employee beforehand either
		// (set the company to null or remove him from the company's employee)
		// list, because when written to the database, that would mean breaking
		// the NOT NULL constraint on the Employee (even removing the Employee
		// from the Company's employee list will set the company field on 
		// Employee to NULL because of bidirectional update.
		// 
		// To remedy this, the solution is to check if an entity is removed
		// (aka deleted), and if it is, do not perform a triggered bidirectional
		// update on it. 
		// The general rule is to avoid modifying an entity after it was removed,
		// which is generally advisable anyways to keep JPA applications portable.
		// 
		// The solution is now to remove (=delete) the entity first, and then 
		// remove it from the other entities that might still have references to
		// it. The new removed check prevents cascaded changes on the removed
		// entity.
		
		// (0) make sure objects are in the database
		em.flush();

		// (1) I remove (DELETE) the employee
		em.remove(e);
		
		// (2) make sure that the removed entity is no longer referenced from
		// the other side
		c.getEmployee().remove(e);
		
		// (3) The call to flush fails, complaining about a failed 
		// NOT NULL contstraint
		em.flush();

	}
}
