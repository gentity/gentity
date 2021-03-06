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
package com.github.gentity.core;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author upachler
 */
public class NameProviderTest {
	
	public NameProviderTest() {
	}

	@Test
	public void testJavatizeName() {
		NameProvider np = new NameProvider();
		assertEquals("foo", np.javatizeName("foo", false));
		assertEquals("Foo", np.javatizeName("foo", true));
		
		// test first character uppercasing
		assertEquals("FooBar", np.javatizeName("Foo_bar", false));
		assertEquals("FooBar", np.javatizeName("Foo_bar", true));
		
		// test first character uppercasing
		assertEquals("fooBar", np.javatizeName("foo_bar", false));
		assertEquals("FooBar", np.javatizeName("foo_bar", true));
		
		// test lowercasing all-uppercase names
		assertEquals("foobar", np.javatizeName("FOOBAR", false));
		assertEquals("fooBar", np.javatizeName("fooBar", false));
		
		// test lowercasing all-uppercase names
		assertEquals("foo1Bar", np.javatizeName("foo1_bar", false));
		assertEquals("foo1Bar", np.javatizeName("foo1bar", false));
		
		assertEquals("_1FooBar", np.javatizeName("1foo_bar", false));
		
	}
	
}
