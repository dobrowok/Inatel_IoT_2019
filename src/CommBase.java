
public abstract class CommBase {
	
	public  String topicRec; 
	
	public CommBase () {
		//System.out.println("construtor do CommBase");
	}    
	public abstract void    publish(String topic, String message);
    public abstract void    process();
	public abstract void    disconnect();
	public abstract boolean isConnected();
	public abstract long    getPublishInterval();
}
