// https://www.journaldev.com/2324/jackson-json-java-parser-api-example-tutorial

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

public abstract class CommBase {
	private final static PropSingleton PROP = PropSingleton.INSTANCE;
	private final static Logger LOGGER = Logger.getLogger("CommMQttImpl");
	
	protected long publishInterval = 15000;
	public static  String clientId;
	private static CommBase instance;
	String topics[] = null;
	
	public static CommBase getInstance(String myClientId) {
		if(instance==null) {
			
			switch(PROP.getProp("mqtt.type")) {
				case "mosquitto": 
				 	instance = new CommMQtt(myClientId);		break;
				 
			 	case "aws":
			 		instance = new CommAWS(myClientId);		break;
			 
			 	case "dummy":
			 		instance = new CommDummy("bla");	break;
					
				default:
					 System.out.println("Error! No Comm type allowed!");
					 System.exit(-1);
			}
			
			// Send a alive message
	        // JSon with system info
			/* old way
	        String now = new Timestamp(System.currentTimeMillis()).toString();
	        String js = "{  \"clienId\":\"" +clientId +"\", "
	        		     + "\"connected\":\"" +now +"\", \"SO\":" +System.getProperty("os.name") 
	        			 + "\", \"DISPLAY\":\"" +System.getenv("DISPLAY") +"\" }";
	        
	        instance.publish("/status", js);
	        */
	        
	        OutputStream bout= new ByteArrayOutputStream();
			JsonGenerator gen;
			try {
				gen = new JsonFactory().createGenerator(new OutputStreamWriter(bout, "UTF-8"));
				
				//for pretty printing
				gen.setPrettyPrinter(new DefaultPrettyPrinter());
				gen.writeStartObject(); // start root object

				gen.writeStringField("clientId",  clientId);
				gen.writeStringField("connected", new Timestamp(System.currentTimeMillis()).toString());
				gen.writeStringField("SO", 		  System.getProperty("os.name"));
				gen.writeStringField("DISPLAY",   System.getenv("DISPLAY"));
				//gen.writeNumberField("um numero", 777);
				//gen.writeBooleanField("permanent", true);

		        //gen.writeEndObject(); //closing properties
				gen.writeEndObject(); //closing root object
		        gen.flush();
		        gen.close();
		        
		        instance.publish("/status", bout.toString());		        

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
		
		return instance;
	}
	
	public CommBase (String myClientId) {
		System.out.println("construtor do CommBase");
		
		if(myClientId==null)
			clientId    = PROP.getProp("mqtt.client.id");
		else 
			clientId    = myClientId;
		
		try {
			publishInterval =  Integer.parseInt(PROP.getProp("mqtt.publish.interval.ms"));
			
		} catch (NumberFormatException e) {
			publishInterval =  15000;
			System.out.println("Error! Default publishInterval= " +publishInterval);
		}
		
		topics = PROP.getProp("mqtt.subscribe").split(";");
		if(topics[0]=="")
			topics[0]= "#";
		
		//for (String s: topics) {
		for (int i=0; i<topics.length; i++)
        {
		    //Do your stuff here
		    System.out.println("topics[" +i +"]= " +topics[i] );
        }
	}    
	public abstract void    publish(String topic, String message);
	public abstract void    disconnect();
	public abstract boolean isConnected();
	
	public long getPublishInterval() {
		return publishInterval;
	};
	
	protected void proccess (String topic, String message) {

		if(topic.toLowerCase().contains(clientId.toLowerCase()))
			return; // This is a message from myself! Just ignore it
		
		else if(!topic.toLowerCase().contains("picture"))
			LOGGER.warning("messageArrived["+topic +"] =>" +message);
		
		else
			LOGGER.warning("messageArrived["+topic +"] => [byte array]"); 
		
		// These messages types comes from other Argos instances running, so should not be processed at all
		if(    topic.toLowerCase().contains("status") || topic.toLowerCase().contains("error") 
			|| topic.toLowerCase().contains("image") ) {
			return;
		
		} else if(topic.toLowerCase().contains("exit")) {
			publish("/status", "{ \"received\": \"exit command\" }");
			PROP.setMustRestart(false);
			PROP.setRunning(false);
			
		} else if(topic.toLowerCase().contains("restart")) {
			publish("/status", "{ \"received\": \"restart command\" }");
			PROP.setMustRestart(true);
			PROP.setRunning(false);
			
		// Receive a command to provide a snapshot
		} else if(topic.toLowerCase().contains("snapshot")) {
			PROP.setSnap(true);

		// Received an answer to a previous snapshot cmd, that means, a .jpeg file
		// Normal mode is ignoring it: unless we be in  GUI mode.
		} else if(topic.toLowerCase().contains("picture") && PROP.isGUIMode()) {
			
			byte[] imageInByte = Base64.getDecoder().decode(message);
			InputStream in = new ByteArrayInputStream(imageInByte);
			BufferedImage bImageFromConvert = null;
			
			try {
				bImageFromConvert = ImageIO.read(in);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			PROP.bufferedImage = bImageFromConvert; 
			
			// Parsing name from topic
			String name[] = topic.split("/");
			PROP.setPictureName(name[name.length-1]);
			

		} else if(topic.toLowerCase().contains("read")) {
			publish("/status", "{ \"received\": \"read\", \"" +message +"\":"
					+PROP.getProp(message) +"\" }");
		
		// Change a configuration to properties file and restart
		} else if(topic.toLowerCase().contains("write")) {
			String temp =  message;
			int Equals = temp.indexOf('=');
			
			if(Equals>0) {
				String parts[] = temp.split("=");

				PROP.setProp(parts[0], parts[1]);

				publish("/status", "{ \"write parameter\": \"" +message +"\" }");
				PROP.setMustRestart(true);
				
			} else {
				publish("/error", "{ \"error on writing parameter\": \"" +message +"\" }");
			}

		// CLASS: received a new class to be compiled
		} else if(topic.toLowerCase().contains("class")) {
			String filename = null;
			
			// 1- Extract the class name			
			int Bracket = message.indexOf('{');
			if(Bracket>=0) {
				String parts[] = message.substring(0, Bracket)
										.split(" ");
				
				if(parts.length<=1) {
					publish("/error", "{ \"error\": \"Class received, but incomplete (cannot found a 'space' inside message body) \" }");
					return;
				}
				
				// To do: better way to identify class name in the string!
				filename = parts[parts.length-1]; // Get last name before the '{'
				
				PROP.setClassName(filename);
				filename += ".java";
				
				// 2- Save to the file
				try (FileOutputStream fos = new FileOutputStream(filename)) {
				    fos.write(message.getBytes()); //kkk ??? .getPayload());
					LOGGER.info("Saving to: " +System.getProperty("user.dir") +"/" +filename );
					publish("/status", "{ \"received new class\": \"" +filename +"\" }");
					PROP.setNewClassReceived(true);
					
				} catch (IOException ioe) {
				    ioe.printStackTrace();
				    publish("/error", "{ \"error Saving class\": \"failed due to: [" +ioe.getMessage() +"]\" }");
				}
				
			}else {
				publish("/error", "{ \"error\": \"Class received, but incomplete (cannot found a '{' inside message body) \" }");
			}
			// kkk JsonNode payload = fromString(new String(message.getPayload(), StandardCharsets.UTF_8));
		}
	}
}
