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

import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Uwe pachler
 */
public class JULShellLogger extends AbstractShellLogger {
	private final Logger logger;

	public JULShellLogger(Logger logger) {
		this.logger = logger;
	}
	
	@Override
	public void info(Throwable t, String format, Object... params) {
		doLog(Level.INFO, t, format, params);
	}

	@Override
	public void warn(Throwable t, String format, Object... params) {
		doLog(Level.WARNING, t, format, params);
	}

	@Override
	public void error(Throwable t, String format, Object... params) {
		doLog(Level.SEVERE, t, format, params);
	}

	@Override
	public void debug(Throwable t, String format, Object... params) {
		doLog(Level.FINE, t, format, params);
	}
	
	private void doLog(Level l, Throwable throwable, String format, Object... params) {
		log(logger.isLoggable(l), throwable, (t,s)->logger.log(l, s, t), format, params);
	}
}
