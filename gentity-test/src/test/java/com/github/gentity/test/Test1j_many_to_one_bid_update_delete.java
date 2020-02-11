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
 *
 * @author count
 */
public class Test1j_many_to_one_bid_update_delete extends AbstractGentityTest{
	@Test
	public void testUpdateDelete() {
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
		
		em.remove(e);
		c.getEmployee().remove(e);

		em.flush();
		
		assertNull(em.find(Employee.class, e.getId()));
	}
}
