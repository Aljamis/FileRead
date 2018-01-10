package org.avr.fileread.dom;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.FileNotFoundException;

import org.avr.fileread.exceptions.LayoutException;


public class NaviagatorTest {
	
	
	@Test(expected = FileNotFoundException.class)
	public void layoutFileNotFound() throws Exception {
		Navigator n = new Navigator();
		n.parseFromFile("OhBlahDi");
		
		fail("Should not have been able to parse this because file is not found");
	}
	
	@Test
	public void layoutFileFound() throws Exception {
		Navigator n = new Navigator();
		n.parseFromFile("testing/layouts/ValidFixedWidth.xml");
		
		assertTrue(true);
	}
	
	@Test
	public void rootElementNotFileLayout() {
		try {
			Navigator n = new Navigator();
			n.parseFromString("<filelyout><record></record><record></record></filelyout>");
		} catch (LayoutException ex) {
			assertTrue(ex.getMessage().startsWith("Root element is <"));
		}
	}
	
	@Test
	public void delimiterIsEmpty() {
		String xml = "<FileLayout>"
				+ "<record classname=\"org.ClassName\" delimiter=\"\" ><field name=\"mbrVariable\"><type>String</type><start>8</start><end>9</end></field></record>"
				+ "</FileLayout>";
		try {			
			Navigator n = new Navigator();
			n.parseFromString( xml );
		} catch (LayoutException ex) {
			assertTrue( ex.getMessage().equals("delimiter attribute provided but it's empty") );
			return;
		}
		fail("delimiter attribute provided but it's empty");
	}
	
	@Test
	public void tooManyHeadersInLayoutFile() {
		String xml = "<FileLayout>"
				+ "<header classname=\"org.ClassName\" ><field name=\"mbrVariable\"><type>String</type><start>8</start><end>9</end></field></header>"
				+ "<header classname=\"org.ClassName2\" ><field name=\"mbrVariable\"><type>String</type><start>8</start><end>9</end></field></header>"
				+ "</FileLayout>";
		try {			
			Navigator n = new Navigator();
			n.parseFromString( xml );
		} catch (LayoutException ex) {
			assertTrue( ex.getMessage().equals("Header already exists.") );
			return;
		}
		fail("XML has 2 <header> nodes.");
	}
	
	@Test
	public void tooManyTrailersInLayoutFile() {
		String xml = "<FileLayout>"
				+ "<trailer classname=\"org.ClassName\" ><field name=\"mbrVariable\"><type>String</type><start>8</start><end>9</end></field></trailer>"
				+ "<trailer classname=\"org.ClassName2\" ><field name=\"mbrVariable\"><type>String</type><start>8</start><end>9</end></field></trailer>"
				+ "</FileLayout>";
		try {			
			Navigator n = new Navigator();
			n.parseFromString( xml );
		} catch (LayoutException ex) {
			assertTrue( ex.getMessage().equals("Trailer already exists.") );
			return;
		}
		fail("XML has 2 <trailer> nodes.");
	}
	
	
	
	
	
	/*   ______ _      _     _    
	 *  |  ____(_)    | |   | |    
	 *  | |__   _  ___| | __| |___ 
	 *  |  __| | |/ _ \ |/ _` / __|
	 *  | |    | |  __/ | (_| \__ \
	 *  |_|    |_|\___|_|\__,_|___/
	 */
	
	
	@Test
	public void dataRecordMissingFields() {
		try {			
			Navigator n = new Navigator();
			n.parseFromString("<FileLayout><record/><record></record></FileLayout>");
		} catch (LayoutException ex) {
			assertTrue( ex.getMessage().startsWith("A record should have have some <fields>") );
		}
	}
	
	@Test
	public void fieldIsEmpty() {
		try {			
			Navigator n = new Navigator();
			n.parseFromString("<FileLayout><record><field></field></record></FileLayout>");
		} catch (LayoutException ex) {
			assertTrue( ex.getMessage().contains("is missing classname attribute") );
		}
	}
	
	@Test
	public void recordWithUIDmissingStartAndEnd() {
		try {
			Navigator n = new Navigator();
			n.parseFromString("<FileLayout><record classname=\"org.ClassName\" uid=\"|\"><field></field></record></FileLayout>");
		} catch (LayoutException ex) {
			assertTrue( ex.getMessage().contains(" UID is defined but uidStart and uidEnd is not.") );
			return;
		}
	}
	
	@Test
	public void recordWithEmptyUIDmissingStartAndEnd() {
		try {
			Navigator n = new Navigator();
			n.parseFromString("<FileLayout><record classname=\"org.ClassName\" uid=\"\"><field></field></record></FileLayout>");
		} catch (LayoutException ex) {
			assertTrue( ex.getMessage().contains("uid attribute is undefined.") );
			return;
		}
	}
	
