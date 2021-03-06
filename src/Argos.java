import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.StringJoiner;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.opencv.core.Core;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLConnection;
import org.opencv.core.Mat;

// http://tutorials.jenkov.com/java/fields.html
// https://stackify.com/java-logging-best-practices/
// https://dzone.com/articles/programmatically-restart-java
// https://www.alatortsev.com/2018/11/21/installing-opencv-4-0-on-raspberry-pi-3-b/
// https://www.pyimagesearch.com/2018/09/26/install-opencv-4-on-your-raspberry-pi/

// https://answers.opencv.org/question/193924/failed-to-use-systemloadlibrarycorenative_library_name-on-raspberry-pi/

/*
 * Rodando no bra�o:
	java -Djava.library.path=.:/home/pi/opencv/build/lib -Dfile.encoding=Cp1252  -jar argos.jar

 */

public class Argos extends ClassLoader {
	
	//PropSingleton prop; 
	private CommBase  commInterface;
	@SuppressWarnings("unused")
	private CVBase    cvBase;
	
	private static final Logger        LOGGER = Logger.getLogger(Argos.class.getName());
	private static final PropSingleton PROP =   PropSingleton.INSTANCE;
	static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }
	
	// Start up
	private Argos() {
		//System.out.println("java.library.path= " +System.getProperty("java.library.path")); 
		PROP.setRunning(true);
		
		// 1) Start the log subsystem
		logStart();

		// 2) Start the Communication subsystem (MQtt or dummy)
		commInterface = CommBase.getInstance(null);
		
		 // 3) Load the OpenCV class and the OpenCV subsystem 
		 if(MyLoadClass(PROP.getProp("opencv.class")) == false) {
				// Error! use original class instead
				if(MyLoadClass(PROP.getProp("opencv.class.original")) == false) {
					LOGGER.severe("Could not load the default OpenCV class, at all!");
					System.exit(-1);
				}
		 }
		 cvBase = new CVBase();  // OpenCV processing thread
	}
		
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
			
			LOGGER.log(Level.ALL, "Finer logged");
			
		}catch(IOException exception){
			LOGGER.log(Level.SEVERE, "Error occur in FileHandler.", exception);
		}
		
		LOGGER.finer("Finest example on LOGGER handler completed.");
	}
	
	private boolean compile(String className) {
		boolean isWindows = System.getProperty("os.name")
				  				  .toLowerCase().startsWith("windows");

		// javac -cp .;src;opencv-410.jar PropSingleton.java
		String javac = null; 
		
		if (isWindows) {
			javac = PROP.getProp("javac.windows");
		} else {
			javac = PROP.getProp("javac.linux");
		}
		
		javac += " "; // space before the class name
		javac += className; 
		
		String javacParams[] = javac.split(" ");		
		ProcessBuilder builder = new ProcessBuilder();//.inheritIO();
		builder.command(javacParams);

		// builder.directory(new File(System.getProperty("user.home")));
		
		String result = "";
		try {
			LOGGER.warning("Going to execute : " +javac);
			
			final Process process = builder.start();			
			int   exitCode = process.waitFor();//5, TimeUnit.SECONDS);
			
		    // get compile command output
			InputStream       is = process.getErrorStream();  
		    InputStreamReader isr = new InputStreamReader(is);  
		    BufferedReader    br = new BufferedReader(isr);  
		    
		    StringJoiner sj = new StringJoiner(System.getProperty("line.separator"));	        
			br.lines().iterator().forEachRemaining(sj::add);
	        result = sj.toString();
	        
	        if(exitCode != 0) {
				commInterface.publish("/error", "{ \"compiled\": \"" +javac +"\", \n"
									 +"\"msg\": \"Compile failed due to : <" +result +">\" }");
				
			} else {
				PROP.setNewClassReceived(true);
				PROP.setClassName(className.substring(0, className.lastIndexOf('.'))); // class without '.java'
			}
	        
			return true;
	        
		} catch (IOException | InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		}
	}
	
	// Reflection: load a class in real-time
	@SuppressWarnings("unchecked")
	private boolean MyLoadClass(String className)  {
		
        try {
        	// Get class bytes from file
            //String url = "file:C:/Users/ekledob/workspace_neon/InatelTCC/MyClass.class";
        	//String url = "file:MyClass.class";
        	String                url = "file:" +className +".class";
            URL                   myUrl = new URL(url);
            URLConnection         connection = myUrl.openConnection();
            InputStream           input = connection.getInputStream();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int                   data = input.read();

            while(data != -1){
                buffer.write(data);
                data = input.read();
            }

            input.close();

            byte[] classData = buffer.toByteArray();

            // Hint: for loading 'MyClass.java' you should use 'MyClass' without any extension
            Class loadedMyClass =  defineClass(className, classData, 0, classData.length);
			LOGGER.warning("Loaded class name: " + loadedMyClass.getName());
	        
	        // Create a new instance from the loaded class
	        Constructor constructor = loadedMyClass.getConstructor();
			PROP.dynamicObject = constructor.newInstance();			
			
	        // Getting the target method from the loaded class and invoke it using its name
			Class[] argTypes = { Mat.class, Mat.class }; // You need, the arguments
			PROP.dynamicMethodProcess = loadedMyClass.getMethod("process", argTypes);
			
	        //System.out.println("Invoked method name: " + dynamicMethod.getName());
			//PROP.dynamicMethod.invoke(dynamicObject);
			
			commInterface.publish("/status", "{ \"msg\":\"Loaded successfully OpenCV class [" +className +"] \"}");
			return true;
			
        } catch (LinkageError | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | IOException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			commInterface.publish("/error", "{\"error\":\" in MyLoadClass<" +className +">\n" 
								  +e.getClass().getName()
								  + " <" +e.getMessage() +"> \" }");

			return false;
        } 
    }
		
	private boolean run() {

		while (PROP.shouldKeepRunning() && commInterface.isConnected() ) {
			try {
				// Event: received a new class for openCV and...
				if(PROP.isNewClassReceived()) {
					// 1- Try to compile the file
					if(compile(PROP.getClassName() +".java") == false) {
						LOGGER.severe("Could not compile [" +PROP.getClassName() +"]");
					}
					
					// 2- Try to load it
					else if(MyLoadClass(PROP.getClassName()) == false) {
						// Error! recover old one
						MyLoadClass(PROP.getProp("opencv.class.original")); 
					}
					PROP.setNewClassReceived(false);
				}
				
				// Event: a new snapshot was created and must be delivered
				if(PROP.getPictureName() != null) {
		            // Saving snapshot to disk
					File outputfile = new File(PROP.getPictureName());
		            ImageIO.write(PROP.bufferedImage, "jpg", outputfile);

					// Extract the image bytes
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					ImageIO.write(PROP.bufferedImage, "jpg", baos );
					baos.flush();
					byte[] imageInByte = baos.toByteArray();
					baos.close();
					
					//Convert byte[] to String
					String imageInString64 = Base64.getEncoder().encodeToString(imageInByte);
 
					//Publish in string format
					commInterface.publish("/picture/" +PROP.getPictureName(), imageInString64);
					
					PROP.setPictureName(null); // Reset
					PROP.bufferedImage = null;
				}
				
				Thread.sleep(50); //commInterface.getPublishInterval()) ;
				
				
			} catch (InterruptedException |  IllegalArgumentException |  IOException e) {
				e.printStackTrace();

				System.out.println(e.getClass().getName());
				commInterface.publish("/error", e.getClass().getName());
			}
		} 
		
		commInterface.disconnect();
		boolean mustRestart = PROP.mustRestart();
		LogManager.getLogManager().reset();
		
		LOGGER.severe("Exit main loop due to: " +(PROP.shouldKeepRunning() ? "is not connected anymore" : " decided o die" ));
		return mustRestart;
	}
	
	 public static void main(String[] args) {
		 boolean shouldRestart = false;
		 
		 boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
		 boolean isDisplaySet = (System.getenv("DISPLAY") != null);

		 System.out.println("SO= " +System.getProperty("os.name"));
		 System.out.println("DISPLAY= " +System.getenv("DISPLAY"));
		 
		 // In Linux we may not have a GUI (depending on DISPLAY variable)
		 if(isDisplaySet || isWindows) 
			 PROP.setGUIMode(true);
		 else
			 PROP.setGUIMode(false);
		 
		 // Start GUI client if CAPS_LOCK is on
		 if(isWindows && PROP.getLockingKeyState(KeyEvent.VK_NUM_LOCK)) {
			 PROP.setGUIMode(true);
			 System.out.println("NumLock=" +PROP.getLockingKeyState(KeyEvent.VK_NUM_LOCK));
			 new ArgosGUI().run();
			 
		 } else { // Else, start normal Server			 
			 do 
			 {
				 Argos argos = new Argos();			 
				 shouldRestart = argos.run();
				 argos = null;
				 
			 } while(shouldRestart);
		 }
		 	 
		 System.exit(0);
	 }
}

/*  Example of OpenCV dummy class for MQtt:
 * 
 *  
    topic: Win32/command/class

    public class MyClass4 {
	
		public void execute() {
			System.out.println("Hello world from the loaded class4 !!!");
		}
	}

*/
