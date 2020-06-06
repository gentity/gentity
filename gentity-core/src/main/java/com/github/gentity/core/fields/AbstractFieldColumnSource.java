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
package com.github.gentity.core.fields;

import com.github.gentity.core.config.dto.TableFieldDto;
import com.github.gentity.core.NameProvider;
import com.github.gentity.core.model.ColumnModel;
import com.github.gentity.core.model.TableModel;

/**
 *
 * @author count
 */
abstract class AbstractFieldColumnSource implements FieldColumnSource{
	static NameProvider np = new NameProvider();
	
	protected DefaultColumnFieldMapping toDefaultColumnFieldMapping(TableModel t, ColumnModel col, TableFieldDto field) {
		String nameCandidate;
		if(field != null && field.getName() != null) {
			nameCandidate = field.getName();
		} else {
			nameCandidate = np.javatizeName(col.getName(), false);
		}
		String enumType = field != null? field.getEnumType() : null;
				
		return new DefaultColumnFieldMapping(t, col, nameCandidate, enumType);
	}
}
