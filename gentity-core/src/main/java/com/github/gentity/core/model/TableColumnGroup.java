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
package com.github.gentity.core.model;

import com.github.gentity.core.model.util.ArrayListTableColumnGroup;
import java.util.Collection;

/**
 *
 * @author count
 */
public interface TableColumnGroup<T extends ColumnModel> extends Collection<T>{
	public default T findColumn(String name) {
		return stream()
			.filter(c -> name.equals(c.getName()))
			.findAny()
			.orElse(null);
	}
	
	public static <T extends ColumnModel> TableColumnGroup<T> of(Collection<T> c) {
		return new ArrayListTableColumnGroup<>(c);
	}
}
