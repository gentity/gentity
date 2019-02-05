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
package com.github.gentity.core;

/**
 * Interface describing the seven kinds of relation that the JPA spec knows
 * (see Sec. 2.10f), that is:
 * <ul>
 * <li>unidirectional one-to-many</li>
 * <li>unidirectional many-to-one</li>
 * <li>bidirectional one-to-many (which is the same as bidirectional many-to-one)</li>
 * <li>unidirectional many-to-many</li>
 * <li>bidirectional many-to-many</li>
 * <li>unidirectional one-to-one</li>
 * <li>bidirectional one-to-one</li>
 * </ul>
 * 
 * A {@link RelationKind} consists of three properties, following directly from
 * the relation described:
 * <ul>
 * <li><em>directionality</em></li>
 * <li><em>from</em> cardinality</li>
 * <li><em>to</em> cardinality</li>
 * </ul>
 * 
 * The seven kinds are all useful combinations of the seven kinds form above.
 * 
 * The kinds implementing this interface reflect subsets of the seven kinds.
 * @author count
 */
public interface RelationKind {

	Directionality getDirectionality();

	Cardinality getFrom();

	Cardinality getTo();
	
}
