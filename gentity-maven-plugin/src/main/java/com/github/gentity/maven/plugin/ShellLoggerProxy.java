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
package com.github.gentity.maven.plugin;

import com.github.gentity.core.AbstractShellLogger;
import org.apache.maven.plugin.logging.Log;

/**
 *
 * @author Uwe pachler
 */
public class ShellLoggerProxy extends AbstractShellLogger{

	private final Log log;

	public ShellLoggerProxy(Log log) {
		this.log = log;
	}
	
	@Override
	public void info(Throwable t, String format, Object... params) {
		log(log.isInfoEnabled(), t, this::receiveInfo, format, params);
	}
	
	@Override
	public void warn(Throwable t, String format, Object... params) {
		log(log.isWarnEnabled(), t, this::receiveWarn, format, params);
	}
	
	@Override
	public void error(Throwable t, String format, Object... params) {
		log(log.isErrorEnabled(), t, this::receiveError, format, params);
	}
	
	@Override
	public void debug(Throwable t, String format, Object... params) {
		log(log.isDebugEnabled(), t, this::receiveDebug, format, params);
	}
	
	private void receiveInfo(Throwable t, CharSequence msg) {
		if(t != null) {
			log.info(msg, t);
		} else {
			log.info(msg);
		}
	}
	
	private void receiveError(Throwable t, CharSequence msg) {
		if(t != null) {
			log.error(msg, t);
		} else {
			log.error(msg);
		}
	}
	
	private void receiveWarn(Throwable t, CharSequence msg) {
		if(t != null) {
			log.warn(msg, t);
		} else {
			log.warn(msg);
		}
	}
	
	private void receiveDebug(Throwable t, CharSequence msg) {
		if(t != null) {
			log.debug(msg, t);
		} else {
			log.debug(msg);
		}
	}
}
