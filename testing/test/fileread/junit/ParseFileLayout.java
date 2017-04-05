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
	public void missingFieldType() {
		try {
			new FileReadWriter("testing/layouts/DelimitedMissingFieldType.xml");
		} catch (Exception ex) {
			fail(ex.getClass() +" was thrown.");
		}
	}

}
