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
package com.github.gentity.test.onetomany;

import java.util.Arrays;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author count
 */
public class OneToManyTest {
	
    @Test
    public void testBothSidesConnected() {
        assertSame(FurniturePiece.relationTo$room.getOther(), Room.relationTo$furniture);
        assertSame(Room.relationTo$furniture.getOther(), FurniturePiece.relationTo$room);
    }
    
	@Test
	public void test() {
		FurniturePiece chair1 = new FurniturePiece("chair1");
		FurniturePiece chair2 = new FurniturePiece("chair2");
		FurniturePiece table = new FurniturePiece("table");
		FurniturePiece couch = new FurniturePiece("couch");
		FurniturePiece bed = new FurniturePiece("bed");
		FurniturePiece cupboard = new FurniturePiece("cupboard");
		
		Room bedroom = new Room("bedroom");
		Room livingroom = new Room("livingroom");
		
		// we put chairs in rooms
		livingroom.getFurniture().addAll(Arrays.asList(
			chair1, chair2, table, couch
		));
		
		bed.setRoom(bedroom);
		cupboard.setRoom(bedroom);
		
		assertEquals(livingroom, chair1.getRoom());
		assertEquals(livingroom, chair2.getRoom());
		assertEquals(livingroom, table.getRoom());
		assertEquals(livingroom, couch.getRoom());
		assertEquals(4, livingroom.getFurniture().size());
		
		assertTrue(bedroom.getFurniture().contains(bed));
		assertTrue(bedroom.getFurniture().contains(cupboard));
		assertEquals(2, bedroom.getFurniture().size());
		
		
		
	}
	
}
