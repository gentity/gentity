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
package com.github.gentity.core;

import com.github.gentity.core.config.dto.CollectionTableDto;
import com.github.gentity.core.config.dto.JoinedEntityTableDto;
import com.github.gentity.core.config.dto.MappingConfigDto;
import com.github.gentity.core.config.dto.RootEntityTableDto;
import com.github.gentity.core.config.dto.SingleTableEntityDto;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 *
 * @author count
 */
public class CollectionTableTravesal<T> {
	
	private Function<RootEntityTableDto, T> rootContextProvider = x -> null;
	private Function<JoinedEntityTableDto, T> joinedContextProvider = x -> null;
	private Function<SingleTableEntityDto, T> singleTableContextProvider = x -> null;
	
	public interface Context<T> {
		public CollectionTableDto getElement();
		public T getParentContext();
	}
	
	private class ContextImpl<T> implements Context<T> {
		private final CollectionTableDto element;
		private final T parentContext;
		@Override
		public CollectionTableDto getElement() {
			return element;
		}

		public ContextImpl(CollectionTableDto element, T parentContext) {
			this.element = element;
			this.parentContext = parentContext;
		}

		@Override
		public T getParentContext() {
			return parentContext;
		}
	}
	
	public static <T> CollectionTableTravesal<T> of(MappingConfigDto configuration,
		Function<RootEntityTableDto, T> rootContextProvider,
		Function<JoinedEntityTableDto, T> joinedContextProvider,
		Function<SingleTableEntityDto, T> singleTableContextProvider
	) {
		return new CollectionTableTravesal(configuration, rootContextProvider, joinedContextProvider, singleTableContextProvider);
	}
	public static <T> CollectionTableTravesal<T> of(MappingConfigDto configuration) {
		return of(configuration, x->null, x->null, x->null);
	}
	
	private final MappingConfigDto cfg;

	private CollectionTableTravesal(MappingConfigDto cfg,
		Function<RootEntityTableDto, T> rootContextProvider,
		Function<JoinedEntityTableDto, T> joinedContextProvider,
		Function<SingleTableEntityDto, T> singleTableContextProvider
	) {
		this.cfg = cfg;
		this.rootContextProvider = rootContextProvider;
		this.joinedContextProvider = joinedContextProvider;
		this.singleTableContextProvider = singleTableContextProvider;
	}
	
	private void traverseJoined(List<JoinedEntityTableDto> jts, Consumer<Context<T>> ctConsumer) {
		for(JoinedEntityTableDto jt : jts) {
			T parent = joinedContextProvider.apply(jt);
			jt.getCollectionTable().stream()
				.forEach(ct -> ctConsumer.accept(new ContextImpl(ct, parent)));
			traverseJoined(jt.getEntityTable(), ctConsumer);
		}
	}
	
	private void traverseSingleTable(List<SingleTableEntityDto> ets, Consumer<Context<T>> ctConsumer) {
		for(SingleTableEntityDto et : ets) {
			T parent = singleTableContextProvider.apply(et);
			et.getCollectionTable().stream()
				.forEach(ct -> ctConsumer.accept(new ContextImpl(ct, parent)));
			traverseSingleTable(et.getEntity(), ctConsumer);
		}
	}
	
	private void traverseRoot(Consumer<Context<T>> ctConsumer) {
		for(Object o : cfg.getExcludeOrJoinTableOrEntityTable()) {
			if(!(o instanceof RootEntityTableDto)) {
				continue;
			}
			RootEntityTableDto et = (RootEntityTableDto)o;
			T parent = rootContextProvider.apply(et);
			et.getCollectionTable().stream()
				.forEach(ct -> ctConsumer.accept(new ContextImpl(ct, parent)));
			if(et.getJoinedHierarchy() != null) {
				traverseJoined(et.getJoinedHierarchy().getEntityTable(), ctConsumer);
			} else if(et.getSingleTableHierarchy() != null) {
				traverseSingleTable(et.getSingleTableHierarchy().getEntity(), ctConsumer);
			}
		}
	}
	
	public void traverse(Consumer<Context<T>> ctConsumer) {
		traverseRoot(ctConsumer);
	}
}
