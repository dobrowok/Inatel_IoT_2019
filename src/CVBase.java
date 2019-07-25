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
import java.time.Instant;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

public class CVBase {
	private final static Logger LOGGER = Logger.getLogger("CommDummy");
	private static final PropSingleton PROP = PropSingleton.INSTANCE;
	
	private String	message = "";
	private String	opencvVideo;
	private JLabel	vidpanel;
	private JFrame	jframe;
	private String  filename;
	private long    fileSeq= 0;
	
	private Mat					frame;
	private Mat					gray;
	private MatOfRect			detections;
	private Rect[] 				detectedArray; 
	private VideoCapture		camera;
	private CascadeClassifier	cascade; 
	private long 				opencvDetectionInterval = 10;
	
	static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }
	
	CVBase () {
		opencvVideo = PROP.getProp("opencv.video");
		filename = PROP.getProp("opencv.snapshot.filename");
		
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
		
        // Load the classifiers
		//cascade =  new  CascadeClassifier("haarcascade_frontalcatface.xml"); 
		cascade =   new  CascadeClassifier("haarcascade_eye.xml");
        //cascade = new  CascadeClassifier("haarcascade_mcs_mouth.xml");
        
		frame =		new Mat();
    	gray =		new Mat(); //frame.clone();
    	detections=	new MatOfRect();
        

        new Thread(this::run).start(); // Run inside a thread
    }
	
	// OpenCV operations
	boolean process() {
    	// 1) Convert image to gray

    	//Imgproc.cvtColor(frame, gray, Imgproc.COLOR_RGB2BGR, 0); // Changes blue-green-red
    	Imgproc.cvtColor(frame, gray, Imgproc.COLOR_RGBA2GRAY, 0); // Grayscale
    	
    	// Turn on equalization
    	if(PROP.getLockingKeyState(KeyEvent.VK_CAPS_LOCK)) {
    		Imgproc.equalizeHist(gray, gray);
    		//Imgproc.equalizeHist(frame, frame);
    		message = "; +equalization";
    	}
    	else
    		message= "";
    	
    	//face_cascade.detectMultiScale(gray, eyedetections, 1.1, 1, 1, new Size(30,30), new Size());
	        //for (int i = 0; i < facesArray.length; i++){     
	            //Mat faceROI = image.submat(facesArray[i]);

    	// detectMultiScale(Mat, MatOfRect, double scaleFactor, int minNeighbors, int flags, Size minSize, Size maxSize)
	    cascade.detectMultiScale(gray, detections, 1.1 /*scaleFactor*/, 1 /*minNeighbors*/, 0, 
    							 new Size(30,30) /*minSize*/, new Size(100,100));

	    // Detected features
	    detectedArray = detections.toArray();
        System.out.println("Eyes Detected:" + detectedArray.length);

		// Print a circle on detected features 
		for (int j = 0; j < detectedArray.length; j++){
		
		    Point center1 = new Point(detectedArray[j].x + detectedArray[j].width * 0.5, 
		    						  detectedArray[j].y + detectedArray[j].height * 0.5);
		    //int radius = (int) Math.round( .75*(eyesArray[j].x + (eyesArray[j].width/2)) );
		    int radius = (int) Math.round( detectedArray[j].width/2);
		    //int radius = (int) Math.round( 30) ;
		    
		    // Plot a yellow circle 
		    Imgproc.circle(frame, center1, radius, new Scalar(0, 255, 255), 4, 8, 0);
		}
		
		return(detectedArray.length>0);
	}
	
	void run() {
		boolean targetDetected = false;
		int     k = 0;
		long    startTime = Instant.now().toEpochMilli();
		
		while (PROP.shouldKeepRunning()) {
		    if (camera.read(frame)) {
		    	targetDetected = process();
    	
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
					jframe.setTitle("Frame=" +k +message);
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

