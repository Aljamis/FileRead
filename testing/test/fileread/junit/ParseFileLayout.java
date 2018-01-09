package test.fileread.junit;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.avr.fileread.FileReadWriter;
import org.avr.fileread.exceptions.LayoutException;
import org.junit.Test;
import static org.junit.Assert.*;


/**
 * 
 * @author axviareque
 *
 */
public class ParseFileLayout {
	
//	@Test
//	public void duplicateLayouts() {
//	}
	
	/**
	 * 1 of 2 methods of Exception testing. 
	 * Here the SPCEIFIC exception is thrown and caught while any other exception is a failure.
	 * (see layoutFileNotFound2() for the other method)
	 * 
	 * Layout file does not exist
	 * @throws Exception
	 */
	@Test
	public void layoutFileNotFound() throws Exception {
		try {
			new FileReadWriter("NoFileName");
		} catch (FileNotFoundException fnfEx) {
			// Success!!
		} catch (Exception ex) {
			fail("Expected Exception was not thrown.");
		}
	}
	/**
	 * 2 of 2 methods of Exception testing.
	 * Here the annotation "expected" tells us specifically which exception MUST
	 * be thrown.
	 * (see layoutFileNotFound() for the other method)
	 * 
	 * Layout file does not exist - using expected.
	 * @throws Exception
	 */
	@Test(expected = FileNotFoundException.class)
	public void layoutFileNotFound2() throws Exception {
		new FileReadWriter("NoFileName");
	}
	/**
	 * Layout file does not exist
	 * @throws Exception
	 */
	@Test(expected = IOException.class)
	public void layoutFileNotFoundIOex() throws Exception {
		new FileReadWriter("NoFileName");
	}
	
	
	/**
	 * Layout file is not properly configured
	 * @throws Exception
	 */
	@Test(expected = LayoutException.class)
	public void layoutExceptionThrown() throws Exception {
		new FileReadWriter("testing/layouts/INvalidDelimitedLayout.xml");
	}
	/**
	 * Layout file is not properly configured
	 * @throws Exception
	 */
	@Test(expected = LayoutException.class)
	public void layoutExceptionThrown2() throws Exception {
		new FileReadWriter("testing/layouts/INvalidFixedWidth.xml");
	}
	
	@Test
	public void layoutValidDelimited() {
		try {
			new FileReadWriter("testing/layouts/ValidDelimitedLayout.xml");
		} catch (Exception ex) {
			fail(ex.getClass() +" was thrown.");
		}
	}
	
	@Test
	public void layoutValidFixedWidth() {
		try {
			new FileReadWriter("testing/layouts/ValidFixedWidth.xml");
		} catch (Exception ex) {
			fail(ex.getClass() +" was thrown.");
		}
	}
	
	@Test
	public void TypeElementMissingFromFieldElement() {
		try {
			new FileReadWriter("testing/layouts/DelimitedMissingFieldType.xml");
		} catch (Exception ex) {
			fail(ex.getClass() +" was thrown.");
		}
	}

}
