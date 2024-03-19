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
package com.github.gentity.test.onetomany;

import com.github.gentity.test.NamedObject;
import com.github.gentity.ToManySide;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.PreRemove;

/**
 * one side of One-to-many relation. This class represents the owning side,
 * therefore it has a static initializer, setting up relationship peers
 * @author count
 */
public class Room extends NamedObject {
	
	private List<FurniturePiece> furniture = new ArrayList();
	static final ToManySide<Room,List<FurniturePiece>,FurniturePiece> relationTo$furniture = ToManySide.of(o -> o.$removed, o -> o.furniture, FurniturePiece.relationTo$room);

	private transient boolean $removed;
	@PreRemove
	private void $onPrepersist() {
		$removed = true;
	}

	public Room(String name) {
		super(name);
	}

	public List<FurniturePiece> getFurniture() {
		return relationTo$furniture.get(this);
	}
}
