package test.avr;

import org.avr.Member;
import org.avr.fileread.FileReadWriter;
import org.avr.fileread.records.UnParsableRecord;

public class ParseFile {

	public static void main(String[] args) {
		
		try{ 
			FileReadWriter reader = new FileReadWriter(args[0] , args[1]);
			
			System.out.println( reader.displayFields() );
			
			Object obj = null;
			while ( (obj = reader.readNextLine()) != null ) {
				if (obj instanceof Member) {
					Member mbr = (Member) obj;
					System.out.println( mbr.getEmail() );
				} else {
					if ( obj instanceof UnParsableRecord)
						System.out.println("Unparseable");
				}
				
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
