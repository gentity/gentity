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
package com.github.gentity.core;

/**
 *
 * @author Uwe pachler
 */
public interface ShellLogger {
	public default void info(String format, Object... params) {
		info(null, format, params);
	}
	public void info(Throwable t, String format, Object... params);
	
	public default void warn(String format, Object... params) {
		warn(null, format, params);
	}
	public void warn(Throwable t, String format, Object... params);
	
	public default void error(String format, Object... params) {
		error(null, format, params);
	}
	public void error(Throwable t, String format, Object... params);
	
	
	public default void debug(String format, Object... params) {
		error(null, format, params);
	}
	public void debug(Throwable t, String format, Object... params);
	
}
