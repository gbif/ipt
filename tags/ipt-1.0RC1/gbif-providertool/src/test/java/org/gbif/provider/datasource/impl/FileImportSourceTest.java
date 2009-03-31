package org.gbif.provider.datasource.impl;


import static org.junit.Assert.*;

import org.gbif.provider.datasource.ImportSource;
import org.junit.Before;
import org.junit.Test;

public class FileImportSourceTest {

	private String escapeRawValue(String val){
		return FileImportSource.ESCAPE_PATTERN.matcher(val).replaceAll(" ");
	}
	
	@Test
	public void testEscapes(){
		String input = " hallo Bernd. / you öüΩ∂6782  gf ";
		assertEquals(escapeRawValue(input), input);
		input += " fhdf    f f  f f  ";
		assertEquals(escapeRawValue(input), input);
		String input2 = input + "tab	tab";
		assertFalse(input2.equals(escapeRawValue(input2)));
		input2 = input + "br\nbr";
		assertFalse(input2.equals(escapeRawValue(input2)));
	}

}
