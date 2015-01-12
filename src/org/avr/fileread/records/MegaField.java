package org.avr.fileread.records;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is the top level container for an object that is a member
 * variable of some other object.
 * 
 * For example a Person object has member variables:
 * 		String firstName
 * 		String lastName
 * 		Address homeAddress
 * Home address is of type Address which has member variables:
 * 		String addr1
 * 		String city
 * 		String state
 * 		String zip
 * 
 * @author Alfonso
 *
 */
public class MegaField extends Field implements IRecord {
	
	private String className;
	private List<Field> fields = new ArrayList<Field>();
	
	public String getClassName() { return className; }
	public void setClassName(String className) { this.className = className; }
	public List<Field> getFields() { return fields; }
	public void setFields(List<Field> fields) { this.fields = fields; }
	
	
	public String toString() {
		return this.toString("");
	}
	
	protected String toString(String prefix) {
		StringBuffer str = new StringBuffer();
		str.append( prefix +"["+ this.getClass().getSimpleName() +"]  "+ this.getClassName() ).append("\n");
		
		for (Field field : fields) {			if (field instanceof MegaField) {
			str.append( ((MegaField) field).toString( prefix+"\t"));
		} else {
			str.append( prefix+"\t" ).append( field.getName() ).append(" - ");
			str.append("start [").append( field.getStart() ).append("] ");		
			str.append("end [").append( field.getEnd() ).append("] ");
			if ( field.getOccurs() > 1 )
				str.append("occurs [").append( field.getOccurs() ).append("] ");
			str.append('\n');
		}
}
		
		return str.toString();
	}
}
