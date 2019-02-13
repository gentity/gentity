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

import com.github.gentity.ToOneSide;

/**
 *
 * @author upachler
 */
public class Kennel {
	private Dog dog;
	static final ToOneSide<Kennel,Dog> relationTo$dog = ToOneSide.of(m -> m.dog, (m,o) -> m.dog = o, Dog.relationTo$kennel);

	public Dog getDog() {
		return relationTo$dog.get(this);
	}

	
	public void setDog(Dog dog) {
		relationTo$dog.set(this, dog);
	}
	
}
