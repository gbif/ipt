package org.gbif.provider.model.voc;


import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class RankTest {

	@Test
	public void testRankOrder() {
		assertTrue(Rank.Family.compareTo(Rank.Order) > 0);
		assertTrue(Rank.Kingdom.compareTo(Rank.Order) < 0);
	}

}
