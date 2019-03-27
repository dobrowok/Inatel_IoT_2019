import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class CommBase {
	
	protected String clientId;

	private final static Logger LOGGER = Logger.getLogger("CommMQttImpl");
	private final static PropSingleton PROP = PropSingleton.INSTANCE; 
	
	//public void process() throws IOException {
		// TODO Auto-generated method stub
	//	}
	public CommBase () {
		System.out.println("CommBase");
	}    
	public abstract void publish(String topic, String message);
    public abstract void process();
	public abstract void disconnect();

	protected void compile(String className) {
		boolean isWindows = System.getProperty("os.name")
				  				   .toLowerCase().startsWith("windows");
		// javac -cp . PropSingleton.java
		String javac = "javac -cp . " +className;
		String javacParams[] = javac.split(" ");
		
		if (isWindows) {	    
			// blabla
		} else {
		    // blabla
		}
		
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
				publish(clientId +"/Error", "Compile failed due to : [" +result +"]");
				
			} else {
				PROP.setNewClassReceived(true);
				PROP.setClassName(className.substring(0, className.lastIndexOf('.'))); // class without '.java'
			}
				
	        
		} catch (IOException | InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
