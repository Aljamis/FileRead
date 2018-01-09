package org.avr.fileread.records;

import java.util.List;

import org.avr.fileread.exceptions.LayoutException;
import org.avr.fileread.fields.Field;


/**
 * Common interface for Records (Header, Trailer, data records and Mega Fields
 * @author Alfonso
 *
 */
public interface IRecord {

	public String getClassName();
	public List<Field> getFields();
	public boolean isDelimited();
	public String getDelimiter();
	
	public int getMaxLength();
	public String getUid();
	public Integer getUidStart();
	public Integer getUidEnd();
	
	public void addField(Field fld) throws LayoutException;
}
