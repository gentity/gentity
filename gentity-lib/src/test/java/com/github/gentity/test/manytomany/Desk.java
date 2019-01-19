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

import com.github.gentity.test.onetomany.*;
import com.github.gentity.test.NamedObject;
import com.github.gentity.ToManySide;
import java.util.ArrayList;
import java.util.List;

/**
 * Owning side of a many-to-many relation. The owning side initializes the
 * relations for both sides.
 * @author count
 */
public class Desk extends NamedObject {
	
	private List<Employee> employees = new ArrayList();
	static final ToManySide<Desk,Employee> employees$relation = new ToManySide<>(o -> o.employees);

	static
	{
		employees$relation.setOther(Employee.desks$relation);
		Employee.desks$relation.setOther(employees$relation);
	}

	public Desk(String name) {
		super(name);
	}

	public List<Employee> getEmployees() {
		return employees$relation.wrap(employees, this);
	}
}
