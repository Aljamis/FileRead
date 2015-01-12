package org.avr.fileread.records;

import java.util.List;


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
	
}
