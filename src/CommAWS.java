import java.util.logging.Logger;

import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotMqttClient;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.amazonaws.services.iot.client.AWSIotTopic;
import com.amazonaws.services.iot.client.sample.sampleUtil.SampleUtil;
import com.amazonaws.services.iot.client.sample.sampleUtil.SampleUtil.KeyStorePasswordPair;


public class CommAWS extends CommBase {
	private final static Logger LOGGER = Logger.getLogger("CommAWS");
	private final static PropSingleton PROP = PropSingleton.INSTANCE;
	
	private static AWSIotMqttClient awsIotClient;
	
	public class TopicListener extends AWSIotTopic {
		CommAWS commAWS = null;

	    public TopicListener(String topic, AWSIotQos qos, CommAWS pAWS) {
	        super(topic, qos);
	    	commAWS = pAWS;
	    }

	    @Override
	    public void onMessage(AWSIotMessage message) {
	    	commAWS.proccess(message.getTopic(), message.getStringPayload());
	    }
	}
	
	public CommAWS(String meu) { //(String myClientId) {
		//super(clientEndpoint, clientEndpoint, connection); //myClientId);
		super(null);
		//super(clientEndpoint, clientId, awsAccessKeyId, awsSecretAccessKey, null);
		//super(meu, AWSIotQos.QOS0);
		
		System.out.println("construtor do CommAWS");
		
		initClient();

	    
		try {
			awsIotClient.connect();
			
			for (String s: topics) {
				LOGGER.warning("subscribing to [" +s +"]");
				awsIotClient.subscribe(new TopicListener(s, AWSIotQos.QOS0, this), true);
			}
			
		} catch (AWSIotException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
	private static void initClient() {
		String clientEndpoint = PROP.getProp("mqtt.aws.endpoint");
		String clientId =		PROP.getProp("mqtt.client.id");
		
		String certificateFile = PROP.getProp("mqtt.aws.certpath"); //arguments.get("certificateFile", SampleUtil.getConfig("certificateFile"));
		String privateKeyFile =  PROP.getProp("mqtt.aws.keypath"); //arguments.get("privateKeyFile", SampleUtil.getConfig("privateKeyFile"));
		
		// System.out.println("actual= " +System.getProperty("user.dir")); // current dir
		
		if (awsIotClient == null && certificateFile != null && privateKeyFile != null) {
		    String algorithm = null; // arguments.get("keyAlgorithm", SampleUtil.getConfig("keyAlgorithm"));
		
		    KeyStorePasswordPair pair = SampleUtil.getKeyStorePasswordPair(certificateFile, privateKeyFile, algorithm);
		
		    awsIotClient = new AWSIotMqttClient(clientEndpoint, clientId, pair.keyStore, pair.keyPassword);
		    
		    //AwsIotMqttClientListener xx = new AwsIotMqttClientListener (awsIotClient);
		    //awsIotClient.
		}
		
		if (awsIotClient == null) {
			String awsAccessKeyId = 	PROP.getProp("mqtt.aws.access.key.id"); //arguments.get("awsAccessKeyId", SampleUtil.getConfig("awsAccessKeyId"));
			String awsSecretAccessKey = PROP.getProp("mqtt.aws.secret.access.key"); //arguments.get("awsSecretAccessKey", SampleUtil.getConfig("awsSecretAccessKey"));
			String sessionToken = 		PROP.getProp("mqtt.aws.session.token"); //arguments.get("sessionToken", SampleUtil.getConfig("sessionToken"));
		
		    if (awsAccessKeyId != null && awsSecretAccessKey != null) {
		        awsIotClient = new AWSIotMqttClient(clientEndpoint, clientId, awsAccessKeyId, awsSecretAccessKey,
		                							sessionToken);
		    }
		}
		
		if (awsIotClient == null) {
		    throw new IllegalArgumentException("Failed to construct client due to missing certificate or credentials.");
	    }
	}

	@Override
	public void publish(String topic, String message) {
		
		try {
            awsIotClient.publish(clientId +topic, AWSIotQos.QOS0, message );
            
        } catch (AWSIotException e) {
            System.out.println(System.currentTimeMillis() + ": publish failed for " + message);
            LOGGER.severe("Cannot 'publish': MQtt is disconnected");
        }
		
		if(topic.toLowerCase().contains("error")) {
			LOGGER.severe(topic +": " +message);
			
		} else if (topic.toLowerCase().contains("picture")) {
			LOGGER.warning("pusblish[" +clientId +topic +", [byte array]");
			
		}    else {
			LOGGER.warning("pusblish[" +clientId +topic +", " +message +"] ");
		}		
	}

	@Override
	public void disconnect() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isConnected() {
		// TODO Auto-generated method stub
		return true;
	}
}
