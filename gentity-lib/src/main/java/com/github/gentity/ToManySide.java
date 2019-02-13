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

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 *
 * @author count
 */
public abstract class ToManySide<T, C extends Collection<O>, O> extends RelationSide<T, O>{
	
	final Function<T,C> collectionProvider;

	private ToManySide(Function<T, C> collectionProvider) {
		this.collectionProvider = collectionProvider;
	}
	
	public static <T,O> ToManySide<T, List<O>, O> of(Function<T, List<O>> listProvider, RelationSide<O, T> other) {
		ToManySide<T, List<O>, O> instance = of(listProvider);
		instance.connect(other);
		return instance;
	}
	
	public static <T,O> ToManySide<T, List<O>, O> of(Function<T, List<O>> listProvider) {
		return new ToManySide<T, List<O>, O>(listProvider) {
			@Override
			public List<O> get(T host) {
				RelationSide<O,T> other = getOther();
				if(other != null) {
					return new ListWrapper(collectionProvider.apply(host), host, getOther());
				} else {
					return collectionProvider.apply(host);
				}
			}
		};
	}

	
	@Override
	public RelationSide<T,O> bind(T thisSide, O otherSide) {
		if(thisSide == null) {
			// We silently ignore binding to null.
			return this;
		}
		collectionProvider
			.apply(thisSide)
			.add(otherSide);
		return this;
	}

	@Override
	public boolean isBound(T thisSide, O otherSide) {
		if(thisSide == null) {
			// null is not bound to anything
			return false;
		}
		return collectionProvider
			.apply(thisSide)
			.contains(otherSide);
	}

	@Override
	public RelationSide<T,O> unbind(T thisSide, O otherSide) {
		if(thisSide == null) {
			// We silently ignore unbinding from null.
			return this;
		}
		collectionProvider
			.apply(thisSide)
			.remove(otherSide);
		return this;
	}

	public abstract C get(T host);
}
