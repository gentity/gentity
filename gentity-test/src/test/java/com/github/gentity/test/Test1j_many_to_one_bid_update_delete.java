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

import org.junit.Test;

import com.github.gentity.test.test1j_many_to_one_bid_update_delete.*;
import static org.junit.Assert.assertNull;
	
/**
 * Test behaviour in automatic bidirectional update scenario
 * 
 * @author count
 */
public class Test1j_many_to_one_bid_update_delete extends AbstractGentityTest{
	@Test
	public void testUpdateDelete() {
		
		// associating a Company and an Employee (one-to-many)
		Company c = Company.builder()
			.name("ACME")
			.build();
		
		em.persist(c);
		em.flush();
		
		Employee e = Employee.builder()
			.firstname("John")
			.surname("Doe")
			.build();
		e.setCompany(c);
		
		em.persist(e);
		
		em.flush();
		
		// now it's all done, here is the simple thing:
		
		// (1) remove the Employee
		em.remove(e);
		
		// (2) fix up the company by removing the employee from the list
		// (yep, we still need to to that, even with auto bidi).
		// Note that with bidirectional update, this operation normally triggers
		// a binding update on both ends of the relation (Company and Employee)
		// - but see step (3)
		c.getEmployee().remove(e);
		
		// (3) flush time! 
		// With this issue:
		// https://github.com/gentity/gentity/issues/3 , flush failed here because
		// even though we removed the entity in (1), initially the Employee 
		// entity was still updated by us in step (2) (bidirectional update).
		// Because removing an entity from a collection calls unbind() on the 
		// other side (the Employee), which in turn causes that Employee to 
		// set its company field to null. 
		// And that null field is what EclipseLink tried to UPDATE to the 
		// database before it attempted DELETE.
		// The fix silently ignores bind/unbind operations on entities that are
		// removed (which requires tracking entity state via generated lifecycle callbacks)
		em.flush();
		
		assertNull(em.find(Employee.class, e.getId()));
	}
}
