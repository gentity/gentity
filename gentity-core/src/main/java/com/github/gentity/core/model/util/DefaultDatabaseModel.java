/*
 * Copyright 2020 The Gentity Project. All rights reserved.
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
package com.github.gentity.core.model.util;

import com.github.gentity.core.model.DatabaseModel;
import com.github.gentity.core.model.TableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author count
 */
public class DefaultDatabaseModel<T extends TableModel> implements DatabaseModel{

	private final Map<String,T> tables;

	public DefaultDatabaseModel(Map<String, T> tables) {
		this.tables = tables;
	}
	
	@Override
	public T getTable(String name) {
		return tables.get(name);
	}

	@Override
	public List<T> getTables() {
		return Collections.unmodifiableList(new ArrayList<>(tables.values()));
	}

	
}