	@Test
	public void recordWithUIDandEndBeforeStart() {
		try {			
			Navigator n = new Navigator();
			n.parseFromString("<FileLayout><record classname=\"org.ClassName\" uid=\"|\" uidStart=\"9\" uidEnd=\"6\"><field></field></record></FileLayout>");
		} catch (LayoutException ex) {
			assertTrue( ex.getMessage().contains(" UID will not fit between positions ") );
			return;
		}
		fail("UID start position is after END position.");
	}
	
	@Test
	public void recordWithUIDnotMatchingLength() {
		try {			
			Navigator n = new Navigator();
			n.parseFromString("<FileLayout><record classname=\"org.ClassName\" uid=\"|\" uidStart=\"9\" uidEnd=\"16\"><field></field></record></FileLayout>");
		} catch (LayoutException ex) {
			assertTrue( ex.getMessage().contains(" UID will not fit between positions ") );
			return;
		}
		fail("UID not the same length as defined");
	}
	
	@Test
	public void recordWithStartAndEndWithoutUID() throws Exception {
		Navigator n = new Navigator();
		n.parseFromString("<FileLayout><record classname=\"org.ClassName\" uidStart=\"9\" uidEnd=\"6\"><field name=\"org.ClassNamd\"><type>String</type><start>8</start><end>9</end></field></record></FileLayout>");
		
		assertTrue(true);
	}
	
	@Test
	public void fieldStartNotANumber() throws Exception {
		try {			
			Navigator n = new Navigator();
			n.parseFromString("<FileLayout><record classname=\"org.ClassName\" uid=\"|\" uidStart=\"xx\" uidEnd=\"8\"><field></field></record></FileLayout>");
		} catch (LayoutException ex) {
			assertTrue( ex.getMessage().startsWith("uidStart is not a valid number [") );
			return;
		}
		fail("UID start position is not a number.");
	}
	
	@Test
	public void fieldEndNotANumber() throws Exception {
		try {			
			Navigator n = new Navigator();
			n.parseFromString("<FileLayout><record classname=\"org.ClassName\" uid=\"|\" uidStart=\"6\" uidEnd=\"x8\"><field></field></record></FileLayout>");
		} catch (LayoutException ex) {
			assertTrue( ex.getMessage().startsWith("uidEnd is not a valid number [") );
			return;
		}
		fail("UID start position is not a number.");
	}
	
	@Test
	public void multipleDataRecordsWithSameClassType() {
		String xml = "<FileLayout>"
				+ "<record classname=\"org.ClassName\" ><field name=\"mbrVariable\"><type>String</type><start>8</start><end>9</end></field></record>"
				+ "<record classname=\"org.ClassName\" ><field name=\"mbrVariable\"><type>String</type><start>8</start><end>9</end></field></record>"
				+ "</FileLayout>";
		try {			
			Navigator n = new Navigator();
			n.parseFromString( xml );
		} catch (LayoutException ex) {
			assertTrue( ex.getMessage().endsWith("] already exists as a layout.  Must use a different ClassName.") );
			return;
		}
		fail("Layout has multiple records of the same class.");
	}
	
	@Test
	public void invalidTagWhereFieldShouldBe() {
		try {			
			Navigator n = new Navigator();
			n.parseFromString("<FileLayout><record classname=\"org.ClassName\"><feld></feld></record></FileLayout>");
		} catch (LayoutException ex) {
			assertTrue( ex.getMessage().endsWith(" is not a valid field type (field, megaField)") );
			return;
		}
		fail("<field> has been misspelled.");
	}
	
	@Test
	public void fieldMissingNameAttribute() {
		try {			
			Navigator n = new Navigator();
			n.parseFromString("<FileLayout><record classname=\"org.ClassName\" ><field></field></record></FileLayout>");
		} catch (LayoutException ex) {
			assertTrue( ex.getMessage().equals("name attribute missing from <field>") );
			return;
		}
		fail("<field> is missing name attribute.");
	}
	
	@Test
	public void fieldWithInvalidNode() {
		try {			
			Navigator n = new Navigator();
			n.parseFromString("<FileLayout><record classname=\"org.ClassName\" ><field name=\"mbrVariable\"><type format=\"MMddyyyy\">date</type><tart>2</tart><end>7</end></field></record></FileLayout>");
		} catch (LayoutException ex) {
			assertTrue( ex.getMessage().endsWith(" is not a valid node for field") );
			return;
		}
		fail("<field> is a date but the format is not usable by SimpleDateFormat");
	}
	
	@Test
	public void fieldMissingTypeNode() {
		try {			
			Navigator n = new Navigator();
			n.parseFromString("<FileLayout><record classname=\"org.ClassName\" ><field name=\"mbrVariable\" ></field></record></FileLayout>");
		} catch (LayoutException ex) {
			assertTrue( ex.getMessage().equals("Missing Field details (type, start, end)") );
			return;
		}
		fail("<field> is missing <type> node.");
	}
	
