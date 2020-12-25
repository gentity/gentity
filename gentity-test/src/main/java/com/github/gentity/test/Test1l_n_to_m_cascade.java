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

import com.github.gentity.test.test1l_n_to_m_cascade.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import static javax.persistence.CascadeType.*;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import org.junit.Assert;
import org.junit.Test;
/**
 *
 * @author Uwe pachler
 */
public class Test1l_n_to_m_cascade extends AbstractGentityTest {
	
	private <T extends Enum<T>> Set<T> setOf(T[] enums) {
		if(enums.length == 0) {
			return Collections.EMPTY_SET;
		}
		T first = enums[0];
		T[] rest = Arrays.copyOfRange(enums, 1, enums.length);
		return EnumSet.of(first, rest);
	}
	
	@Test
	public void test() throws NoSuchFieldException {
		OneToMany orderItemOTM = PizzaOrder.class.getDeclaredField("pizzaOrderItem").getAnnotation(OneToMany.class);
		Assert.assertEquals(EnumSet.of(PERSIST, MERGE), setOf(orderItemOTM.cascade()));
		
		ManyToOne orderMTO = PizzaOrderItem.class.getDeclaredField("pizzaOrder").getAnnotation(ManyToOne.class);
		Assert.assertEquals(EnumSet.of(ALL), setOf(orderMTO.cascade()));
		
		ManyToMany recipeMTM = PizzaRecipe.class.getDeclaredField("topping").getAnnotation(ManyToMany.class);
		Assert.assertEquals(EnumSet.of(PERSIST), setOf(recipeMTM.cascade()));
		
		ManyToMany toppingMTO = Topping.class.getDeclaredField("pizzaRecipe").getAnnotation(ManyToMany.class);
		Assert.assertEquals(EnumSet.of(REMOVE), setOf(toppingMTO.cascade()));
	}
}
