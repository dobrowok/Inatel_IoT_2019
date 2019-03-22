import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

// http://tutorials.jenkov.com/java/fields.html
// https://stackify.com/java-logging-best-practices/
//  

public class Argos {
	
	PropSingleton prop; 
	CommInterface commInterface;
	ArgosCV       argosCV;
	ArgosSensor   argosSensor;
	
	private static final Logger LOGGER = Logger.getLogger(Argos.class.getName());
	
	char []buff = new char[50];
	String temp ;
	
	int  buffSize = 50;
	
	private void logStart() {
		Handler consoleHandler = null;
		Handler fileHandler  = null;
		try{
			//Creating consoleHandler and fileHandler
			consoleHandler = new ConsoleHandler();
			fileHandler  = new FileHandler("./" +Argos.class.getName() +".log");
			
			//Assigning handlers to LOGGER object
			LOGGER.addHandler(consoleHandler);
			LOGGER.addHandler(fileHandler);
			
			//Setting levels to handlers and LOGGER
			consoleHandler.setLevel(Level.ALL);
			fileHandler.setLevel(Level.ALL);
			LOGGER.setLevel(Level.ALL);
			
			LOGGER.config("Configuration done.");
			
			//Console handler removed
			LOGGER.removeHandler(consoleHandler);
			
			LOGGER.log(Level.FINE, "Finer logged");
			
		}catch(IOException exception){
			LOGGER.log(Level.SEVERE, "Error occur in FileHandler.", exception);
		}
		
		LOGGER.finer("Finest example on LOGGER handler completed.");
	}
	
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
		System.out.println("constructor stub");
		logStart();
		
		
		 prop = PropSingleton.INSTANCE;
		
		 if(prop.getProp("mqtt.type").equals("aws") || prop.getProp("mqtt.type").equals("mosquitto") )
			 commInterface = new CommMQttImpl();
		 else {
			 System.out.println("Error! No Comm");
			 System.exit(-1);
		 }
		
		 commInterface.publish("Status", "brá blá");
		
	}
	
	public void run() {

		while (prop.isRunning()) {
			try {
				Thread.sleep(5000) ;
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		commInterface.disconnect();
	}
	

	 public static void main(String[] args) {
		 //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		 //Mat mat = Mat.eye(3, 3, CvType.CV_8UC1);
		 Argos argos = new Argos();
		 

		 
		 System.out.println("Sing = " + PropSingleton.INSTANCE.getInstance());
		 
		 System.out.println("mqtt.type = " + PropSingleton.INSTANCE.getProp("mqtt.type")  );
		 argos.run();
		 try {
			Thread.sleep(10000) ;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		 
	 }
}
