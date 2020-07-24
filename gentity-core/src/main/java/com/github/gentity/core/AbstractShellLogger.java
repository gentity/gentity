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

import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.function.BiConsumer;

/**
 *
 * @author Uwe pachler
 */
public abstract class AbstractShellLogger implements ShellLogger{
	protected void log(boolean enabled, Throwable t, BiConsumer<Throwable, String> receiver, String format, Object... params) {
		if(!enabled) {
			return;
		}
		String msg;
		try {
			msg = String.format(format, params);
		} catch(IllegalFormatException ifx) {
			msg = "[log format broken, original format:]" + format + "[parameters passed to format:]" + Arrays.toString(params);
		}
		
		receiver.accept(t, msg);
	}
	
}
