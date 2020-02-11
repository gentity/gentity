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
import javax.persistence.PreRemove;

/**
 *
 * @author upachler
 */
public class Dog {
	private Kennel kennel;
	static final ToOneSide<Dog,Kennel> relationTo$kennel = ToOneSide.of(m -> m.$removed, m -> m.kennel, (m,o) -> m.kennel = o, Kennel.relationTo$dog);

	private transient boolean $removed;
	@PreRemove
	private void $onPrepersist() {
		$removed = true;
	}

	public Kennel getKennel() {
		return relationTo$kennel.get(this);
	}

	
	
}
