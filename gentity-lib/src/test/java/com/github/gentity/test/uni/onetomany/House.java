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

import com.github.gentity.test.NamedObject;

/**
 * The house is the one side of the relation to inhabitants. However, it
 * has no association with its inhabitants, rendering the relationship
 * unidirectional.
 * @author count
 */
public class House extends NamedObject{
	
	public House(String name) {
		super(name);
	}
	
}
