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

import com.github.gentity.test.test1d_many_to_one_unidirectional.Child;
import com.github.gentity.test.test1d_many_to_one_unidirectional.Parent;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author upachler
 */
public class Test1d_many_to_one_unidirectional extends AbstractGentityTest{
	
	@Test
	public void test() {
		Parent p1 = Parent.builder()
			.name("parent_1")
			.buildWithId(1);
		Parent p2 = Parent.builder()
			.name("parent_2")
			.buildWithId(2);
		
		Child c1 = Child.builder()
			.name("child_1")
			.parent(p1)
			.buildWithId(1);
		Child c2 = Child.builder()
			.name("child_2")
			.parent(p1)
			.buildWithId(2);
		Child c3 = Child.builder()
			.name("child_3")
			.parent(p1)
			.buildWithId(3);
		
		em.persist(p1);
		em.persist(p2);
		em.persist(c1);
		em.persist(c2);
		em.persist(c3);
		
		List<Child> result = em.createQuery("SELECT c FROM Child c JOIN c.parent p WHERE p.name='parent_1'", Child.class)
			.getResultList();
		
		Assert.assertEquals(3, result.size());
		Assert.assertFalse(hasClassDeclaredField(Parent.class, "child"));
		
	}
}
