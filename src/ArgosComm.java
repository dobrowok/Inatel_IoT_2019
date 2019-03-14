import java.io.IOException;

/**
 * @author ekledob
 * Based on: http://tutorials.jenkov.com/java/interfaces-vs-abstract-classes.html
 */
public interface ArgosComm {
    //public void process() throws IOException;
    public void publish(String arg0);

	public void process();
}
