/* 
 * https://www.programcreek.com/java-api-examples/?api=org.opencv.videoio.VideoCapture
 */
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import org.opencv.objdetect.CascadeClassifier;;

public class ArgosCV {
	private static final Logger LOGGER = Logger.getLogger(Argos.class.getName());
	private static final PropSingleton PROP = PropSingleton.INSTANCE;

	String opencvVideo;
	
	ArgosCV () {
		opencvVideo = PROP.getProp("opencv.video");

    	try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            
    	} catch(SecurityException e){
    		e.printStackTrace();
    		System.out.println("De ruim!");
    	}
    	
        //Mat mat = Mat.eye(3, 3, CvType.CV_8UC1);
        System.out.println("mat = " + "bla mat.dump()");
        run();
    }
        
    void run () {
    	VideoCapture camera;
		Mat frame = new Mat();

		// Open from a webcam (0) or a video file
		if(opencvVideo.equals("0"))
			camera = new VideoCapture(0);
		else
			camera = new VideoCapture(opencvVideo);
		
		JFrame jframe = new JFrame("Title");
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jframe.setLayout(new FlowLayout());        
		
		jframe.setSize( (int)camera.get(3), (int)camera.get(4) ); //img2.getWidth(null)+50, img2.getHeight(null)+50);     
		
		JLabel vidpanel = new JLabel();
		jframe.setContentPane(vidpanel);
		jframe.setVisible(true);
		int j=0;
		
		//CascadeClassifier face_cascade = CascadeClassifier("haarcascade_frontalface_default.xml");
		//CascadeClassifier eye_cascade  = CascadeClassifier ( "haarcascade_eye.xml");
		
				
		while (true) {
		    if (camera.read(frame)) {
		    	j++;
		    	jframe.setTitle("Frame=" +j);
		
		        ImageIcon image = new ImageIcon(Mat2BufferedImage(frame));
		        vidpanel.setIcon(image);
		        vidpanel.repaint();
		        try {
					Thread.sleep(10);
					//System.out.println("Thread.sleep(100)");
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		
		        
		    } else {
			    // Rewind video
			    camera.set(Videoio.CAP_PROP_POS_FRAMES, 0);
			    System.out.println("Video rewind at frame [" +j +"]");
			    j= 0;
		    }		    
		}
    }
        
        public BufferedImage Mat2BufferedImage(Mat m){
            //source: http://answers.opencv.org/question/10344/opencv-java-load-image-to-gui/
            //Fastest code
            //The output can be assigned either to a BufferedImage or to an Image

             int type = BufferedImage.TYPE_BYTE_GRAY;
             if ( m.channels() > 1 ) {
                 type = BufferedImage.TYPE_3BYTE_BGR;
             }
             int bufferSize = m.channels()*m.cols()*m.rows();
             byte [] b = new byte[bufferSize];
             m.get(0,0,b); // get all the pixels
             BufferedImage image = new BufferedImage(m.cols(),m.rows(), type);
             final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
             System.arraycopy(b, 0, targetPixels, 0, b.length);  
             return image;
        }       
} 