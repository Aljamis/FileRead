package org.avr.fileread.records;

import java.util.List;


/**
 * Common interface for Records (Header, Trailer, data records and Mega Fields
 * @author Alfonso
 *
 */
public interface IRecord {

	public List<Field> getFields();
}
