package test.avr;

import org.avr.Member;
import org.avr.MyHeader;
import org.avr.MyTrailer;
import org.avr.Spouse;
import org.avr.fileread.FileReadWriter;
import org.avr.fileread.records.UnParsableRecord;


/**
 * Simple Read layout and parse a file
 * @author Alfonso
 *
 */
public class ParseFile {

	public static void main(String[] args) {
		
		try{ 
			FileReadWriter reader = new FileReadWriter(args[0] , args[1]);
			
			Object obj = null;
			while ( (obj = reader.readNextLine()) != null ) {
				if (obj instanceof Member) {
					Member mbr = (Member) obj;
					System.out.println( mbr.getFirstName() +" "+ mbr.getLastName() +"     "+ mbr.getDob() );
					mbr.setFirstName("Kuovelo");
				} else if( obj instanceof MyHeader ) {
					System.out.println("  Rec Count : "+ ((MyHeader)obj).getRecCount() );
				} else if ( obj instanceof UnParsableRecord) {
					System.out.println( ((UnParsableRecord)obj).getDescription() +" : "+ ((UnParsableRecord)obj).getLine() );
				} else if( obj instanceof MyTrailer ) {
					System.out.println(" TRLR Count : "+ ((MyTrailer)obj).getRecCount() );
				}
//				
//				if (obj instanceof Spouse)
//					writer.write( (Member)obj );
//				else 
//					writer.write( obj );
				
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
