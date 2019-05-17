import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringJoiner;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;

// http://tutorials.jenkov.com/java/fields.html
// https://stackify.com/java-logging-best-practices/
// https://dzone.com/articles/programmatically-restart-java
// https://www.alatortsev.com/2018/11/21/installing-opencv-4-0-on-raspberry-pi-3-b/
// https://www.pyimagesearch.com/2018/09/26/install-opencv-4-on-your-raspberry-pi/

// https://answers.opencv.org/question/193924/failed-to-use-systemloadlibrarycorenative_library_name-on-raspberry-pi/

/*
 * Rodando no braço:
	java -Djava.library.path=.:/home/pi/opencv/build/lib -Dfile.encoding=Cp1252  -jar argos.jar

 */

public class Argos extends ClassLoader {
	
	//PropSingleton prop; 
	private CommBase       commInterface;
	private ArgosCV        argosCV;
	private Method 		   dynamicMethod = null;
	private Object 		   dynamicObject = null; 
	
	private static final Logger LOGGER = Logger.getLogger(Argos.class.getName());
	private static final PropSingleton PROP = PropSingleton.INSTANCE;
	
	char []buff = new char[50];
	String temp ;	
	int  buffSize = 50;
	
	// Start up
	public Argos() {
			logStart();

			PROP.setRunning(true);
			
			// Path currentRelativePath = Paths.get("");
			//String s = currentRelativePath.toAbsolutePath().toString();
			// System.out.println("Current relative path is: " + s);
			
			 if(PROP.getProp("mqtt.type").equals("aws") || PROP.getProp("mqtt.type").equals("mosquitto") )
				 commInterface = new CommMQttImpl("bla");
			 
			 else if(PROP.getProp("mqtt.type").toLowerCase().equals("dummy"))
			     commInterface = new CommDummy("bla");
			 
			 else {
				 System.out.println("Error! No Comm type allowed!");
				 System.exit(-1);
			 }
			
			 // Load the OpenCV class 
			 if(MyLoadClass(PROP.getProp("opencv.class")) == false) {
					// Error! recover old 
					MyLoadClass(PROP.getProp("opencv.original.class")); 
			 }
			 
			 argosCV = new ArgosCV();  // OpenCV processing thread
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
		// javac -cp . PropSingleton.java
		String javac = null; 
		
		if (isWindows) {
			javac = PROP.getProp("javac.windows");
		} else {
			javac = PROP.getProp("javac.linux");
		}
		
		javac += className; 
		String javacParams[] = javac.split(" ");
		
		ProcessBuilder builder = new ProcessBuilder();//.inheritIO();
		builder.command(javacParams);

		// builder.directory(new File(System.getProperty("user.home")));
		
		String result = "";
		try {
			LOGGER.info("Going to execute : " +javac);
			
			final Process process = builder.start();			
			int exitCode = process.waitFor();
			
		    // get compile command output
			InputStream is = process.getErrorStream();  
		    InputStreamReader isr = new InputStreamReader(is);  
		    BufferedReader br = new BufferedReader(isr);  
		    
		    StringJoiner sj = new StringJoiner(System.getProperty("line.separator"));	        
			br.lines().iterator().forEachRemaining(sj::add);
	        result = sj.toString();
	        
	        if(exitCode != 0) {
				commInterface.publish("/error", "Compile failed due to : [" +result +"]");
				
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
	public boolean MyLoadClass(String className)  {
		String url = null;
		
        try {
        	// Get class bytes from file
            //String url = "file:C:/Users/ekledob/workspace_neon/InatelTCC/MyClass.class";
        	//String url = "file:MyClass.class";
        	url = "file:" +className +".class";
            URL myUrl = new URL(url);
            URLConnection connection = myUrl.openConnection();
            InputStream input = connection.getInputStream();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int data = input.read();

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
	        Constructor constructor = null;
			constructor = loadedMyClass.getConstructor();
			dynamicObject = constructor.newInstance();
			
	        // Getting the target method from the loaded class and invoke it using its name
			dynamicMethod = loadedMyClass.getMethod("execute");
			
	        //System.out.println("Invoked method name: " + dynamicMethod.getName());
			dynamicMethod.invoke(dynamicObject);
			
			commInterface.publish("/status", "Loaded successfully OpenCV class [" +className +"]");
			return true;
			
        } catch (LinkageError | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | IOException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			commInterface.publish("error in MyLoadClass(" +className +")", e.getClass().getName()  +" [" +e.getMessage() +"]");
			/* To do: criar uma classe default para o OpenCV */
			return false;
        } 
    }
		
	boolean run() {

		while (PROP.shouldKeepRunning() && commInterface.isConnected() ) {
			try {
				// Received a new class for openCV
				if(PROP.isNewClassArrived()) {
					// 1- Try to compile file
					if(compile(PROP.getClassName() +".java") == false) {
						LOGGER.severe("Could not compile [" +PROP.getClassName() +"]");
					}
					
					// 2- Try to load it
					else if(MyLoadClass(PROP.getClassName()) == false) {
						// Error! recover old 
						MyLoadClass(PROP.getProp("opencv.class.original")); 
					}
					PROP.setNewClassReceived(false);
				}
				
				if(PROP.getPictureName() != null) {
					String temp = new String(PROP.imageInByte);
					
					commInterface.publish("/status/" +PROP.getPictureName(), 
										  temp);
					
					PROP.setPictureName(null);
				}
				
				if(dynamicMethod != null) {
					// Getting the target method from the loaded class and invoke it using its name
			        System.out.println("Invoked method name: " + dynamicMethod.getName());
			        dynamicMethod.invoke(dynamicObject);
			        commInterface.publish("/status", "Invoked method name: " + dynamicMethod.getName());
				}
				
				Thread.sleep(commInterface.getPublishInterval()) ;
				
				
			} catch (InterruptedException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();

				dynamicMethod = null;			
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
		 
		 // Start GUI if CAPS_LOCK is on
		 if(PROP.getLockingKeyState(KeyEvent.VK_CAPS_LOCK)) {
			 System.out.println("CAPS=" +PROP.getLockingKeyState(KeyEvent.VK_CAPS_LOCK));
			 
			 new ArgosGUI().run();
		 }
		 else { // Else, start normal Server
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
