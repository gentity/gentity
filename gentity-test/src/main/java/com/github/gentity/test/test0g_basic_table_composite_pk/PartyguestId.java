/*
 * Copyright 2019 The Gentity Project. All rights reserved.
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
package com.github.gentity.test.test0g_basic_table_composite_pk;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author count
 */
public class PartyguestId implements Serializable{
	private String firstname;
	private String surname;
	
	// not sure if the JPA spec needs a default constructor
	// for idclasses, but hibernate seems to need that.
	// a package private constructor seems to suffice
	PartyguestId(){
	}
	
	public PartyguestId(String firstname, String surname) {
		this.firstname = firstname;
		this.surname = surname;
	}
	
	
	public static PartyguestId of(String firstname, String surname) {
		return new PartyguestId(firstname, surname);
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 23 * hash + Objects.hashCode(this.firstname);
		hash = 23 * hash + Objects.hashCode(this.surname);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final PartyguestId other = (PartyguestId) obj;
		if (!Objects.equals(this.firstname, other.firstname)) {
			return false;
		}
		if (!Objects.equals(this.surname, other.surname)) {
			return false;
		}
		return true;
	}
	
	
}
