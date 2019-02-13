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

import java.util.AbstractList;
import java.util.List;

/**
 * Wraps a given list instance to intercept calls to add() and remove(), so that
 * the other side of the relation can be updated
 * @param <E>	element type of the list (other side of the relationship)
 * @param <T>	type of enclosing class (this side of the relationship)
 * @author count
 */
class ListWrapper<E,T> extends AbstractList<E> {
	
	private final List<E> delegate;
	private final T host;
	private final RelationSide<E,T> otherSide;

	public ListWrapper(List<E> delegate, T host, RelationSide<E, T> otherSide) {
		this.delegate = delegate;
		this.host = host;
		this.otherSide = otherSide;
	}

	
	@Override
	public E get(int index) {
		return delegate.get(index);
	}

	@Override
	public int size() {
		return delegate.size();
	}

	@Override
	public E set(int index, E element) {
		
		checkNotNull(element);
		
		otherSide.bind(element, host);
		
		return delegate.set(index, element);
	}

	@Override
	public E remove(int index) {
		E e = delegate.remove(index);
		otherSide.unbind(e, host);
		return e;
	}

	@Override
	public void add(int index, E element) {
		// we ask the other side if the element is bound. The reason we're not
		// calling this.isBound() is because we KNOW that isBound() is expensive
		// for us (this.isBound() calls a Collection.contains()) - but for the
		// other side there is a chance that it is a ToOneSide, for which
		// isBound() is really cheap (a refernce comparison)
		if(otherSide.isBound(element, host)) {
			throw new IllegalStateException(String.format("cannot add object %s to collection in object %s, because it is already in that collection", element, host));
		}
		checkNotNull(element);
		delegate.add(index, element);
		otherSide.bind(element, host);
	}

	private void checkNotNull(E element) {
		if(element == null) {
			throw new NullPointerException("provided collection element may not be null");
		}
	}
	
}
