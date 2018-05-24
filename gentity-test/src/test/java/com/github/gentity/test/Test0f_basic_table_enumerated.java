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
import com.github.gentity.test.test0e_basic_table_enumerated.MovieGenre;
import com.github.gentity.test.test0e_basic_table_enumerated.MovieRating;
import com.github.gentity.test.test0f_basic_table_enumerated.Movie;
import java.util.List;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 *
 * @author count
 */
public class Test0f_basic_table_enumerated extends AbstractGentityTest{
	
	@Test
	public void test() {
		em.persist(Movie.builder()
			.name("Star Wars")
			.genre(MovieGenre.SCIFI)
			.rating(MovieRating.PG)
			.build()
		);
		
		em.persist(Movie.builder()
			.name("Third Man")
			.genre(MovieGenre.CRIME)
			.rating(MovieRating.ADULT)
			.build()
		);
		List<Movie> p = em.createQuery("SELECT m FROM Movie m WHERE m.genre=:genre")
			.setParameter("genre", MovieGenre.CRIME)
			.getResultList();
		
		assertEquals(1, p.size());
		assertEquals("Third Man", p.get(0).getName());
	}
}
