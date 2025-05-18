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
package com.github.gentity.test.uni.onetomany;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author count
 */
public class UniOneToManyTest {

    @Test
    public void testUniSideNotConnected() {
        // House has no relation, so Inahbitant is not connected to an other side
        Assert.assertNull(Inhabitant.relationTo$house.getOther());
    }
    
    @Test
	public void test() {
		House h = new House("our house");
		
		Inhabitant john = new Inhabitant("John");
		
		john.setHouse(h);
		Assert.assertSame(h, john.getHouse());
	}
}
