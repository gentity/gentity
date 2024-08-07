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

import com.github.gentity.test.test0h_base_table_standard_types.NumberSample;
import java.math.BigDecimal;
import java.math.BigInteger;
import jakarta.persistence.Column;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

/**
 *
 * @author upachler
 */
public class Test0h_base_table_standard_types extends AbstractGentityTest{
	@Test
	public void test() throws NoSuchFieldException {
		// if this compiles we're good
		NumberSample ns = new NumberSample.Builder()
			.numBigint(Long.MAX_VALUE)            // BIGINT -> long
			.numDecimal(BigDecimal.ONE)           // DECIMAL withtout precision/scale -> BigDecimal
			.numDouble(1.0d)                      // DOUBLE -> double
			.numFloat(1.0f)                       // FLOAT -> float
			.numReal(1.0f)                        // REAL -> float
			.numInt(Integer.MAX_VALUE)            // INT -> int
            
            // all NUMERIC/DECIMAL columns, regardless of what scale is configured, are mapped to BigDecimal
			.numNumericDefault(BigDecimal.ONE)  
			.numNumericFloat83(BigDecimal.ONE) 
			.numNumericFloat143(BigDecimal.ONE)
			.numNumericFloat283(BigDecimal.ONE)
			.numNumericInt80(BigDecimal.ONE)
			.numNumericInt12(BigDecimal.ONE)
			.numNumericInt16(BigDecimal.ONE)
			.numNumericInt32(BigDecimal.ONE) 
			.build();
		
		// no scale or precision set for numBigint, numDecimal, numDouble, numReal, numInt and numNumericDefault fields
		assertEquals(0, columnOf(NumberSample.class, "numBigint").scale());
		assertEquals(0, columnOf(NumberSample.class, "numBigint").precision());
		assertEquals(0, columnOf(NumberSample.class, "numDecimal").scale());
		assertEquals(0, columnOf(NumberSample.class, "numDecimal").precision());
		assertEquals(0, columnOf(NumberSample.class, "numDouble").scale());
		assertEquals(0, columnOf(NumberSample.class, "numDouble").precision());
		assertEquals(0, columnOf(NumberSample.class, "numReal").scale());
		assertEquals(0, columnOf(NumberSample.class, "numReal").precision());
		assertEquals(0, columnOf(NumberSample.class, "numInt").scale());
		assertEquals(0, columnOf(NumberSample.class, "numInt").precision());
		assertEquals(0, columnOf(NumberSample.class, "numNumericDefault").scale());
		assertEquals(0, columnOf(NumberSample.class, "numNumericDefault").precision());
		
		// check whether scale and precision are set according to the specification in the model
		assertEquals(8, columnOf(NumberSample.class, "numNumericFloat83").precision());
		assertEquals(3, columnOf(NumberSample.class, "numNumericFloat83").scale());
		
		assertEquals(14, columnOf(NumberSample.class, "numNumericFloat143").precision());
		assertEquals(3, columnOf(NumberSample.class, "numNumericFloat143").scale());
		
		assertEquals(28, columnOf(NumberSample.class, "numNumericFloat283").precision());
		assertEquals(3, columnOf(NumberSample.class, "numNumericFloat283").scale());
		
		assertEquals(8, columnOf(NumberSample.class, "numNumericInt80").precision());
		assertEquals(0, columnOf(NumberSample.class, "numNumericInt80").scale());
		
		assertEquals(12, columnOf(NumberSample.class, "numNumericInt12").precision());
		assertEquals( 0, columnOf(NumberSample.class, "numNumericInt12").scale());
		
		assertEquals(16, columnOf(NumberSample.class, "numNumericInt16").precision());
		assertEquals( 0, columnOf(NumberSample.class, "numNumericInt16").scale());
		
		assertEquals(32, columnOf(NumberSample.class, "numNumericInt32").precision());
		assertEquals(0, columnOf(NumberSample.class, "numNumericInt32").scale());
		
		em.persist(ns);
	}
	
	private Column columnOf(Class c, String fieldName) throws NoSuchFieldException {
		return c.getDeclaredField(fieldName).getAnnotation(Column.class);
	}
}
