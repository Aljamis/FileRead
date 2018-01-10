package org.avr.fileread.records;

import java.util.ArrayList;
import java.util.List;

import org.avr.fileread.exceptions.LayoutException;
import org.avr.fileread.fields.Field;
import org.avr.fileread.fields.MegaField;

public abstract class BasicRecord implements IRecord {
	
	private String className = "";
	private String uid = "";			//  Unique record identifier
	private Integer uidStart = null;
	private Integer uidEnd = null;
	private List<Field> fields = new ArrayList<Field>();
	private String delimiter = "";
	private int maxRecordLength=0;
	
	
	public String getClassName() { return className; }
	public void setClassName(String className) { this.className = className; }
	
	public String getUid() { return uid; }
	public void setUid(String uid) throws LayoutException { this.uid = uid; }
	
	public Integer getUidStart() { return uidStart; }
	public void setUidStart(Integer uidStart) throws LayoutException {
		this.uidStart = uidStart; 
	}
	
	public Integer getUidEnd() { return uidEnd; }
	public void setUidEnd(Integer uidEnd) throws LayoutException { this.uidEnd = uidEnd; }
	
	public List<Field> getFields() { return fields; }
	public void setFields(List<Field> fields) { this.fields = fields; }
	public void addField(Field field) throws LayoutException {
		validateField( field );
		this.fields.add(field);
	}
	
	public boolean isDelimited() {
		if (delimiter == null || delimiter.trim().isEmpty() )
			return false;
		return true; 
	}
//	public void setDelimited(boolean boo) { this.delimited = boo; }
	
	public String getDelimiter() { return this.delimiter; }
	public void setDelimiter(String d) { this.delimiter = d; }
	
	
	/**
	 * Calculate the MAX length of the record iterating through the fields looking for
	 * the field with the greatest getEnd()
	 * @return
	 */
	public int getMaxRecordLength(List<Field> myFields , int maxEnd) { 
		for (Field field : myFields) {
			if (field instanceof MegaField) {
				maxEnd = getMaxRecordLength( ((MegaField)field).getFields() , maxEnd);
				continue;
			}
			
			if(field.getEnd() > maxEnd) {
				int occurances = 0;
				if( field.getOccurs() > 1 ) {
					occurances = ( (field.getOccurs() -1) * (field.getEnd() - field.getStart()) );
				}
				maxEnd = field.getEnd() + occurances;
			}
		}
		this.setMaxRecordLength( maxEnd );
		
		return maxRecordLength;
	}
	public void setMaxRecordLength(int maxRecordLength) { this.maxRecordLength = maxRecordLength; }
	
	
	
	public String toString() {
		StringBuffer str = new StringBuffer();
		str.append("["+ this.getClass().getSimpleName() +"]  "+ this.getClassName() ); 
		
		if ( !this.getUid().isEmpty() ) {
			str.append("\t UID [").append( getUid() ).append("]  start [");
			str.append( getUidStart() ).append("]   end [").append( getUidEnd() ).append("]");
		}
		
		if ( this.getDelimiter() != null || !this.getDelimiter().isEmpty() ) {
			str.append("\t DELIMITER [").append(getDelimiter()).append("]");
		}
			
		
		str.append("\n");
		for (Field field : fields) {
			if (field instanceof MegaField) {
				str.append( ((MegaField) field).toString("\t"));
			} else {
				str.append("\t").append( field.getName() ).append(" - ");
				str.append("start [").append( field.getStart() ).append("] ");		
				str.append("end [").append( field.getEnd() ).append("] ");
				if ( field.getOccurs() > 1 )
					str.append("occurs [").append( field.getOccurs() ).append("] ");
				str.append('\n');
			}
		}
		
		return str.toString();
	}
	
	
	
	
	/**
	 * UID is valid ONLY if all 3 fields have a value.  If any of them is empty throw a LayoutException.
	 * Make sure uid will fit between the starting and ending positions defined.
	 * @return
	 * @throws LayoutException
	 */
	public boolean validUID() throws LayoutException {
		if ( uid == null && uidStart == null && uidEnd == null )
			return true;

		if ( uid != null && !uid.isEmpty() ) {
			if (uidStart == 0 && uidEnd == 0)
				throw new LayoutException(getClassName() +" UID is defined but uidStart and uidEnd is not." );
			if (uid.length() != ( uidEnd - uidStart )) {
				throw new LayoutException(getClassName() +" UID will not fit between positions "+ uidStart +" and "+ uidEnd );
			}
			return true;
		} else {
			/* uid is NULL but uidStart and uidEnd have values */
			if ( ( uidStart!= null && uidEnd !=  null ) ) {
				throw new LayoutException(getClassName() +" uidStart and uidEnd are defined without a UID" );
			}
		}
		return true;
	}
	
	
	
	
	/**
	 * If the record is not delimited
	 *  - defined with start and end
	 *  - start is not greater than the end
	 * then perform Field specific validation
	 * 
	 * @param field
	 * @throws LayoutException
	 */
	private void validateField(Field field) throws LayoutException {
		if ( !isDelimited() ){
			if ( field.getStart() == 0 && field.getEnd() == 0 )
				throw new LayoutException( field.getName() +" field is missing start and end elements.");
			if ( field.getStart() > field.getEnd() )
				throw new LayoutException( field.getName() +" <end> position is before <start> position");
		}
	}
	
	
	
	
	
	/**
	 * Return the maximum length of this record
	 */
	public int getMaxLength() {
		
		return this.getMaxRecordLength( fields , 0);
	}
	
	
	
	private int numberOfFields=0;
	/**
	 * Use this to compare to with the number of delimited tokens found in a 
	 * from the line.
	 * @return
	 */
	public int getNumberOfFields() {
		if (numberOfFields == 0)
			countFields(this.getFields());
		return numberOfFields;
	}
	private void countFields(List<Field> flds) {
		for (Field field : flds) {
			if (field instanceof MegaField) {
				countFields( ((MegaField)field).getFields() );
			} else {
				numberOfFields++;
			}
		}
	}
}
