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
import org.junit.Test;

/**
 *
 * @author upachler
 */
public class Test0h_base_table_standard_types {
	@Test
	public void test() {
		// if this compiles we're good
		NumberSample ns = new NumberSample.Builder()
			.numBigint(Long.MAX_VALUE)            // BIGINT -> long
			.numDecimal(Integer.MAX_VALUE)        // DECIMAL withtout precision/scale -> int
			.numDouble(1.0d)                      // DOUBLE -> double
			.numFloat(1.0f)                       // FLOAT -> float
			.numReal(1.0f)                        // REAL -> float
			.numInt(Integer.MAX_VALUE)            // INT -> int
			.numNumericDefault(Integer.MAX_VALUE) // NUMERIC without precision/scale -> int
			.numNumericFloat83(1.0f)              // NUMERIC(8,3) - eight digits of which three are after the dot -> float
			.numNumericFloat143(1.0d)             // NUMERIC(14,3) - ... -> double
			.numNumericFloat283(BigDecimal.ONE)   // NUMERIC(24,9) - too large for double to hold -> BigDecimal
			.numNumericFloat83(1.0f)              // NUMERIC(8,3) - eight digits of which three are after the dot -> float
			.numNumericInt80(Integer.MAX_VALUE)   // NUMERIC(8,0) - ... -> int
			.numNumericInt12(Integer.MAX_VALUE)   // NUMERIC(12) - twelve digits, zero dots behind the dot (or unspecified) -> int
			.numNumericInt16(Long.MAX_VALUE)      // NUMERIC(16) - ... -> long
			.numNumericInt32(BigInteger.ONE)      // NUMERIC(32) - 32 digits are too big for long -> BigInteger
			.build();
	}
}
