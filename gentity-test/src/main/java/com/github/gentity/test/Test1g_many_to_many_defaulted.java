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

import com.github.gentity.test.test1g_many_to_many_defaulted.*;
import java.util.Arrays;
import java.util.HashSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author upachler
 */
public class Test1g_many_to_many_defaulted extends AbstractGentityTest{
	
	@Test
	public void test() {
		
		/**
		 * Note: this is the same as test1c, but we use defaulting here for the
		 * many-to-many relations' inverses foreign keys.
		 */
		
		// ensure fields for a bidirectional relationship were generated
		assertTrue(hasClassDeclaredField(Book.class, "author"));
		assertTrue(hasClassDeclaredField(Author.class, "book"));
		
		Book book1 = Book.builder()
			.title("book1")
			.buildWithId(1L);
		Book book2 = Book.builder()
			.title("book2")
			.buildWithId(2L);
		
		Author author1 = Author.builder()
			.name("Maier")
			.buildWithId(1L);
		Author author2 = Author.builder()
			.name("Müller")
			.buildWithId(2L);
		Author author3 = Author.builder()
			.name("Schulz")
			.buildWithId(3L);
		
		book1.getAuthor().addAll(Arrays.asList(author1, author2));
		book2.getAuthor().addAll(Arrays.asList(author2, author3));
		
		em.persist(book1);
		em.persist(book2);
		
		em.persist(author1);
		em.persist(author2);
		em.persist(author3);
		
		assertEquals(
			new HashSet<>(
				Arrays.asList("book1", "book2")
			),
			new HashSet<>(
				em.createQuery("SELECT b.title FROM Book b JOIN b.author a WHERE a.name='Müller'", String.class)
				.getResultList()
			)
		);
		
		assertEquals(
			new HashSet<>(
				Arrays.asList("Müller", "Schulz")
			),
			new HashSet<>(
				em.createQuery("SELECT a.name FROM Author a JOIN a.book b WHERE b.title='book2'", String.class)
				.getResultList()
			)
		);
		
			
	}
}
