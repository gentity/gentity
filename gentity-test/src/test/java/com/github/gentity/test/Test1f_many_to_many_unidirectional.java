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

import com.github.gentity.test.test1f_many_to_many_unidirectional.Ghostwriter;
import com.github.gentity.test.test1f_many_to_many_unidirectional.Book;
import java.util.Arrays;
import java.util.HashSet;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author upachler
 */
public class Test1f_many_to_many_unidirectional extends AbstractGentityTest{
	
	@Test
	public void test() {
		assertTrue(hasClassDeclaredField(Ghostwriter.class, "book"));
		assertFalse(hasClassDeclaredField(Book.class, "ghostwriter"));
		
		Book book1 = Book.builder()
			.id(1L)
			.title("book1")
			.build();
		Book book2 = Book.builder()
			.id(2L)
			.title("book2")
			.build();
		
		Ghostwriter author1 = Ghostwriter.builder()
			.id(1L)
			.name("Maier")
			.book(Arrays.asList(book1))
			.build();
		Ghostwriter author2 = Ghostwriter.builder()
			.id(2L)
			.name("Müller")
			.book(Arrays.asList(book1, book2))
			.build();
		Ghostwriter author3 = Ghostwriter.builder()
			.id(3L)
			.name("Schulz")
			.book(Arrays.asList(book2))
			.build();
		
		em.persist(book1);
		em.persist(book2);
		
		em.persist(author1);
		em.persist(author2);
		em.persist(author3);
		
		assertEquals(
			new HashSet<>(
				Arrays.asList("Müller", "Schulz")
			),
			new HashSet<>(
				em.createQuery("SELECT g.name FROM Ghostwriter g JOIN g.book b WHERE b.title='book2'", String.class)
				.getResultList()
			)
		);
		
			
	}
}
