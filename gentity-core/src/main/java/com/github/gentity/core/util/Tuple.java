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
package com.github.gentity.core.util;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 *
 * @author upachler
 */
public class Tuple<X,Y> {
	private final X x;
	private final Y y;

	public Tuple(X x, Y y) {
		this.x = x;
		this.y = y;
	}
	
	public static <X,Y> Tuple<X,Y> of(Map.Entry<X,Y>e) {
		return of(e.getKey(), e.getValue());
	}
	
	public static <X,Y> Tuple<X,Y> of(X x, Y y) {
		return new Tuple(x,y);
	}

	public final Tuple<Y,X> swap() {
		return new Tuple<>(y(),x());
	}
	
	public final X x() {
		return x;
	}
	
	public final Y y() {
		return y;
	}
	
	public final X getX() {
		return x;
	}
	
	public final Y getY() {
		return y;
	}
	
	public final <X2> Tuple<X2,Y> mapX(Function<X,X2> mapper){
		return replaceX(mapper.apply(x));
	}
	
	public final <X2> Tuple<X2,Y> replaceX(X2 x2){
		return new Tuple<>(x2, y());
	}
	
	public final <Y2> Tuple<X,Y2> mapY(Function<Y,Y2> mapper){
		return replaceY(mapper.apply(y));
	}
	
	public final <Y2> Tuple<X,Y2> replaceY(Y2 y2){
		return new Tuple<>(x, y2);
	}
	
	@Override
	public final int hashCode() {
		return Objects.hash(x, y);
	}

	@Override
	public final boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Tuple<?, ?> other = (Tuple<?, ?>) obj;
		if (!Objects.equals(this.x, other.x)) {
			return false;
		}
		if (!Objects.equals(this.y, other.y)) {
			return false;
		}
		return true;
	}
}
