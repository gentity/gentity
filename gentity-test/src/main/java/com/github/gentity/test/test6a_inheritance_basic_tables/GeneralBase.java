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
package com.github.gentity.test.test6a_inheritance_basic_tables;

import static com.github.gentity.test.test6a_inheritance_basic_tables.GeneralBase.*;
import javax.persistence.MappedSuperclass;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * Mapped superclass for entities, configured as default superclass for entities
 * generated in test 6a. For demonstration purposes, this superclass also has
 * a {@link NamedQuery} defined.
 * @author count
 */
@MappedSuperclass
@NamedQueries({
	@NamedQuery(
		name = FIND_PERSON_WITH_NAME,
		query = "SELECT p FROM Person p WHERE p.name=:name"
	)
})
public class GeneralBase {
	public static final String FIND_PERSON_WITH_NAME = "FIND_PERSON_WITH_NAME";
}
