/* 
 * https://www.programcreek.com/java-api-examples/?api=org.opencv.videoio.VideoCapture
 * https://www.tutorialspoint.com/java_dip/grayscale_conversion_opencv.htm
 * 
 * http://answers.opencv.org/question/155046/how-use-haarcascade-smile-detection-in-java/
 * 
 * https://docs.opencv.org/3.4/db/de0/group__core__utils.html#ga705441a9ef01f47acdc55d87fbe5090c
 *    => Muita função bacana!
 *    
 * https://docs.opencv.org/2.4.13.7/doc/tutorials/imgproc/table_of_content_imgproc/table_of_content_imgproc.html
 *    => Guia processamento imagens
 *    
 */
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

public class CVBase {
	private final static Logger LOGGER = Logger.getLogger("CommDummy");
	protected static final PropSingleton PROP = PropSingleton.INSTANCE;
	
	private String	opencvVideo;
	private JLabel	vidpanel;
	private JFrame	jframe;
	private String  filename;
	private long    fileSeq= 0;
	
	protected Mat				frame;
	protected Mat				gray;
	private   VideoCapture		camera;
	private long 				opencvDetectionInterval = 10;
	
	//static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }
	
	//public abstract boolean process(Mat, Mat);
	
	public CVBase () {
		System.out.println("construtor do CVBase");
		
		opencvVideo = PROP.getProp("opencv.video");
		filename =    PROP.getProp("opencv.snapshot.filename");
		
		try {
			opencvDetectionInterval = Long.parseLong(PROP.getProp("opencv.detect.interval"));
			fileSeq =                 Long.parseLong(PROP.getProp("opencv.snapshot.seq"));
			
		} catch (NumberFormatException e) {
			opencvDetectionInterval =  10;
			fileSeq = 0;
			System.out.println("Error! Default opencvDetectionInterval= " +opencvDetectionInterval);
			System.out.println("Error! Default opencv.file.seq= " +fileSeq);
		}

		// Open from a webcam (0) or a video file
		if(opencvVideo.equals("0"))
			camera = new VideoCapture(0);
		else
			camera = new VideoCapture(opencvVideo);

		if(PROP.isGUIMode()) {
			// Create a graphical JFrame for showing the video output
			jframe = new JFrame("Title");
			jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			jframe.setLayout(new FlowLayout());        
			jframe.setSize( (int)camera.get(3), (int)camera.get(4) ); //img2.getWidth(null)+50, img2.getHeight(null)+50);
			System.out.println("Size = [" +camera.get(3) +"," +camera.get(4) +"]");
			vidpanel = new JLabel();
			jframe.setContentPane(vidpanel);
			jframe.setVisible(true);
		} else {
			LOGGER.warning("No GUI detected! Will work only as server !");
		}
		
		frame =		new Mat();
    	gray =		new Mat(); //frame.clone();

        new Thread(this::run).start(); // Run inside a thread
    }
	
	void run() {
		boolean targetDetected = false;
		int     k = 0;
		long    startTime = Instant.now().toEpochMilli();
		
		while (PROP.shouldKeepRunning()) {
		    if (camera.read(frame)) {
		    	
		    	targetDetected = PROP.dynamicMethodProcess(frame, gray);
    	
				// Show 'gray' image, or the color processed, based on SCROLL_LOCK key status
		    	BufferedImage img;
				if(PROP.getLockingKeyState(KeyEvent.VK_SCROLL_LOCK)) {
					img = Mat2BufferedImage(gray);
					
				} else {
					img = Mat2BufferedImage(frame);
				}

				// Only uses Windows is have one available
		    	if(PROP.isGUIMode()) {
					ImageIcon image = new ImageIcon(img);
					vidpanel.setIcon(image);
					vidpanel.repaint();
					k++;
					jframe.setTitle("Frame=" +k);
		    	}
		    	
		    	// If any target detected....
		    	if(targetDetected) {
		    		// ... and is time to another shot... (remember: milisseconds!)
		    		if((startTime+1000*opencvDetectionInterval) < Instant.now().toEpochMilli()) {
		    			//System.out.println("Times gone");
		    			
		    			// Set to take a picture and reset clock
		    			PROP.setSnap(true);
		    			startTime = Instant.now().toEpochMilli();
		    		}		    		
		    	}
				
				// Received a command to create a snapshot OR detected the target
				if(PROP.mustSnap()) {
					//System.out.println("Salvou Prop");
					// Increment counter and save picture on global variable
					fileSeq++;
					PROP.setPictureName(filename +Long.toString(fileSeq) +".jpg");
					PROP.bufferedImage = img;
					PROP.setSnap(false);
					PROP.setProp("opencv.snapshot.seq", fileSeq);
				}
				
				try {
					Thread.sleep(1000); // kkk
					//System.out.println("Thread.sleep(100)");
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				        
			} else {
			    // Rewind video
				camera.set(Videoio.CAP_PROP_POS_FRAMES, 0);
				System.out.println("Video rewind at frame [" +k +"]");
			    k= 0;
			}	
		}
	}
    
    public static BufferedImage Mat2BufferedImage(Mat m){
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
    
    // Save an image to disk
    public void saveImage(BufferedImage img) { 
    	System.exit(-10);
        try {
        	int seq = Integer.valueOf(filename.replaceAll("[^\\d]", "" )); // remove all strings, but remains numbers
        	filename= filename.replaceAll("\\d",""); 					   // Remove all numbers
        	seq++;
        	filename += seq;
        	
            File outputfile = new File(filename + ".jpg");
            ImageIO.write(img, "jpg", outputfile);
            //Imgcodecs.imwrite("enhancedParrot.jpg", gray); // Outro jeito fácil
            
            // Set the last file name taken, and indicates to PROP that a snapshot must be sent (latter, by Argos Main)
            PROP.setProp("opencv.snapshot", filename);
            PROP.setPictureName(filename);
            //final byte[] targetPixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
            //PROP.imageInByte = targetPixels;
            
        } catch (Exception e) {
        	e.printStackTrace();
            System.out.println("error");
        }
    }
} 

