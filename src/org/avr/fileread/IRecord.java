package org.avr.fileread;

import java.util.List;


/**
 * Common interface for Records (Header, Trailer, data records and Mega Fields
 * @author Alfonso
 *
 */
interface IRecord {

	public String getClassName();
	public List<Field> getFields();
	public boolean isDelimited();
	public String getDelimiter();
	
}
