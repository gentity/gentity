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

/**
 * Models one side of a relation.A {@link RelationSide} implementation wraps
 the member variable that holds the association and abstracts the operations
 required bind/unbind the other side of the relation
 * 
 * @author count
 * @param <T>	Entity type of this side of the relation
 * @param <O>	Entity type of the other side of the relation
 */
public abstract class RelationSide<T,O> {
	private RelationSide<O,T> other = null;
	
	/**
	 * Get other side of the relationship, if it is bidirectional. If the relationship
	 * is unidirectional, no other side is connected.
	 * @return The other side of the relationship, of {@code null} if the 
	 *	relationship is unidirectional.
	 */
	public final RelationSide<O,T> getOther(){
		return other;
	}
	
	/**
	 * Connects the other side of the relation. Note that connecting to a 
	 * {@code null} {@link RelationSide} has no effect
	 * @param other	the other side of the relation to connect to
	 * @return this relation side
	 */
	public RelationSide<T,O> connect(RelationSide<O, T> other) {
		if(other != null) {
			this.other = other;
			if(other.getOther() != this) {
				other.connect(this);
			}
		}
		return this;
	}
	
	
	/**
	 * Binds the instance of this side of the relation to an instance of the
	 * other side. 
	 * This updates the member variable of thisSide backing the
	 * relation. Note that the other side is not updated by this method.
	 * @param thisSide	instance of this side of the relation
	 * @param otherSide	instance of the other side of the relation that should be bound
	 * @return this relation side
	 */
	public abstract RelationSide<T,O> bind(T thisSide, O otherSide);
	
	/**
	 * Checks if the instance of this side of the relation is already bound
	 * to the other side. Note that this call may be expensive because it may
	 * involve searching for the entity in an underlying collection.
	 * @param thisSide	instance of this side of the relation
	 * @param otherSide	instance of the other side of the relation that should be checked if bound
	 * @return true of this side is bound to the other side, false otherwise
	 */
	public abstract boolean isBound(T thisSide, O otherSide);
	
	/**
	 * Removes the binding between the instance of this side of the relation and 
	 * the instance of the other side of the relation. 
	 * This updates the member variable of thisSide backing the
	 * relation. Note that the other side is not updated by this method.
	 * @param thisSide	instance of this side of the relation
	 * @param otherSide	instance of the other side of the relation that should be unbound
	 * @return this relation side
	 */
	public abstract RelationSide<T,O> unbind(T thisSide, O otherSide);
	
}
