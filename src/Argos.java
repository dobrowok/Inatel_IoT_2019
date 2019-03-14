import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

// http://tutorials.jenkov.com/java/fields.html
// https://stackify.com/java-logging-best-practices/
//  

public class Argos {
	
	ArgosComm   argosComm;
	ArgosCV     argosCV;
	ArgosSensor argosSensor;
	
	char []buff = new char[50];
	String temp ;
	
	int  buffSize = 50;
	
	public static String snprintf( int size, String format, Object ... args ) {
        StringWriter writer = new StringWriter( size );
        PrintWriter out = new PrintWriter( writer );
        out.printf( format, args );
        out.close();
        return writer.toString();
    }
	
	public void LCDPrint(String text) {
		  int textLen  = text.length();

		  System.out.println("txtLen=" +textLen +"; buffSize-textLen=" +(buffSize-textLen));
		  
		  
		  temp = snprintf(buffSize-textLen, "%s", buff[textLen]);
		    
		    //display.drawStringMaxWidth(0, 0, 128, "kkkkkkkk" /*text.c_str()*/ );
		  //display.drawString(0, 0, "Hello World");
		  //display.drawStringMaxWidth(0, 0, 128, "Lorem ipsum\n dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore." );
		  //display.display();

		  //Serial.print(text);
		  System.out.printf("%s", buff);
		}
	/**
	 * 
	 */
	public Argos() {
		
		// TODO Auto-generated constructor stub
		//name = new ArgosComm();
		argosComm = new ArgosCommMQtt();
		
		argosComm.publish("brá");
		
		
		EnumArgos.INSTANCE.getInstance();
	}
	

	 public static void main(String[] args) {
		 //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		 //Mat mat = Mat.eye(3, 3, CvType.CV_8UC1);
		 System.out.println("Mqtt = " + EnumArgos.INSTANCE);
		 
		 System.out.println("Mqtt = " + EnumArgos.INSTANCE.getProp("mqtt.type") +"(singleton)" );
	 }
}
