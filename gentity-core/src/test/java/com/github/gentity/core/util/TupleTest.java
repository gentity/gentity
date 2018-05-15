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
package com.github.gentity.core.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author upachler
 */
public class TupleTest {
	
	public TupleTest() {
	}

	@Test
	public void testBasics() {
		Tuple<String,Integer> t1 = Tuple.of("foo", 42);
		
		assertEquals("foo", t1.x());
		assertEquals((Integer)42, t1.y());
		
		assertEquals(Tuple.of("foo", 42), t1);
		
		assertEquals(Tuple.of(42, "foo"), t1.swap());
	}
	
	@Test
	public void testMapReplace() {
		Tuple<String,Integer> t1 = Tuple.of("foo", 42);
		
		Tuple<List<String>,Integer> t2;
		t2 = t1.replaceX(Arrays.asList(t1.x()));
		t2 = t1.mapX(Arrays::asList);
		
		Tuple<Integer, String> t3 = Arrays.asList(t1).stream()
			.map(t -> t.mapY(y -> y+1))
			.map(t -> t.mapY(y -> 2*y))
			.map(t -> t.mapX(x -> x+"bar"))
			.map(Tuple::swap)
			.findFirst()
			.get();
		
		assertEquals(Tuple.of(86, "foobar"), t3);
	}
}
