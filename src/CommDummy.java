import java.util.logging.Logger;

public class CommDummy extends CommBase {

	private String clientId;
	
	private final static Logger LOGGER = Logger.getLogger("CommDummy");
	private final static PropSingleton PROP = PropSingleton.INSTANCE; 
	
	public CommDummy(String myClientId) {
		super(myClientId);
		
		//System.out.println("CommDummy constructor");
		
		if(myClientId.isEmpty())
			clientId    = PROP.getProp("mqtt.client.id");
		else 
			clientId    = myClientId;
	}
	
	@Override
	public void publish(String topic, String message) {
		System.out.println("Dummy pusblish[" +clientId +topic +", " +message +"] ");
		LOGGER.warning("Dummy pusblish[" +clientId +topic +", " +message +"] ");
		try {
			Thread.sleep(1000) ;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean isConnected() {
		return true;
	}

	@Override
	public long getPublishInterval() {
		return 0;
	}
}
