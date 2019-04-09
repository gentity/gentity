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
package onetoone;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author upachler
 */
public class OneToOneTest {

	@Test
	public void test() {
		Dog dog = new Dog();
		Kennel kennel = new Kennel();
		
		assertNull(dog.getKennel());
		assertNull(kennel.getDog());
		
		kennel.setDog(dog);
		
		assertSame(kennel, dog.getKennel());
		assertSame(dog, kennel.getDog());
		
		kennel.setDog(null);
		
		assertNull(dog.getKennel());
		assertNull(kennel.getDog());
		
	}
}
