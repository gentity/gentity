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
package com.github.gentity.core.model.dbs;

import com.github.gentity.core.model.dbs.dto.SequenceDto;
import com.github.gentity.core.model.SequenceModel;

/**
 *
 * @author upachler
 */
public class DbsSequenceModel implements SequenceModel{
	final SequenceDto dto;

	public DbsSequenceModel(SequenceDto dto) {
		this.dto = dto;
	}
	
	
	@Override
	public String getName() {
		return dto.getName();
	}

	@Override
	public Long getStartValue() {
		return dto.getStart();
	}
	
}