	@Test
	public void fieldAttributeOccursNotAnumber() {
		try {			
			Navigator n = new Navigator();
			n.parseFromString("<FileLayout><record classname=\"org.ClassName\" ><field name=\"mbrVariable\" occurs=\"x\"><type>String</type></field></record></FileLayout>");
		} catch (LayoutException ex) {
			assertTrue( ex.getMessage().startsWith("Occurs is not a valid number [") );
			return;
		}
		fail("<field> is missing <type> node.");
	}
	
	@Test
	public void fieldMissingStartAndEnd() {
		try {			
			Navigator n = new Navigator();
			n.parseFromString("<FileLayout><record classname=\"org.ClassName\" ><field name=\"mbrVariable\"><type>String</type></field></record></FileLayout>");
		} catch (LayoutException ex) {
			assertTrue( ex.getMessage().endsWith(" field is missing start and end elements.") );
			return;
		}
		fail("<field> is missing <start> and <end> nodes.");
	}
	
	@Test
	public void fieldStartAndEndAreZeroes() {
		try {			
			Navigator n = new Navigator();
			n.parseFromString("<FileLayout><record classname=\"org.ClassName\" ><field name=\"mbrVariable\"><type>String</type><start>0</start><end>0</end></field></record></FileLayout>");
		} catch (LayoutException ex) {
			assertTrue( ex.getMessage().endsWith(" field is missing start and end elements.") );
			return;
		}
		fail("<field> is missing <start> and <end> nodes.");
	}
	
	@Test
	public void fieldEndsBeforeItStarts() {
		try {			
			Navigator n = new Navigator();
			n.parseFromString("<FileLayout><record classname=\"org.ClassName\" ><field name=\"mbrVariable\"><type>String</type><start>18</start><end>9</end></field></record></FileLayout>");
		} catch (LayoutException ex) {
			assertTrue( ex.getMessage().endsWith(" <end> position is before <start> position") );
			return;
		}
		fail("<field> <end> position is before <start> position.");
	}
	
	@Test
	public void dateFieldMissingFormat() {
		try {			
			Navigator n = new Navigator();
			n.parseFromString("<FileLayout><record classname=\"org.ClassName\" ><field name=\"mbrVariable\"><type>date</type><start>8</start><end>9</end></field></record></FileLayout>");
		} catch (LayoutException ex) {
			assertTrue( ex.getMessage().endsWith(" missing format.") );
			return;
		}
		fail("<field> is a date but the format attribute is missing.");
	}
	
	@Test
	public void dateFieldWithInvalidFormat() {
		try {			
			Navigator n = new Navigator();
			n.parseFromString("<FileLayout><record classname=\"org.ClassName\" ><field name=\"mbrVariable\"><type format=\"XxXx\">date</type><start>8</start><end>11</end></field></record></FileLayout>");
		} catch (LayoutException ex) {
			assertTrue( ex.getMessage().startsWith("Invalid Date Format [") );
			return;
		}
		fail("<field> is a date but the format is not usable by SimpleDateFormat");
	}
	
//	@Test
	/**
	 * This is not a valid test because if the RECORD is delimited the start and end fields will be
	 * omitted.
	 */
	public void dateFormatLengthDontMatchStartToEnd() {
		try {			
			Navigator n = new Navigator();
			n.parseFromString("<FileLayout><record classname=\"org.ClassName\" ><field name=\"mbrVariable\"><type format=\"MMddyyyy\">date</type><start>8</start><end>9</end></field></record></FileLayout>");
		} catch (LayoutException ex) {
			assertTrue( ex.getMessage().startsWith("format attribute does not match length between <start> and <end>") );
			return;
		}
		// You're going to have to test for this before adding the field to the collection
		fail("<field> is a date but the format is not usable by SimpleDateFormat");
	}
	
	@Test
	public void megaFieldMissingNameAttribute() {
		try {			
			Navigator n = new Navigator();
			n.parseFromString("<FileLayout><record classname=\"org.ClassName\" ><megaField classname=\"org.OtherClass\"><type>string</type><start>8</start><end>11</end></megaField></record></FileLayout>");
		} catch (LayoutException ex) {
			assertTrue( ex.getMessage().equals("name attribute missing from <MegaField>") );
			return;
		}
		fail("<MegaField> missing name attribute.");
	}
	
	@Test
	public void megaFieldMissingClassNameAttribute() {
		try {			
			Navigator n = new Navigator();
			n.parseFromString("<FileLayout><record classname=\"org.ClassName\" ><megaField name=\"mbrVariable\"><type>string</type><start>8</start><end>11</end></megaField></record></FileLayout>");
		} catch (LayoutException ex) {
			assertTrue( ex.getMessage().equals("classname attribute missing from <MegaField>") );
			return;
		}
		fail("<MegaField> missing name attribute.");
	}
}
