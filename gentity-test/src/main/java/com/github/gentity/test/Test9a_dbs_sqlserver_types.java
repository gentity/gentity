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
package com.github.gentity.test;

import com.github.gentity.test.test9a_dbs_sqlserver_types.SqlserverSpecific;
import org.junit.Test;

/**
 *
 * @author upachler
 */
public class Test9a_dbs_sqlserver_types {
	@Test
	public void test() {
		// if this compiles we're good.
		SqlserverSpecific sss = new SqlserverSpecific.Builder()
			.strNtext("föö_bär")
			.strText("foo_bar")
			.binImage(new byte[]{(byte)0xaf, (byte)0xf3})
			.build();
	}
}
