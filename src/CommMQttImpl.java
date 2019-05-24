/* Baixar o .jar daqui:
 * https://www.eclipse.org/downloads/download.php?file=/paho/releases/1.1.0/Java/plugins/org.eclipse.paho.client.mqttv3_1.1.0.jar
 * Based on : https://www.eclipse.org/paho/clients/java/
 * https://www.hackster.io/mariocannistra/python-and-paho-for-mqtt-with-aws-iot-921e41
 * 
 * https://www.programcreek.com/java-api-examples/?api=org.eclipse.paho.client.mqttv3.MqttMessage
 * 
 * The payload for every publish request is limited to 128 KB. The AWS IoT service rejects publish and connect requests larger than this size.
 * https://docs.aws.amazon.com/general/latest/gr/aws_service_limits.html#limits_iot
 * 
 * https://mosquitto.org/man/mosquitto-conf-5.html
 * Max payload
 */


import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
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
	private long   publishInterval = 15000;
	private String broker;
	private String subscribeTo;
	private String clientId;
	private String capath;
	private String certpath;
	private String keypath;
	private MqttAsyncClient sampleClient;
	private MemoryPersistence persistence = new MemoryPersistence();
	private String tmpDir;
	private final static Logger LOGGER = Logger.getLogger("CommMQttImpl");
	
	private final static PropSingleton PROP = PropSingleton.INSTANCE; 
	
	public long getPublishInterval() {
		return publishInterval;
	}

	public CommMQttImpl(String myClientId) {
		tmpDir = System.getProperty("java.io.tmpdir"); // Temp dir
		LOGGER.finest("java.io.tmpdir= " +tmpDir);
		
		
		mqttType    = PROP.getProp("mqtt.type");
		if(myClientId.isEmpty())
			clientId    = PROP.getProp("mqtt.client.id");
		else 
			clientId    = myClientId;
			
		subscribeTo = PROP.getProp("mqtt.subscribe.to");
		try {
			publishInterval =  Integer.parseInt(PROP.getProp("mqtt.publish.interval.ms"));
			
		} catch (NumberFormatException e) {
			publishInterval =  15000;
			System.out.println("Error! Default publishInterval= " +publishInterval);
		}

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

	        String now = new Timestamp(System.currentTimeMillis()).toString();
	        publish("/status", "Connected...[" +now +"]");
	        
	        PROP.setProp("mqtt.startup", now );
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
				publish("/status", "Disconnecting..." +ZonedDateTime.now());
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
			sampleClient.publish(clientId +topic, mqTTmessage);
			
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
        	LOGGER.warning("pusblish[" +clientId +topic +", " +message +"] ");
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
		// Only 
		if(!topic.toLowerCase().contains("snapshot"))
			LOGGER.warning("messageArrived["+topic +"] =>" +message.toString());
		else
			LOGGER.warning("messageArrived["+topic +"] => [byte array]"); 
		
		// These messages types comes from other Argos instances running, so should not be processed
		if(topic.toLowerCase().contains("status") || topic.toLowerCase().contains("error") || topic.toLowerCase().contains("image") ) {
			return;
		
		} else if(topic.toLowerCase().contains("exit")) {
			publish("/status", "received 'exit' command");
			PROP.setRunning(false);
			
		} else if(topic.toLowerCase().contains("restart")) {
			publish("/status", "received 'restart' command");
			PROP.setMustRestart(true);
			PROP.setRunning(false);
			
		} else if(topic.toLowerCase().contains("snapshot")) {
			PROP.setSnap(true);

		} else if(topic.toLowerCase().contains("read")) {
			publish("/status", PROP.getProp(message.toString()));
		
		// Change a configuration to properties file and restart
		} else if(topic.toLowerCase().contains("write")) {
			String temp =  message.toString();
			int Equals = temp.indexOf('=');
			
			if(Equals>0) {
				String parts[] = temp.split("=");

				PROP.setProp(parts[0], parts[1]);

				publish("/status", "write parameter [" +message.toString() +"]");
				PROP.setMustRestart(true);
				
			} else {
				publish("/status", "Error on writing parameter [" +message.toString() +"]");
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
					publish("/status", "received new class [" +filename +"]");
					PROP.setNewClassReceived(true);
					
				} catch (IOException ioe) {
				    ioe.printStackTrace();
				    publish("/error", "Saving class failed due to: [" +ioe.getMessage() +"]");
				}
				
			}else {
				publish("/error", "Class received, but incomplete (cannot found a '{' inside message body)");
			}
			
	

			// kkk JsonNode payload = fromString(new String(message.getPayload(), StandardCharsets.UTF_8));
			
		}
	}
}
