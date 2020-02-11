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
package com.github.gentity.test.manytomany;

import com.github.gentity.ToManySide;
import com.github.gentity.test.NamedObject;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.PreRemove;

/**
 *
 * @author count
 */
public class Employee extends NamedObject {
	
	private List<Desk> desks = new ArrayList();
	static final ToManySide<Employee, List<Desk>, Desk> relationTo$desks = ToManySide.of(o -> o.$removed, o -> o.desks, Desk.relationTo$employees);

	private transient boolean $removed;
	@PreRemove
	private void $onPrepersist() {
		$removed = true;
	}

	public Employee(String name) {
		super(name);
	}

	public List<Desk> getDesks() {
		return relationTo$desks.get(this);
	}
}