import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.opencv.core.Mat;

// https://www.devmedia.com.br/utilizando-arquivos-de-propriedades-no-java/25546
// https://dzone.com/articles/java-singletons-using-enum
	
public enum PropSingleton {
    
    INSTANCE("Initial class info"); 
	static final String FILENAME = "argos.properties";
	private static final Logger LOGGER = Logger.getLogger(PropSingleton.class.getName());
	
    private Properties props;
    private String  className;
    private String  pictureName =       null;
    private boolean running =			true;
    private boolean newClassReceived =	false;
    private boolean mustRestart =		false;
	private boolean doSnap =			false;
	private boolean guiMode =			false;
	private Toolkit kit =               Toolkit.getDefaultToolkit();
    public  BufferedImage bufferedImage =        null;
    public  Object        dynamicObject =        null;
	public  Method        dynamicMethodProcess = null;

	public boolean dynamicMethodProcess(Mat frame, Mat gray) {
		boolean targetDetected = false;
		
		try {
			targetDetected = (boolean)dynamicMethodProcess.invoke(dynamicObject, frame, gray);
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvocationTargetException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		return targetDetected;
	}
  
    private PropSingleton(String info) {
        // Get the properties file, or create it if not exist
        props = new Properties();
        FileInputStream file;
        
        // Try 3 times before giving up
        int numTries = 3;        
        
        while (true) {
    		try {
        	    // Create a file if it don�t exist
    			new FileOutputStream(FILENAME, true).close();
    			
    			file = new FileInputStream("argos.properties");
    			props.load(file);
    			System.out.println("prop.server.login= " +props.getProperty("prop.server.login"));
    			mustRestart = false;
    			
        	    break;
        	    
    		} catch (Exception e ) {
    	    	if (--numTries == 0) {
    	    		System.out.println("Could not create properties file at all!");
    	    		System.exit(-1);    			
    			} 
    		}
    	}
    }
  
    public PropSingleton getInstance() {
        return INSTANCE;
    }
    
    // Get SCROLL_LOCK, NUM_LOCK, etc status (to turn on or off, some features, in real-time)
    public boolean getLockingKeyState(int keyCode) {
    	// Only works for Windows!!!
    	 if(System.getProperty("os.name").toLowerCase().startsWith("windows"))
    		 return kit.getLockingKeyState(keyCode);
    	 else
    		 return false;    	
    }
    
    // getters and setters
	public String getProp(String key) {
		if(null == props.getProperty(key)) {
			System.out.println("[" +key +"] Is empty!!!");
			LOGGER.severe("[" +key +"] Is empty!!!");
			return "";  // Return an empty string, to not cause damage
			
		} else {
			return props.getProperty(key);
		}
    }

	public void setProp(String key, String value) {
		props.setProperty(key, value);
		
        try {			
			Properties tmp = new Properties() {
				private static final long serialVersionUID = 1L;

				@Override
			    public synchronized Enumeration<Object> keys() {
			        return Collections.enumeration(new TreeSet<Object>(super.keySet()));
			    }
			};
			tmp.putAll(props);
			tmp.store(new FileWriter(FILENAME), null);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

	public boolean shouldKeepRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public boolean isNewClassReceived() {
		return newClassReceived;
	}

	public void setNewClassReceived(boolean newClassReceived) {
		this.newClassReceived = newClassReceived;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public boolean mustRestart() {
		return mustRestart;
	}

	public void setMustRestart(boolean mustRestart) {
		this.mustRestart = mustRestart;
	}

	public boolean mustSnap() {
		return doSnap;
	}
	
	public void setSnap(boolean b) {
		this.doSnap = b;
	}
	
	public String getPictureName() {
		return pictureName;
	}

	public void setPictureName(String s) {
		this.pictureName = s;
	}
	
	public boolean isGUIMode() {
		return guiMode;
	}
	
	public void setGUIMode(boolean b) {
		this.guiMode = b;
	}

	public void setProp(String key, long i) {
		setProp(key, Long.toString(i));
	}	
}
