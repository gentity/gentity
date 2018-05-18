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

import com.github.gentity.test.test0c_basic_table_autoincrement.Person;
import com.github.gentity.test.test0d_basic_table_blob.Document;
import java.util.Base64;
import java.util.logging.Logger;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author upachler
 */
public class Test0d_basic_table_blob extends AbstractGentityTest {

	Logger logger = Logger.getLogger(getClass().getName());
	
	byte[] byteArrayOf(int... ints) {
		byte[] bytes = new byte[ints.length];
		for(int n=0; n<ints.length; ++n) {
			int i = ints[n];
			if(i > 0xff|| i<0) {
				throw new IllegalArgumentException("value out of range for conversion to byte: " + i);
			}
			bytes[n] = (byte)i;
		}
		return bytes;
	}
	
	@Test
	public void test() {
		byte[] byteArrayContent = byteArrayOf(0xaf, 0xfe, 0xde, 0xad, 0xbe, 0xef);
		em.persist(Document.builder()
			.name("deadbeef")
			.content(byteArrayContent)
			.build()
		);
		em.flush();
		
		
		Document d = em.createQuery("SELECT d FROM Document d WHERE d.name='deadbeef'", Document.class)
			.getSingleResult();
		
		Assert.assertArrayEquals(byteArrayContent, d.getContent());
			
	}
}
