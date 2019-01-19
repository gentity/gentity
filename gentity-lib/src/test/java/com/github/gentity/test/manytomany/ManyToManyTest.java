/*
 * Copyright 2019 The Gentity Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.gentity.test.manytomany;

import java.util.Collection;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author count
 */
public class ManyToManyTest {
	@Test
	public void test() {
		// four employees share three desks
		
		Desk desk1 = new Desk("desk1");
		Desk desk2 = new Desk("desk2");
		Desk desk3 = new Desk("desk3");

		Employee john = new Employee("John");
		Employee mary = new Employee("Mary");
		Employee michael = new Employee("Michael");
		Employee livia = new Employee("Livia");

		desk1.getEmployees().add(john);
		
		desk2.getEmployees().add(mary);
		desk2.getEmployees().add(michael);
		
		michael.getDesks().add(desk3);
		livia.getDesks().add(desk3);
		
		
		assertTrue(containsSameAs(desk1.getEmployees(), john));
		assertTrue(containsSameAs(desk2.getEmployees(), mary, michael));
		assertTrue(containsSameAs(desk3.getEmployees(), livia, michael));
		
		assertTrue(containsSameAs(john.getDesks(), desk1));
		assertTrue(containsSameAs(michael.getDesks(), desk2, desk3));
		assertTrue(containsSameAs(livia.getDesks(), desk3));
		
		
		// now michael gives up desk 2
		michael.getDesks().remove(desk2);
		livia.getDesks().add(desk1);
		
		assertTrue(containsSameAs(desk1.getEmployees(), john, livia));
		assertTrue(containsSameAs(desk2.getEmployees(), mary));
		assertTrue(containsSameAs(desk3.getEmployees(), livia, michael));
		
		assertTrue(containsSameAs(john.getDesks(), desk1));
		assertTrue(containsSameAs(michael.getDesks(), desk3));
		assertTrue(containsSameAs(livia.getDesks(), desk1, desk3));
	}
	
	private <T> boolean containsSameAs(Collection<T> c, T... objects) {
		if(c.size() != objects.length) {
			return false;
		}
		for(T o : objects) {
			if(!c.contains(o)) {
				return false;
			}
		}
		return true;
	}
}
