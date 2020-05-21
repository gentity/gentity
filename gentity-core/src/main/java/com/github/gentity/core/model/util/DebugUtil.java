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

import com.github.gentity.core.model.ColumnModel;
import com.github.gentity.core.model.TableModel;

/**
 *
 * @author upachler
 */
public class DebugUtil {
	public static class StringIndenter {
		private StringBuilder sb = new StringBuilder();
		private String indentBlock = "\t";
		
		public void push() {
			sb.append(indentBlock);
		}
		public void pop() {
			sb.subSequence(0, sb.length()-indentBlock.length());
		}
		public CharSequence getIndent() {
			return sb;
		}
	}
	
	public static String toDescriptiveSQLString(TableModel t) {
		return toDescriptiveSQLString(t, new StringIndenter());
	}
	
	public static String toDescriptiveSQLString(TableModel t, StringIndenter indenter) {
		StringBuilder sb = new StringBuilder(indenter.getIndent())
			.append("TABLE (")
			.append(t.getName())
			.append('\n');
		
		indenter.push();
		
		for(ColumnModel c : t.getColumns()) {
			sb.append('\t')
			.append(c.getName())
			.append(' ')
			.append(c.getType().name())
			.append('\n');
		}
		
		indenter.pop();
		
		sb.append(");");
		
		return sb.toString();
	}
	
}
