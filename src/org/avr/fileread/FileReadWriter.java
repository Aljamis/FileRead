package org.avr.fileread;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.avr.fileread.dom.Navigator;
import org.avr.fileread.exceptions.LayoutException;
import org.avr.fileread.records.UnParsableRecord;

/**
 * The principle class in this API used to read an XML (record layout) file and
 * read lines from a file.  Basic DOM XML parsing produces a {@link org.avr.fileread.FileLayout }.
 * This is the blue-print or record definition
 * 
 * @author Alfonso
 *
 */
public class FileReadWriter {
	private Logger logger = Logger.getLogger( FileReadWriter.class );
	
	private String layoutFileName;				//  The XML file storing the record layout
	private FileDigester myDigester = new FileDigester();
	private BufferedReader reader = null;
	
	private BufferedWriter buffWriter;
	private String outFileName = "";
	public String getOutFileName() { return this.outFileName; }
	public void setOutFileName(String out) { this.outFileName = out; }
	
	private AtomicInteger recordsRead = new AtomicInteger();
	
	public FileReadWriter(String fileLayout , String fileName) throws LayoutException , IOException {
		this.layoutFileName = fileLayout;
		this.myDigester.setMyFileLayouts( Navigator.getLayout(fileLayout));
		this.openFile( fileName );
	}
	
	
	/**
	 * This constructor is exclusive to instantiating OUTPUT files.
	 * @param fileLayout
	 * @throws IOException
	 * @throws LayoutException
	 */
	public FileReadWriter(String fileLayout ) throws LayoutException , IOException {
		this.layoutFileName = fileLayout;
		this.myDigester.setMyFileLayouts( Navigator.getLayout(fileLayout));
	}

	
	
	
	/**
	 * Open file for reading.
	 * @param fileName
	 * @throws IOException
	 */
	private void openFile(String fileName ) throws IOException {
		Path paths = FileSystems.getDefault().getPath( fileName );
		try {
			this.reader = Files.newBufferedReader( paths , Charset.defaultCharset() );
			logger.info("FILE ["+ fileName +"] has been opened.");
		} catch (IOException ioEx) {
			logger.fatal("Could not open file: "+ fileName );
			throw ioEx;
		}
	}
	
	
	
	
	/**
	 * Read a line from the file returning an Object defined in the layout
	 * XML.
	 * 
	 * @return
	 */
	public Object readNextLine() throws IOException {
		String line = null;
		if ( (line = reader.readLine()) != null ) {
			recordsRead.incrementAndGet();
			return myDigester.parseLine(line);
		}
		return null;
	}
	
	
	
	
	/**
	 * Write Object to a FileLayout
	 * @param obj
	 * @throws IOException
	 */
	public void write (Object obj) throws IOException {
		if (buffWriter == null)
			createOutFile();
		
		String out = "";
		if (obj instanceof UnParsableRecord) 
			out = ((UnParsableRecord)obj).getLine();
		else
			out = myDigester.buildLine( obj );
		
		if (out == null) {
			logger.warn("["+ layoutFileName +"] does not have a record for ["+ obj.getClass().getName() +"]");
		} else {
			this.buffWriter.write(out);
			this.buffWriter.newLine();
			this.buffWriter.flush();
		}
	}
	
	
	
	
	/**
	 * Create an OUT FILE to write to
	 */
	private void createOutFile() throws IOException {
		if (this.outFileName == null || this.outFileName.trim().length() == 0)
			throw new IOException("Output file has not been named");
		this.buffWriter = new BufferedWriter( new FileWriter( this.outFileName) ) ;
	}
}