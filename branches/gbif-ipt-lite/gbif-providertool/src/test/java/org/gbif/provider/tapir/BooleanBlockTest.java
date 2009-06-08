package org.gbif.provider.tapir;


import org.gbif.provider.tapir.filter.And;
import org.gbif.provider.tapir.filter.BooleanBlock;
import org.gbif.provider.tapir.filter.BooleanOperator;
import org.gbif.provider.tapir.filter.LessThan;
import org.gbif.provider.tapir.filter.Like;
import org.gbif.provider.tapir.filter.Not;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class BooleanBlockTest {

	@Test
	public void testIterator() throws Exception {
		BooleanBlock block = new BooleanBlock();
		block.addAtom(new Not());
		block.addAtom(new Like());
		block.addAtom(new And());
		block.addAtom(new LessThan());
		for (BooleanOperator op : block){
//			System.out.println(block);
		}
		assertTrue(block.size()==4);
	}

}
