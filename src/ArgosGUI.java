import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class ArgosGUI {
	private static final PropSingleton PROP = PropSingleton.INSTANCE;

	private CommMQttImpl	commInterface;
	private JFrame			jframe;
	private JLabel			vidpanel;
	
	ArgosGUI() {
		String clientId = PROP.getInstance().getProp("client.id")+"_GUI";
		 
		if(PROP.getProp("mqtt.type").equals("aws") || PROP.getProp("mqtt.type").equals("mosquitto") )
			commInterface = new CommMQttImpl(clientId);
		 
		 else {
			 System.out.println("Error! No Comm type allowed!");
			 System.exit(-1);
		 }
		
    	try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            
    	} catch(SecurityException e){
    		e.printStackTrace();
    		System.out.println("Deu ruim!");
    	}
    	
		// Create a graphical JFrame for showing the video output
		jframe = new JFrame("Title");
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jframe.setLayout(new FlowLayout());        
		jframe.setSize( 640, 480); 
		vidpanel = new JLabel();
		jframe.setContentPane(vidpanel);
		jframe.setVisible(true);
	}
	
	public void run() {

		int k=0;
		while (PROP.shouldKeepRunning() && commInterface.isConnected() ) {
			k++;
	    	jframe.setTitle("Frame=" +k);
	    	
			try {
				Mat src = Imgcodecs.imread("h:\\Untitled.png");

				BufferedImage  img = ArgosCV.Mat2BufferedImage(src);
		        ImageIcon image = new ImageIcon(img);
		        vidpanel.setIcon(image);
		        vidpanel.repaint();
		        
				Thread.sleep(commInterface.getPublishInterval()/10) ;
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
