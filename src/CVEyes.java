import java.awt.event.KeyEvent;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

public class CVEyes {
	private static final PropSingleton PROP = PropSingleton.INSTANCE;
	protected CascadeClassifier	cascade; 
	protected MatOfRect			detections;
	protected Rect[] 			detectedArray; 
	
	public CVEyes() {
		//System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		System.out.println("construtor do CVEyes");

		// Load the classifiers
		//cascade =  new  CascadeClassifier("haarcascade_frontalcatface.xml"); 
		cascade =   new  CascadeClassifier("haarcascade_eye.xml");
        //cascade = new  CascadeClassifier("haarcascade_mcs_mouth.xml");
    	detections=	new MatOfRect();
	}
	
    // OpenCV operations
	public boolean process(Mat frame, Mat gray) {
    	// 1) Convert image to gray

    	//Imgproc.cvtColor(frame, gray, Imgproc.COLOR_RGB2BGR, 0); // Changes blue-green-red
    	Imgproc.cvtColor(frame, gray, Imgproc.COLOR_RGBA2GRAY, 0); // Grayscale
    	
    	// Turn on equalization
    	if(PROP.getLockingKeyState(KeyEvent.VK_CAPS_LOCK)) {
    		Imgproc.equalizeHist(gray, gray);
    		//Imgproc.equalizeHist(frame, frame);
    		//message = "; +equalization";
    	}
    	
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
}
