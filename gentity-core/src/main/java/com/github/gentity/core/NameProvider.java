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
package com.github.gentity.core;

import java.util.Locale;
import java.util.function.Predicate;

/**
 *
 * @author upachler
 */
public class NameProvider {

	private static final int MAX_CANDIDATES = 100;

	String findNonexistingName(String prefix, String suffix, Predicate<String> existenceTest) {
		String candidate = "";
		for (int n = 0; n < MAX_CANDIDATES; ++n) {
			String candidateNumber = n == 0 ? "" : Integer.toString(n);
			candidate = prefix + candidateNumber + suffix;
			if (!existenceTest.test(candidate)) {
				return candidate;
			}
		}
		throw new RuntimeException("too many attempts to form a name for table, last unsuccessful candidate was '" + candidate + "'");
	}

	private static boolean isNum(char c) {
		return  c >= '0' && c <= '9';
	}
	
	private static boolean isAlpha(char c) {
		return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z';
	}
	
	private static boolean isAllUppercase(String s) {
		for(int i=0; i<s.length(); ++i) {
			char c = s.charAt(i);
			if(isAlpha(c) && !Character.isUpperCase(c)) {
				return false;
			}
		}
		return true;
	}
	
	public String javatizeName(String name, boolean startUppercase) {
		
		// all-uppercase names like 'BANKACCOUNT' will be lowercased first 
		// (resulting in 'bankaccount'. Input strings like 'BankAccount' will
		// remain untouched
		if(isAllUppercase(name)) {
			name = name.toLowerCase(Locale.US);
		}
		
		boolean needsUppercasing = startUppercase;
		StringBuilder sb = new StringBuilder();
		for (char c : name.toCharArray()) {
			if(isNum(c)) {
				sb.append(c);
				needsUppercasing = true;
			} else if(isAlpha(c)) {
				if (needsUppercasing && isAlpha(c)) {
					c = Character.toUpperCase(c);
					needsUppercasing = false;
				}
				sb.append(c);
			} else {
				needsUppercasing = true;
			}
		}
		
		// prepend name with underscore '_' for non-alpha start characters
		if(sb.length()>0 && !isAlpha(sb.charAt(0))) {
			sb.insert(0 , '_');
		}
		
		return sb.toString();
	}
	
}
