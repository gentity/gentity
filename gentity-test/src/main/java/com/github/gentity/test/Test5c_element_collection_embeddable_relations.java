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

import com.github.gentity.test.test5c_element_collection_embeddable_relations.*;
import java.util.Arrays;
import javax.persistence.Embeddable;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author upachler
 */
public class Test5c_element_collection_embeddable_relations extends AbstractGentityTest{
	
	
	@Test
	public void test() {
		
		// make sure that OrderItem is an embeddable with a reference to Product
		assertTrue(hasClassAnnotation(PizzaOrderItem.class, Embeddable.class));
		assertTrue(hasClassDeclaredField(PizzaOrderItem.class, "product"));
		
		String PIZZA_MARGERITA = "Pizza Margerita";
		String PIZZA_QUATTRO = "Pizza Quattro";
		Product pizzaMargerita = Product.builder()
			.id("123-001")
			.name(PIZZA_MARGERITA)
			.build();
		Product pizzaQuattro = Product.builder()
			.id("123-002")
			.name(PIZZA_QUATTRO)
			.build();
		
		PizzaOrder order = PizzaOrder.builder()
			.customerName("Max")
			.pizzaOrderItem(Arrays.asList(
				PizzaOrderItem.builder()
				.amount(2)
				.product(pizzaQuattro)
				.build(),
				PizzaOrderItem.builder()
				.amount(1)
				.product(pizzaMargerita)
				.build()
			))
			.build();
		
		em.persist(order);
		em.persist(pizzaMargerita);
		em.persist(pizzaQuattro);
		
		// we flush and detach, so that the ORM will provide us with freshly
		// read objects from the database
		em.flush();
		
		em.detach(pizzaMargerita);
		em.detach(pizzaQuattro);
		em.detach(order);
		
		order = em.createQuery("SELECT o FROM PizzaOrder o", PizzaOrder.class).getSingleResult();
		
		assertEquals(2, order.getPizzaOrderItem().size());
		assertTrue(order.getPizzaOrderItem().stream()
			.anyMatch(item -> item.getProduct().getName().equals(PIZZA_MARGERITA))
		);
		assertTrue(order.getPizzaOrderItem().stream()
			.anyMatch(item -> item.getProduct().getName().equals(PIZZA_QUATTRO))
		);
		
	}
}
