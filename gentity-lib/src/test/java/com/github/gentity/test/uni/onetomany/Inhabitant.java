/*
 * Copyright 2019 The Gentity Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.gentity.test.uni.onetomany;

import com.github.gentity.ToOneSide;
import com.github.gentity.test.NamedObject;
import jakarta.persistence.PreRemove;

/**
 * Multiple inhabitants may live in a House. An Inhabitant knows which house he/she
 * is living in, but not vice versa (after all, the house is a house, and therefore
 * not very smart :). This renders the relationship unidirectional.
 * An Inhabitant is the Many side of the relationship to the House.
 * @author count
 */
public class Inhabitant extends NamedObject{
	
	House house;
	static final ToOneSide<Inhabitant,House> relationTo$house = ToOneSide.of(o -> o.$removed, o -> o.house, (o,m) -> o.house = m);

	private transient boolean $removed;
	@PreRemove
	private void $onPrepersist() {
		$removed = true;
	}

	public Inhabitant(String name) {
		super(name);
	}

	public House getHouse() {
		return relationTo$house.get(this);
	}
	
	public void setHouse(House house) {
		relationTo$house.set(this, house);
	}
	
}
