

/**
 * @author Kleber Dobrowolski
 * Based on: http://tutorials.jenkov.com/java/interfaces-vs-abstract-classes.html
 */
public interface CommInterface {
    //public void process() throws IOException;
	
    public void publish(String topic, String message);
    public void process();
	public void disconnect();
	public boolean isConnected();
}
