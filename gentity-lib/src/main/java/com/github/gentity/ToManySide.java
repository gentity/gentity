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
public class ToManySide<T, O> extends RelationSideBase<T, O> implements RelationSide<T, O>{
	
	final Function<T,? extends Collection<O>> collectionProvider;

	public ToManySide(Function<T, ? extends Collection<O>> collectionProvider) {
		this.collectionProvider = collectionProvider;
	}
	
	@Override
	public void bind(T thisSide, O otherSide) {
		collectionProvider
			.apply(thisSide)
			.add(otherSide);
	}

	@Override
	public void unbind(T thisSide, O otherSide) {
		collectionProvider
			.apply(thisSide)
			.remove(otherSide);
	}

	@Override
	public boolean bound(T thisSide, O otherSide) {
		return collectionProvider
			.apply(thisSide)
			.contains(otherSide);
	}

	public List<O> wrap(List<O> list, T host) {
		return new ListWrapper(list, host, getOther());
	}
}
