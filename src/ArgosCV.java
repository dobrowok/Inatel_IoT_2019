/* 
 * https://www.programcreek.com/java-api-examples/?api=org.opencv.videoio.VideoCapture
 * https://www.tutorialspoint.com/java_dip/grayscale_conversion_opencv.htm
 */
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
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
import org.opencv.objdetect.CascadeClassifier;

public class ArgosCV {
	private static final Logger LOGGER = Logger.getLogger(Argos.class.getName());
	private static final PropSingleton PROP = PropSingleton.INSTANCE;

	private String opencvVideo;
	
	ArgosCV () {
		opencvVideo = PROP.getProp("opencv.video");

    	try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            
    	} catch(SecurityException e){
    		e.printStackTrace();
    		System.out.println("Deu ruim!");
    	}
    	
        //Mat mat = Mat.eye(3, 3, CvType.CV_8UC1);
        System.out.println("mat = " + "bla mat.dump()");
        
        new Thread(this::run).start(); // Run inside a thread
    }
        
	void run() {
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
		int k=0;
		
		while (true) {
		    if (camera.read(frame)) {
		    	System.out.println(frame);
		    	
		    	
		    	k++;
		    	jframe.setTitle("Frame=" +k);
		    	
		    	Mat gray = frame.clone();
		    	Imgproc.cvtColor(gray, frame, Imgproc.COLOR_RGBA2GRAY, 0);
		    	
		    	//cv.cvtColor(src, gray, cv.COLOR_RGBA2GRAY, 0);

		    	//try {
			        CascadeClassifier eye_cascade =   new  CascadeClassifier("haarcascade_mcs_mouth.xml");//"haarcascade_eye.xml");
			        CascadeClassifier mouth_cascade = new  CascadeClassifier("haarcascade_mcs_mouth.xml");

			        MatOfRect eyedetections=   new MatOfRect();
			        MatOfRect mouthDetections= new MatOfRect();

			        MatOfRect faceDetections = new MatOfRect();
			        //faceDetector.detectMultiScale(frame, faceDetections, 1.1, 1, 1, new Size(30,30), new Size());

			        Rect[] rect2=faceDetections.toArray();
			        rect2.toString();
			        
			        //Rect[] facesArray = facedetections.toArray();

			        //for (int i = 0; i < facesArray.length; i++){     
			            //Mat faceROI = image.submat(facesArray[i]);

			            eye_cascade.detectMultiScale(frame, eyedetections, 1.1, 1, 1, new Size(30,30), new Size());
			            Rect[] eyesArray = eyedetections.toArray();
			            System.out.println("Eyes Detected:" + eyesArray.length);

			            //if(eyesArray.length>0) {
			            for (int j = 0; j < eyesArray.length; j++){

			                Point center1 = new Point(eyesArray[j].x + eyesArray[j].width * 0.5, 
			                						  eyesArray[j].y + eyesArray[j].height * 0.5);
			                int radius = (int) Math.round( 5 );
			                Imgproc.circle(frame, center1, radius, new Scalar(255, 0, 0), 4, 8, 0);
			            }
			            //}
			            /*
			            mouth_cascade.detectMultiScale(faceROI, mouthDetections, 1.1, 1, 1, new Size(30,30), new Size());
			            Rect[] mouthArray = mouthDetections.toArray();

			            for (int j = 0; j < 1; j++){

			                Point center1 = new Point(facesArray[i].x + mouthArray[j].x + mouthArray[j].width * 0.5, 
			                        facesArray[i].y + mouthArray[j].y + mouthArray[j].height * 0.5);
			                int radius = (int) Math.round( 3 );
			                Core.circle(image, center1, radius, new Scalar(255, 0, 0), 4, 8, 0);
			            }
*/
			        //}
			        //return image;
			            
				    	BufferedImage img = Mat2BufferedImage(frame);
				        ImageIcon image = new ImageIcon(img);
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
					    System.out.println("Video rewind at frame [" +k +"]");
					    k= 0;
				    }	
	        /*
		} catch (final Exception e) {
			System.out.println("Exception" + e);
		} finally {
			System.out.println("--done--");
		}
*/

		}
	}
	
    void run2 () {
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
		
		
		/*try {
			new Thread(() ->{
			    System.out.println("Does it work?");
			    System.out.println("Nope, it doesnt...again.");       
			})
			{{start();}}.join();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}  */
		
		/*
		JFrame jj= new JFrame("Title");
		jj.setBackground(Color.black);
		jj.setLayout(new FlowLayout());
		jj.setSize(300,300);
		jj.setContentPane(new JLabel());
		jj.setVisible(true); */
		
		
		//CascadeClassifier face_cascade = CascadeClassifier("haarcascade_frontalface_default.xml");
		//CascadeClassifier eye_cascade  = CascadeClassifier ( "haarcascade_eye.xml");
		
				
		while (true) {
		    if (camera.read(frame)) {
		    	j++;
		    	jframe.setTitle("Frame=" +j);
		    	
		    	
		    	BufferedImage img = Mat2BufferedImage(frame);
		        ImageIcon image = new ImageIcon(img);
		        vidpanel.setIcon(image);
		        vidpanel.repaint();
		        
		        // Received a command to create a snapshot
		        if(PROP.mustSnap()) {
		        	saveImage(img);
		        	PROP.setSnap(false);
		        }
		        
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
    
    // Save an image
    public void saveImage(BufferedImage img) {        
        try {
        	String filename = PROP.getProp("opencv.snapshot");

        	int seq = Integer.valueOf(filename.replaceAll("[^\\d]", "" )); // remove all strings, but remains numbers
        	filename= filename.replaceAll("\\d",""); 					   // Remove all numbers
        	seq++;
        	filename += seq;
        	
            File outputfile = new File(filename + ".jpg");
            ImageIO.write(img, "jpg", outputfile);
            
            PROP.setProp("opencv.snapshot", filename);            
            
        } catch (Exception e) {
        	e.printStackTrace();
            System.out.println("error");
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

/* 
public class Java8Thread {

public Java8Thread ()  {
    System.out.println("Main thread");
    new Thread(this::myBackgroundTask).start();
}

private void myBackgroundTask() {
    System.out.println("Inner Thread");
}
	public void execute() {
		System.out.println("Hello world from the loaded class5 !!!");
	}
}

*/