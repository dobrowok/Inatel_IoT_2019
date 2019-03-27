import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

// https://www.devmedia.com.br/utilizando-arquivos-de-propriedades-no-java/25546
// https://dzone.com/articles/java-singletons-using-enum
	
public enum PropSingleton {
    
    INSTANCE("Initial class info"); 
  
    private Properties props;
    private String className;
    private boolean running = true;
    private boolean newClassReceived = false;
    private boolean mustRestart = false;
  
    private PropSingleton(String info) {
        // Get the properties file, or create it if not exist
        props = new Properties();
        FileInputStream file;
        
        // Try 3 times before giving up
        int numTries = 3;        
        
        while (true) {
    		try {
        	    // Create a file if it don´t exist
    			new FileOutputStream("argos.properties", true).close();
    			
    			file = new FileInputStream("argos.properties");
    			props.load(file);
    			System.out.println(props.getProperty("prop.server.login"));
    			
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
     
    // getters and setters
	public String getProp(String key) {
		return props.getProperty(key);
    }

	public void setProp(String key, String value) {
		props.setProperty(key, value);
    }

	public boolean isRunning() {
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
}
