
public abstract class CommBase {
	
	protected String clientId;
	//public void process() throws IOException {
		// TODO Auto-generated method stub
	//	}
	public CommBase () {
		System.out.println("CommBase");
	}    
	public abstract void    publish(String topic, String message);
    public abstract void    process();
	public abstract void    disconnect();
	public abstract boolean isConnected();
}
