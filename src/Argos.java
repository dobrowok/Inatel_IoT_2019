import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;

// http://tutorials.jenkov.com/java/fields.html
// https://stackify.com/java-logging-best-practices/
//  

public class Argos extends ClassLoader {
	
	//PropSingleton prop; 
	private String 		   clientId;
	private CommBase       commInterface;
	private ArgosCV        argosCV;
	private ArgosSensor    argosSensor;
	private Method 		   dynamicMethod = null;
	private Object 		   dynamicObject = null; 
	
	private static final Logger LOGGER = Logger.getLogger(Argos.class.getName());
	private static final PropSingleton PROP = PropSingleton.INSTANCE;
	
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
	
	
	public Argos() {
		LOGGER.finest("constructor stub");
		
		logStart();
		
		clientId = PROP.getProp("client.id");
		
		// Path currentRelativePath = Paths.get("");
		//String s = currentRelativePath.toAbsolutePath().toString();
		// System.out.println("Current relative path is: " + s);

		
		
		 if(PROP.getProp("mqtt.type").equals("aws") || PROP.getProp("mqtt.type").equals("mosquitto") )
			 commInterface = new CommMQttImpl();
		 else {
			 System.out.println("Error! No Comm type allowed!");
			 System.exit(-1);
		 }
		
	 
		 MyLoadClass(PROP.getProp("opencv.class"));
	}
	
	public void MyLoadClass(String className)  {
       // if(!"reflection.MyObject".equals(name))
       //         return super.loadClass(name);

        try {
        	// Get class bytes from file
            //String url = "file:C:/Users/ekledob/workspace_neon/InatelTCC/MyClass.class";
        	//String url = "file:MyClass.class";
        	String url = "file:" +className +".class";
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

            Class loadedMyClass =  defineClass(className, classData, 0, classData.length);

			LOGGER.warning("Loaded class name: " + loadedMyClass.getName());
	        
	        // Create a new instance from the loaded class
	        Constructor constructor = null;
			constructor = loadedMyClass.getConstructor();
			dynamicObject = constructor.newInstance();
			
	        // Getting the target method from the loaded class and invoke it using its name
			dynamicMethod = loadedMyClass.getMethod("sayHello");
			
	        System.out.println("Invoked method name: " + dynamicMethod.getName());
			dynamicMethod.invoke(dynamicObject);
			
        } catch (NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
        } catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
	        
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
		
	public void run() {

		while (PROP.isRunning()) {
			try {
				// Received a new class for openCV
				if(PROP.isNewClassReceived()) {
					
					PROP.setProp("opencv.class", PROP.getClassName());
					MyLoadClass(PROP.getProp("opencv.class"));
					PROP.setNewClassReceived(false);
				}
				
				if(dynamicMethod != null) {
					// Getting the target method from the loaded class and invoke it using its name
			        System.out.println("Invoked method name: " + dynamicMethod.getName());
			        dynamicMethod.invoke(dynamicObject);
				}
				
				Thread.sleep(5000) ;
				
				
			} catch (InterruptedException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
				// to do
			}
		} 
		
		commInterface.disconnect();
		LOGGER.severe("Exit main loop");
	}

	 public static void main(String[] args) {
		 Argos argos = new Argos();
		 
		 argos.run();
	 }
}

/* 
topic: Win32/class

// Example Class for MQtt 
public class MyClass4 {
	
	public void sayHello() {
		System.out.println("Hello world from the loaded class4 !!!");
	}

}

*/
