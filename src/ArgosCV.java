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

public class ArgosCV {
	private static final PropSingleton PROP = PropSingleton.INSTANCE;

	public String	message = "";
	private String	opencvVideo;
	public JLabel	vidpanel;
	public JFrame	jframe;
	
	public Mat					frame;
	public Mat					gray;
	public MatOfRect			detections;
	public Rect[] 				detectedArray; 
	public VideoCapture			camera;
	public CascadeClassifier	cascade; 
	
	ArgosCV () {
		opencvVideo = PROP.getProp("opencv.video");

    	try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            
    	} catch(SecurityException e){
    		e.printStackTrace();
    		System.out.println("Deu ruim!");
    	}
    	
		// Open from a webcam (0) or a video file
		if(opencvVideo.equals("0"))
			camera = new VideoCapture(0);
		else
			camera = new VideoCapture(opencvVideo);
		
		// Create a graphical JFrame for showing the video output
		jframe = new JFrame("Title");
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jframe.setLayout(new FlowLayout());        
		jframe.setSize( (int)camera.get(3), (int)camera.get(4) ); //img2.getWidth(null)+50, img2.getHeight(null)+50);
		System.out.println("Size = [" +camera.get(3) +"," +camera.get(4) +"]");
		vidpanel = new JLabel();
		jframe.setContentPane(vidpanel);
		jframe.setVisible(true);
		
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
	void process() {
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
	}
	
	void run() {
		int k=0;
		while (PROP.shouldKeepRunning()) {
		    if (camera.read(frame)) {
		    	process();
			            
				// Show 'gray' image, or the color processed, based on SCROLL_LOCK key status
				BufferedImage img;
				if(PROP.getLockingKeyState(KeyEvent.VK_SCROLL_LOCK)) {
					img = Mat2BufferedImage(gray);
					
				} else {
					img = Mat2BufferedImage(frame);
				}
				
				ImageIcon image = new ImageIcon(img);
				vidpanel.setIcon(image);
				vidpanel.repaint();
				k++;
				jframe.setTitle("Frame=" +k +message);
				
				// Received a command to create a snapshot
				if(PROP.mustSnap()) {
					saveImage(img);
					PROP.setSnap(false);
				}
				
				try {
					Thread.sleep(100);
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
    
    // Save an image
    public void saveImage(BufferedImage img) {        
        try {
        	String filename = PROP.getProp("opencv.snapshot.filename");

        	int seq = Integer.valueOf(filename.replaceAll("[^\\d]", "" )); // remove all strings, but remains numbers
        	filename= filename.replaceAll("\\d",""); 					   // Remove all numbers
        	seq++;
        	filename += seq;
        	
            File outputfile = new File(filename + ".jpg");
            ImageIO.write(img, "jpg", outputfile);
            //Imgcodecs.imwrite("enhancedParrot.jpg", gray); // Outro jeito fácil
            
            PROP.setProp("opencv.snapshot", filename);            
            
        } catch (Exception e) {
        	e.printStackTrace();
            System.out.println("error");
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
} 

