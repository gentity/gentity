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
public class ToOneSide<T,O> extends RelationSide<T, O>{
	
	final Function<T,O> getter;
	final BiConsumer<T,O> setter;

	private ToOneSide(Function<T, O> getter, BiConsumer<T, O> setter) {
		this.getter = getter;
		this.setter = setter;
	}
	
	public static <T,O> ToOneSide<T,O> of(Function<T, O> getter, BiConsumer<T, O> setter, RelationSide<O, T> otherSide) {
		ToOneSide<T, O> instance = of(getter, setter);
		instance.connect(otherSide);
		return instance;
	}
	
	public static <T,O> ToOneSide<T,O> of(Function<T, O> getter, BiConsumer<T, O> setter) {
		return new ToOneSide<>(getter, setter);
	}
	
	@Override
	public final RelationSide<T,O> bind(T thisSide, O otherSide) {
		setter.accept(thisSide, otherSide);
		return this;
	}

	@Override
	public final RelationSide<T,O> unbind(T thisSide, O otherSide) {
		setter.accept(thisSide, null);
		return this;
	}

	@Override
	public boolean isBound(T thisSide, O otherSide) {
		return getter.apply(thisSide) == otherSide;
	}

	
	/**
	 * Getter variant for {@link ToOneSide}, single-valued association
	 * @param host
	 * @return 
	 */
	public O get(T host) {
		return getter.apply(host);
	}
	
	/**
	 * For {@link ToOneSide}, there is a standard setter implementation
	 * @param host
	 * @param otherSide 
	 */
	public void set(T host, O otherSide) {
		// update binding of other side, if there is another side..
		if(getOther() != null) {
			O current = getter.apply(host);
			// only update if binding state actually changes. This check is
			// very easy on a ToOneSide because it only involves
			// a reference comparison. Because it is typical setter semantics,
			// double-setting is simply ignored. 
			// This is very different on a ToManySide, which has no setter,
			// but Collection.add() semantics instead; especially for List<>, 
			// double-adds won't work properly.
			if(current != otherSide) {
				// for binding to non-null, unbind other side first before
				// re-binding
				if(current != null) {
					getOther().unbind(current, host);
				}
				getOther().bind(otherSide, host);
			}
		}
		
		// update this side: rebind
		setter.accept(host, otherSide);
	}
}
