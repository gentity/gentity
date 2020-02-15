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
package com.github.gentity.core.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

/**
 * A supplier-like interface providing {@link InputStream} instances. A supplier
 * alone cannot be used to supply InputStream instances because supplying them
 * may case {@link IOException}s, which a {@link Supplier#get()} does not 
 * support throwing. Hence the need for a specialized supplier.
 * @author count
 */
public interface InputStreamSupplier {
	InputStream get() throws IOException;
}
