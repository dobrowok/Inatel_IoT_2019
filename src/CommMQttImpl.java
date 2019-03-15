/* Baixar o .jar daqui:
 * https://www.eclipse.org/downloads/download.php?file=/paho/releases/1.1.0/Java/plugins/org.eclipse.paho.client.mqttv3_1.1.0.jar
 * Based on : https://www.eclipse.org/paho/clients/java/
 * https://www.hackster.io/mariocannistra/python-and-paho-for-mqtt-with-aws-iot-921e41
 */


import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


public class CommMQttImpl extends ArgosCommBase implements MqttCallback 
{
	private PropSingleton prop; 

	// MQtt stuff
	private String mqttType;
	private String topic;
	private int qos= 2;
	private String broker;
	private String content  = "Starting up: ";
	private String clientId;
	private String capath;
	private String certpath;
	private String keypath;
	private int publishInterval;
	private MqttClient sampleClient;
	private MemoryPersistence persistence = new MemoryPersistence();
	private String tmpDir;
	private final static Logger LOGGER = Logger.getLogger("CommMQttImpl");

	public CommMQttImpl() {
		tmpDir = System.getProperty("java.io.tmpdir"); // Temp dir
		LOGGER.info("java.io.tmpdir= " +tmpDir);
		
		prop = PropSingleton.INSTANCE;
		
		mqttType = prop.getProp("mqtt.type");
		topic 	 = prop.getProp("mqtt.base_topic");
		clientId = prop.getProp("mqtt.clientid");
		
		publishInterval = Integer.parseInt(prop.getProp("mqtt.publish.interval"));
		
		
		if(mqttType.equals("aws")) {
			broker	 = prop.getProp("mqtt.aws.broker");
			capath   = prop.getProp("mqtt.aws.capath");
			certpath = prop.getProp("mqtt.aws.certpath");
			keypath  = prop.getProp("mqtt.aws.keypath");
		}
		else if (mqttType.equals("mosquitto")) {
			broker	 = prop.getProp("mqtt.mosq.broker");
		}
				
		
		try {
			sampleClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            System.out.println("Connecting to broker: "+broker);
            sampleClient.connect(connOpts);
            System.out.println("Connected");
            
            content += ZonedDateTime.now();
            System.out.println("Publishing message: "+content);
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(qos);
            sampleClient.publish(topic, message);
            System.out.println("Message published");
            
            sampleClient.subscribe(topic);
            
            
        } catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }
		
		//System.out.println("MQtt (" +broker +":"+port +"=> ok");
		
	}
	public void disconnect() {
		
		try {
			sampleClient.disconnect();
			LOGGER.info("Disconnected");
			
		} catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }
		
    	System.out.println("Disconnected");
	}

	public void publish(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void process() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void connectionLost(Throwable arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
