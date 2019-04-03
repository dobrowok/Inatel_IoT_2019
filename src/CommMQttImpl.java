/* Baixar o .jar daqui:
 * https://www.eclipse.org/downloads/download.php?file=/paho/releases/1.1.0/Java/plugins/org.eclipse.paho.client.mqttv3_1.1.0.jar
 * Based on : https://www.eclipse.org/paho/clients/java/
 * https://www.hackster.io/mariocannistra/python-and-paho-for-mqtt-with-aws-iot-921e41
 * 
 * https://www.programcreek.com/java-api-examples/?api=org.eclipse.paho.client.mqttv3.MqttMessage
 */


import java.io.FileOutputStream;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


public class CommMQttImpl extends CommBase implements MqttCallback 
{
	// MQtt stuff
	private String mqttType;
	private int    QoS= 0;
	private String broker;
	private String subscribeTo;
	private String capath;
	private String certpath;
	private String keypath;
	private MqttAsyncClient sampleClient;
	private MemoryPersistence persistence = new MemoryPersistence();
	private String tmpDir;
	private final static Logger LOGGER = Logger.getLogger("CommMQttImpl");
	
	private final static PropSingleton PROP = PropSingleton.INSTANCE; 

	public CommMQttImpl() {
		tmpDir = System.getProperty("java.io.tmpdir"); // Temp dir
		LOGGER.finest("java.io.tmpdir= " +tmpDir);
		
		
		mqttType    = PROP.getProp("mqtt.type");
		clientId    = PROP.getProp("client.id");
		subscribeTo = PROP.getProp("mqtt.subscribe.to");

		if(mqttType.equals("aws")) {
			broker	 = PROP.getProp("mqtt.aws.broker");
			capath   = PROP.getProp("mqtt.aws.capath");
			certpath = PROP.getProp("mqtt.aws.certpath");
			keypath  = PROP.getProp("mqtt.aws.keypath");
		}
		else if (mqttType.equals("mosquitto")) {
			broker	 = PROP.getProp("mqtt.mosq.broker");
		}
				
		connect();		
	}
	
	private void connect() {
		try {
			sampleClient = new MqttAsyncClient(broker, clientId, persistence);
			sampleClient.setCallback(this);
			
	        MqttConnectOptions options = new MqttConnectOptions();
	        options.setCleanSession(true);
	        //options.setConnectionTimeout(10);
	        options.setKeepAliveInterval(60);
	        //options.setAutomaticReconnect(true);
	        
	       // options.setSocketFactory(SslUtil.getSocketFactory("caFilePath", "clientCrtFilePath", "clientKeyFilePath", "password"));
	        
	        LOGGER.warning("Connecting to broker [" +broker +"]");	        
	        sampleClient.connect(options).waitForCompletion();

	        String content = "" +ZonedDateTime.now();
	        publish(clientId +"/Status", "Connected...[" +content +"]");
	        
	        LOGGER.warning("subscribing to [" +subscribeTo +"]");
	        sampleClient.subscribe(subscribeTo, QoS).waitForCompletion();

		} catch(MqttException me) {
	    	
	        System.out.println("reason " +me.getReasonCode());
	        System.out.println("msg "    +me.getMessage());
	        System.out.println("loc "    +me.getLocalizedMessage());
	        System.out.println("cause "  +me.getCause());
	        System.out.println("excep "  +me);
	        me.printStackTrace();
	        LOGGER.severe("Error connecting to [" +broker +"]");
	        PROP.setRunning(false);
	    }		
	}

	@Override
	public boolean isConnected() {
		return sampleClient.isConnected();
	}
	
	@Override
	public void disconnect() {
		if(sampleClient.isConnected()) {
			try {
				publish(clientId +"/Status", "Disconnecting..." +ZonedDateTime.now());
				sampleClient.disconnect();
				
			} catch(MqttException me) {
	            System.out.println("reason "+me.getReasonCode());
	            System.out.println("msg "+me.getMessage());
	            System.out.println("loc "+me.getLocalizedMessage());
	            System.out.println("cause "+me.getCause());
	            System.out.println("excep "+me);
	            me.printStackTrace();
	        }
		}
		
		LOGGER.info("MQtt disconnected");
	}

	@Override
	public void publish(String topic, String message) {
		
		if(!sampleClient.isConnected()) {
			LOGGER.warning("Cannot 'publish': MQtt is disconnected");
			return;
		}
		
        MqttMessage mqTTmessage = new MqttMessage(message.getBytes());
        mqTTmessage.setQos(QoS);
        try {
			sampleClient.publish(topic, mqTTmessage);
			
		} catch (MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
		}
        
        if(topic.toLowerCase().contains("error")) {
        	LOGGER.severe(topic +": " +message);
        	
        } else {
        	LOGGER.warning("pusblish[" +topic +", " +message +"] ");
        }        
	}

	@Override
	public void process() {
		LOGGER.warning("process[]");
	}
	
	@Override
	public void connectionLost(Throwable arg0) {
		LOGGER.warning("connectionLost[]");
	}
	
	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		LOGGER.finer("deliveryComplete[]");
	}
	
	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		LOGGER.warning("messageArrived["+topic +"] =>" +message.toString());
		
		if(topic.toLowerCase().contains("status") || topic.toLowerCase().contains("error") ) {
			return;
		
		} else if(topic.toLowerCase().contains("exit")) {
			publish(clientId +"/Status", "received 'exit' command");
			PROP.setRunning(false);
			
		} else if(topic.toLowerCase().contains("restart")) {
			publish(clientId +"/Status", "received 'restart' command");
			PROP.setMustRestart(true);
			PROP.setRunning(false);
		
		// Change a configuration to file and restart
		} else if(topic.toLowerCase().contains("write")) {
			String temp =  message.toString();
			int Equals = temp.indexOf('=');
			
			if(Equals>0) {
				String parts[] = temp.split("=");

				PROP.setProp(parts[0], parts[1]);

				publish(clientId +"/Status", "write parameter [" +message.toString() +"]");
				PROP.setMustRestart(true);
				
			} else {
				publish(clientId +"/Status", "Error on writing parameter [" +message.toString() +"]");
			}

		} else if(topic.toLowerCase().contains("class")) {
			String filename = null;
			
			// 1- Extract the class name			
			int Bracket = message.toString().indexOf('{');
			if(Bracket>0) {
				String parts[] = message.toString().substring(0, Bracket)
										.split(" ");
				filename = parts[parts.length-1]; // Get last name before the '{'
				PROP.setClassName(filename);
				filename += ".java";
				
				// 2- Save to the file
				try (FileOutputStream fos = new FileOutputStream(filename)) {
				    fos.write(message.getPayload());
					LOGGER.info("Saving to: " +System.getProperty("user.dir") +"/" +filename );
					publish(clientId +"/Status", "received new class [" +filename +"]");
					PROP.setNewClassReceived(true);
					
				} catch (IOException ioe) {
				    ioe.printStackTrace();
				    publish(clientId +"/error", "Saving class failed due to: [" +ioe.getMessage() +"]");
				}
				
			}else {
				publish(clientId +"/error", "Class received, but incomplete (cannot found a '{' inside message body)");
			}
			
	

			// kkk JsonNode payload = fromString(new String(message.getPayload(), StandardCharsets.UTF_8));
			
		}
	}
}
