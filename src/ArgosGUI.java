import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class ArgosGUI {
	private static final PropSingleton PROP = PropSingleton.INSTANCE;

	private CommBase commInterface;
	private JFrame	 jframe;
	private JLabel	 vidpanel;
	
	String classExample = "/* Example of OpenCV dummy class for MQtt */ \n"
						+"public class MyClass4 { \n"
						+"   public void execute() { \n"
						+"       System.out.println(\"Hello world from the loaded class4 !!!\"); \n"
						+"   } \n"
						+"} \n";
	

	ArgosGUI() {
		String clientId = PROP.getProp("mqtt.client.id")+"_GUI";
		 
		if(PROP.getProp("mqtt.type").equals("aws") || PROP.getProp("mqtt.type").equals("mosquitto") )
			commInterface = new CommMQtt(clientId);
		 
		else if(PROP.getProp("mqtt.type").toLowerCase().equals("dummy"))
		     commInterface = new CommDummy(clientId);		 

		else {
			 System.out.println("Error! No Comm type allowed!");
			 System.exit(-1);
		 }
		
		// Create a graphical JFrame for showing the video output
		jframe = new JFrame("Title");
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jframe.setLayout(new FlowLayout());
		//jframe.setLayout(new GridLayout(3, 2));
		jframe.setSize( 1024, 600); 


		// Topic text
		JTextField jTopic = new JTextField(20);
		jTopic.setText("/command/class");
		
		// Class example text
		JTextArea  jClass = new JTextArea (10 /*rows*/, 40 /*cols*/);
		jClass.setText(classExample);
		jClass.setBorder(BorderFactory.createLineBorder(Color.black, 3));

	
		// Button to send
		JButton button = new JButton();
		button.setText("Send class:");
		
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
		    	System.out.println("Butao");
		        commInterface.publish(jTopic.getText(), jClass.getText());
		    }
		} );

		//jframe.setContentPane(vidpanel);
		// create a line border with the specified color and width
		vidpanel = new JLabel();
		vidpanel.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
		vidpanel.setOpaque(true);
		vidpanel.setBackground(Color.blue);
		vidpanel.setPreferredSize(new Dimension (640, 380));
		
		jframe.add(button);
		jframe.add(jTopic);
		jframe.add(jClass);
		jframe.add(vidpanel);

		jframe.setVisible(true);
	}
	
	public void run() {
		BufferedImage img = null;
		int k=0;
		
		while (PROP.shouldKeepRunning() && commInterface.isConnected() ) {
			k++;
	    	jframe.setTitle(commInterface.topicRec +k);
	    	
			try {
				// Received a picture file
				if( (PROP.getPictureName() != null) && (PROP.bufferedImage != null) ) {
					
					ImageIcon image = new ImageIcon(PROP.bufferedImage);
					vidpanel.setIcon(image);
					vidpanel.setText(PROP.getPictureName());
					vidpanel.repaint();
					k++;
					Thread.sleep(commInterface.getPublishInterval()/10) ;

				// Default test image
				} else { 
					jframe.setTitle("Frame=" +k);
				}
				if(img != null) {
			        ImageIcon image = new ImageIcon(img);
			        vidpanel.setIcon(image);
			        vidpanel.repaint();
				}
	        
				Thread.sleep(commInterface.getPublishInterval()/10) ;
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
