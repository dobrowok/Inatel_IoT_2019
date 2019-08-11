import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

public class MyJson {
	private static MyJson        instance = null;
	private static JsonGenerator gen =  null; 
	private static OutputStream  bout = new ByteArrayOutputStream();
	
	MyJson() {
	}
	
	public static MyJson getInstance() {
		if(instance==null) {
			instance = new MyJson();
		}
		
		return instance;
	}
	
	public String getJson() {
		
		try {
			gen.writeEndObject();
			gen.flush();
			//gen.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} //closing root object
		
		return bout.toString();
	}

	public void writeStringField(String fieldName, String value) {
		try {
			gen.writeStringField(fieldName, value);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	public void writeStartObject() {
		try {
			bout= new ByteArrayOutputStream();			
			gen = new JsonFactory().createGenerator(new OutputStreamWriter(bout, "UTF-8"));
			//for pretty printing
			gen.setPrettyPrinter(new DefaultPrettyPrinter());
			gen.writeStartObject();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
/* Original code

OutputStream bout= new ByteArrayOutputStream();
JsonGenerator gen;
try {
	gen = new JsonFactory().createGenerator(new OutputStreamWriter(bout, "UTF-8"));
	
	//for pretty printing
	gen.setPrettyPrinter(new DefaultPrettyPrinter());
	gen.writeStartObject(); // start root object

	gen.writeStringField("clientId",     clientId);
	gen.writeStringField("connected",    new Timestamp(System.currentTimeMillis()).toString());
	gen.writeStringField("opencv.video", PROP.getProp("opencv.video"));
	gen.writeStringField("SO", 		  	 System.getProperty("os.name"));

	gen.writeStringField("DISPLAY",   System.getenv("DISPLAY"));
	//gen.writeNumberField("um numero", 777);
	//gen.writeBooleanField("permanent", true);

    //gen.writeEndObject(); //closing properties
	gen.writeEndObject(); //closing root object
    gen.flush();
    gen.close();
    
    instance.publish("/status", bout.toString());	
*/