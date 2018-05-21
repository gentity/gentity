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

import com.github.dbsjpagen.config.CollectionTableDto;
import com.github.dbsjpagen.config.JoinedEntityTableDto;
import com.github.dbsjpagen.config.MappingConfigDto;
import com.github.dbsjpagen.config.RootEntityTableDto;
import com.github.dbsjpagen.config.SingleTableEntityDto;
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
		private final Supplier<T> parentContextSupplier;
		@Override
		public CollectionTableDto getElement() {
			return element;
		}

		public ContextImpl(CollectionTableDto element, Supplier<T> parentContextSupplier) {
			this.element = element;
			this.parentContextSupplier = parentContextSupplier;
		}

		@Override
		public T getParentContext() {
			return parentContextSupplier.get(); 
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
	
	private void visitJoined(List<JoinedEntityTableDto> jts, Consumer<Context<T>> ctConsumer) {
		for(JoinedEntityTableDto jt : jts) {
			jt.getCollectionTable().stream()
				.forEach(ct -> ctConsumer.accept(new ContextImpl(ct, () -> joinedContextProvider.apply(jt))));
			visitJoined(jt.getEntityTable(), ctConsumer);
		}
	}
	
	private void visitSingleTable(List<SingleTableEntityDto> ets, Consumer<Context<T>> ctConsumer) {
		for(SingleTableEntityDto et : ets) {
			et.getCollectionTable().stream()
				.forEach(ct -> ctConsumer.accept(new ContextImpl(ct, () -> singleTableContextProvider.apply(et))));
			visitSingleTable(et.getEntity(), ctConsumer);
		}
	}
	
	private void visitRoot(Consumer<Context<T>> ctConsumer) {
		for(RootEntityTableDto et : cfg.getEntityTable()) {
			et.getCollectionTable().stream()
				.forEach(ct -> ctConsumer.accept(new ContextImpl(ct, () -> rootContextProvider.apply(et))));
			if(et.getJoinedHierarchy() != null) {
				visitJoined(et.getJoinedHierarchy().getEntityTable(), ctConsumer);
			} else if(et.getSingleTableHierarchy() != null) {
				visitSingleTable(et.getSingleTableHierarchy().getEntity(), ctConsumer);
			}
		}
	}
	
	public void traverse(Consumer<Context<T>> ctConsumer) {
		visitRoot(ctConsumer);
	}
}
