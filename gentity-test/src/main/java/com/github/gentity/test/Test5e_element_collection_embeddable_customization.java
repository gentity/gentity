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

import com.github.gentity.test.test5e_element_collection_embeddable_customization.*;
import java.util.Arrays;
import jakarta.persistence.Embeddable;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author upachler
 */
public class Test5e_element_collection_embeddable_customization extends AbstractGentityTest{
	
	
	@Test
	public void test() {
		// in this test we're checking if collection tables can be properly
		// customized in the gentity file - on the field level as well as
		// on the embeddable class level. In this example, PizzaOrderItem is
		// generated as an embeddable class that we're customizing. To check we:
		// * customize the generated embeddable class to implement the interface
		//   OrderFragment
		// * customize the field 'size' to carry an enum rather than a String
		
		// make sure that OrderItem is an embeddable with a reference to Product
		assertTrue(hasClassAnnotation(PizzaOrderItem.class, Embeddable.class));
		assertTrue(hasClassDeclaredField(PizzaOrderItem.class, "product"));
		assertTrue(OrderFragment.class.isAssignableFrom(PizzaOrderItem.class));
		
		String PIZZA_MARGERITA = "Pizza Margerita";
		Product pizzaMargerita = Product.builder()
			.name(PIZZA_MARGERITA)
			.buildWithId("123-001");
		
		PizzaOrder order = PizzaOrder.builder()
			.customerName("Max")
			.pizzaOrderItem(Arrays.asList(
				PizzaOrderItem.builder()
				.amount(1)
				// NOTE: The size field is customized as enum
				.size(PizzaSize.LARGE)
				.product(pizzaMargerita)
				.build()
			))
			.build();
		
		em.persist(order);
		em.persist(pizzaMargerita);
		
		// we flush and detach, so that the ORM will provide us with freshly
		// read objects from the database
		em.flush();
		
		em.detach(pizzaMargerita);
		em.detach(order);
		
		order = em.createQuery("SELECT o FROM PizzaOrder o", PizzaOrder.class).getSingleResult();
		
		assertEquals(1, order.getPizzaOrderItem().size());
		assertTrue(order.getPizzaOrderItem().stream()
			.anyMatch(item -> item.getProduct().getName().equals(PIZZA_MARGERITA))
		);
		
	}
}
