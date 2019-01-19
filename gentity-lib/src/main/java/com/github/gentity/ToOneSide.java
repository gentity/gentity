/*
 * Copyright 2019 The Gentity Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.gentity;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 *
 * @author count
 */
public class ToOneSide<T,O> extends RelationSideBase<T, O> implements RelationSide<T, O>{
	
	final Function<T,O> getter;
	final BiConsumer<T,O> setter;

	public ToOneSide(Function<T, O> getter, BiConsumer<T, O> setter) {
		this.getter = getter;
		this.setter = setter;
	}
	
	
	@Override
	public final void bind(T thisSide, O otherSide) {
		setter.accept(thisSide, otherSide);
	}

	@Override
	public final void unbind(T thisSide, O otherSide) {
		if(bound(thisSide, otherSide)) {
			setter.accept(thisSide, null);
		}
	}

	@Override
	public final boolean bound(T thisSide, O otherSide) {
		return getter.apply(thisSide) == otherSide;
	}
	
}
