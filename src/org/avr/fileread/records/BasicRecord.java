package org.avr.fileread.records;

import java.util.ArrayList;
import java.util.List;

import org.avr.fileread.exceptions.LayoutException;

public abstract class BasicRecord implements IRecord {
	
	private String className = "";
	private String uid = null;			//  Unique record identifier
	private Integer uidStart = null;
	private Integer uidEnd = null;
	private List<Field> fields = new ArrayList<Field>();
	private boolean delimited = false;
	private String delimiter = "";
	
	
	public String getClassName() { return className; }
	public void setClassName(String className) { this.className = className; }
	
	public String getUid() { return uid; }
	public void setUid(String uid) throws LayoutException { this.uid = uid; validate(); }
	
	public Integer getUidStart() { return uidStart; }
	public void setUidStart(Integer uidStart) throws LayoutException { this.uidStart = uidStart; validate(); }
	
	public Integer getUidEnd() { return uidEnd; }
	public void setUidEnd(Integer uidEnd) throws LayoutException { this.uidEnd = uidEnd; validate(); }
	
	public List<Field> getFields() { return fields; }
	public void setFields(List<Field> fields) { this.fields = fields; }
	
	public boolean isDelimited() { return this.delimited; }
	public void setDelimited(boolean boo) { this.delimited = boo; }
	
	public String getDelimiter() { return this.delimiter; }
	public void setDelimiter(String d) { this.delimiter = d; }
	
	
	public String toString() {
		StringBuffer str = new StringBuffer();
		str.append("["+ this.getClass().getSimpleName() +"]  "+ this.getClassName() ); 
		
		if ( !this.getUid().isEmpty() ) {
			str.append("\t delimiter [").append( getUid() ).append("]  start [");
			str.append( getUidStart() ).append("]   end [").append( getUidEnd() ).append("]");
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
	 * Make sure uid will fit between the starting and ending positions defined.
	 * @return
	 * @throws LayoutException
	 */
	private boolean validate() throws LayoutException {
		if ( uid != null
				&& uidStart!= null
				&& uidEnd !=  null) {
			if (uid.length() != ( 1+ uidEnd - uidStart )) {
				throw new LayoutException(getClassName() +" UID will not fit between positions "+ uidStart +" and "+ uidEnd );
			}
			return true;
		}
		return false;
	}
	
	
	/**
	 * UID is valid ONLY if all 3 fields have a value.  If any of them is empty throw a LayoutException.
	 * @return
	 * @throws LayoutException
	 */
	public boolean validUID() throws LayoutException {
		if ( uid == null && uidStart == null && uidEnd == null )
			return true;
		return validate();
	}
}
