/*
 * Copyright 2020 The Gentity Project. All rights reserved.
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

import com.github.gentity.test.test1k_many_to_one_composite_pk_derived_identity.*;
import javax.persistence.Id;
import org.junit.Test;
import static org.junit.Assert.*;
/**
 *
 * @author Uwe pachler
 */
public class Test1k_many_to_one_composite_pk_derived_identity extends AbstractGentityTest{
	
	private boolean fieldHasAnnotation(Class<?> clazz, String fieldName, Class<?> annotationClass) {
		return false;
	}
	
	@Test
	public void test() throws NoSuchFieldException {
		// 'order' and 'sku' fields have @Id annotatins ...
		assertNotNull(OrderItem.class.getDeclaredField("pos").getAnnotation(Id.class));
		assertNotNull(OrderItem.class.getDeclaredField("order").getAnnotation(Id.class));
		
		// 'order' in the Id class is of the same type as the OrderHeader.id field
		assertEquals(Integer.class, OrderItem.Id.class.getDeclaredField("order").getType());
		// 'pos' is the OrderItem's own primary key component
		assertEquals(Integer.class, OrderItem.Id.class.getDeclaredField("pos").getType());
		
		
		// 'orderItem' and 'extraCode' fields have @Id annotatins ...
		assertNotNull(OrderItemExtra.class.getDeclaredField("orderItem").getAnnotation(Id.class));
		assertNotNull(OrderItemExtra.class.getDeclaredField("extraCode").getAnnotation(Id.class));
		
		// 'orderItem' in the Id class is of the same type as the OrderHeader.id field
		assertEquals(OrderItem.Id.class, OrderItemExtra.Id.class.getDeclaredField("orderItem").getType());
		// 'extraCode' is the OrderItemExtra's own primary key component
		assertEquals(Integer.class, OrderItemExtra.Id.class.getDeclaredField("extraCode").getType());
		
		
		Orderhead o = Orderhead.builder()
			.description("foo")
			.buildWithId(22);
		em.persist(o);
		
		OrderItem i = OrderItem.builder()
			.sku(4711L)
			.buildWithId(1, o);
		em.persist(i);
		
		OrderItemExtra e = OrderItemExtra.builder()
			.description("some more")
			.buildWithId(22, i);
		em.persist(e);
			
	}
}
