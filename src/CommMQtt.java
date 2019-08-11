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
 * 
 * https://howtodoinjava.com/array/convert-byte-array-string-vice-versa/
 */


import java.sql.Timestamp;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


public class CommMQtt extends CommBase implements MqttCallback 
{
	// MQtt stuff
	public  int    QoS= 0;
	private String broker;
	private MqttAsyncClient sampleClient;
	private MemoryPersistence persistence = new MemoryPersistence();
	private final static Logger LOGGER = Logger.getLogger("CommMQttImpl");
	
	private final static PropSingleton PROP = PropSingleton.INSTANCE; 
	
	public CommMQtt(String myClientId) {
		
		super(myClientId);
		
		String tmpDir = System.getProperty("java.io.tmpdi"); // Temp dir
		LOGGER.finest("java.io.tmpdir= " +tmpDir);
		
		
		if(myClientId==null)
			clientId    = PROP.getProp("mqtt.client.id");
		else 
			clientId    = myClientId;
		
		broker = PROP.getProp("mqtt.mosq.broker");
				
		connect();		
	}
	
	private void connect() {
		// We can have more than one address configured
		String brokers[] = broker.split(";");
		
		try {
			sampleClient = new MqttAsyncClient(brokers[0], clientId, persistence);
			sampleClient.setCallback(this);
			
	        MqttConnectOptions options = new MqttConnectOptions();
	        options.setCleanSession(true);
	        options.setConnectionTimeout(2);
	        options.setKeepAliveInterval(10);
	        //options.setAutomaticReconnect(true);
	        options.setServerURIs(brokers);
	        
	        LOGGER.warning("Connecting to broker [" +broker +"]");	        
	        sampleClient.connect(options).waitForCompletion();
	        System.out.println("Connected to: [" +sampleClient.getCurrentServerURI() +"]");

        	for (String s: topics) {
                LOGGER.warning("subscribing to [" +s +"]");
    			sampleClient.subscribe(s, QoS).waitForCompletion();
        	}

        	//String now = new Timestamp(System.currentTimeMillis()).toString();
	        PROP.setProp("mqtt.startup.date", new Timestamp(System.currentTimeMillis()).toString() );

		} catch(MqttException  me) {
	    	
	        System.out.println("reason " +me.getReasonCode());
	        System.out.println("msg "    +me.getMessage());
	        System.out.println("loc "    +me.getLocalizedMessage());
	        System.out.println("cause "  +me.getCause());
	        System.out.println("excep "  +me);
	        me.printStackTrace();
	        LOGGER.severe("Error connecting to [" +broker +"]");
	        System.exit(-1);
	    }
	}

	@Override
	public boolean isConnected() {
		return sampleClient.isConnected();
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
        // Log
        super.publish(topic, message);
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
	public void messageArrived(String topic, MqttMessage message) {
		proccess(topic, message.toString());
	}
	
}
