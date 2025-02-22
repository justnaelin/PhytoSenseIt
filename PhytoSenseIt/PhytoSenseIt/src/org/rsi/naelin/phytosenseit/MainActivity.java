package org.rsi.naelin.phytosenseit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgcodecs.*;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.core.Size;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity implements CvCameraViewListener2,
	OnTouchListener {
    //private static final String TAG = "PhytoSenseIt::Activity";
    private static String mImageNameTag = "";
    private static int mTemplNumTag = -1;
    private static String mTemplNameTag = "";

    private CameraView mOpenCvCameraView;
    private List<android.hardware.Camera.Size> mResolutionList;
    private MenuItem[] mEffectMenuItems;
    private SubMenu mColorEffectsMenu;
    private MenuItem[] mResolutionMenuItems;
    private SubMenu mResolutionMenu;
    private Button mTakePhotoButton;
    private String mPhotoFilename;
    private ImageView mBinaryImageView;
    private Mat mImgMat;
    private Mat mTemplMat;
    private Mat mResultMat;
    private Mat mImgMatSat;
    private Mat mTemplMatSat;
    private Mat mImgMatBin;
    private Mat mTemplMatBin;
    private Mat mImgMatGray;
    private Mat mTemplMatGray;
    private Bitmap mImgBmp;
    private Bitmap mTemplBmp;
    private Bitmap mResultBmp;
    private double[] mMaxValues = new double[8];
    private int x = 0;
    private Point[] mMaxLoc = new Point[8];
    
    // Cascade classifier
    private static final String TAG = "BinTest";
    private Mat img; // matric that will hold image
    private ImageView imgV;
    CascadeClassifier greyCas; // cascade classifier variable
    String cas_grey_fname; 
    // For logging purposes
    private static String classifierTAG = "Cascade Class";

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
	@Override
	public void onManagerConnected(int status) {
	    switch (status) {
	    case LoaderCallbackInterface.SUCCESS: {
		Log.i(TAG, "OpenCV loaded successfully");
		
		// ========== Cascade Classifier start =================
	        // Create bitmap of image to run classifier on
                Bitmap imgBMap = BitmapFactory.decodeResource(getResources(), R.drawable.potato_blackscurf_12);
    		// create the memory for the matrix
    		img = new Mat(imgBMap.getHeight(), imgBMap.getWidth(), CvType.CV_8U);
    		// Convert bitmap to a matrix
    		Utils.bitmapToMat(imgBMap, img);
  
    		// Create a new matrix for the image that will be resized
    		Mat img2 = new Mat();
		// Resize image to 100 by 100 - this might not be necessary
    		Imgproc.resize(img, img2, new Size(100,100),0.0,0.0,Imgproc.INTER_LINEAR);
    		// Make sure image is in grayscale
    		Imgproc.cvtColor(img2, img2, Imgproc.COLOR_RGB2GRAY);

    		// Get file name for classifier
    		AssetManager am = getAssets();
    		InputStream inputS;
		try {
			// create local copy of file so we can open it with opencv
			inputS = am.open("cascadebs.xml"); // filename of cascade classifier
			Log.d(classifierTAG, "Opened asset");
			cas_grey_fname = getAndCreateFileName(inputS);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    		
    		// log file was created successfully
    		Log.d(classifierTAG, "File Created: " + cas_grey_fname);
    		// Create a new classifier object
		greyCas = new CascadeClassifier();
		// load file and check that load was successful
    		if(!(greyCas.load(cas_grey_fname))){
    			
    			Log.d(classifierTAG, "ERROR: Could not load classifier");
    		}
    		Log.d(classifierTAG, "Classifier file loaded");
    		// ======== Run classifier =====================
    		MatOfRect rects = new MatOfRect();
    		// equalize image - uncomment following line to run
    		Imgproc.equalizeHist(img2, img2);
    		Size s_start = new Size(5,5);
    		Size s_stop = new Size(100, 100);
		// run classifier on image
    		greyCas.detectMultiScale(img2, rects, 1.01, 1,3, s_start, s_stop);
    	
    		Log.d(classifierTAG, "Completed detection");
    		// Display result if any
    		Rect arr[] = rects.toArray();
    		Log.d(classifierTAG, "# Rects = " + arr.length);
    		if(arr.length > 0){
    			Imgproc.cvtColor(img2, img2, Imgproc.COLOR_GRAY2RGBA);
    			Imgproc.rectangle(img2, new Point(arr[0].x, arr[0].y), 
    				new Point(arr[0].width, arr[0].height), new Scalar(0,255,0));
    		}

    		Log.d(classifierTAG, "After draw rectangle");
    		// Display resized image on android screen
    		imgV = (ImageView) findViewById(R.id.image_view);
    		Bitmap bmp = Bitmap.createBitmap(img2.cols(), img2.rows(), Bitmap.Config.ARGB_8888);
    		Utils.matToBitmap(img2, bmp);//imgBMap);// convert matrix to bitmap
    		imgV.setImageBitmap(bmp);//imgBMap);// set image view to binary image
    		
    		
    		// ========== Cascade Classifier end ==========
    		 
    		 
		
    		// ========== Camera View ================
		mOpenCvCameraView.enableView();
		mOpenCvCameraView.setOnTouchListener(MainActivity.this);
	    }
		break;
	    default: {
		super.onManagerConnected(status);
	    }
		break;
	    }
	}
    };

    public MainActivity() {
	Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
	Log.i(TAG, "called onCreate");
	super.onCreate(savedInstanceState);
	getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

	// Set views
	setContentView(R.layout.activity_main);
	mOpenCvCameraView = (CameraView) findViewById(R.id.java_surface_view);
	mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
	mOpenCvCameraView.setCvCameraViewListener(this);

	// Button for detection process
	mTakePhotoButton = (Button) findViewById(R.id.take_photo_button);
	mTakePhotoButton.setOnClickListener(new View.OnClickListener() {

	    @Override
	    public void onClick(View v) {
		// binaryThresholding();
		mOpenCvCameraView.setVisibility(SurfaceView.GONE);
		imageAcquisition();
	    }
	});
    }

    @Override
    public void onPause() {
	super.onPause();
	if (mOpenCvCameraView != null)
	    mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
	super.onResume();
	if (!OpenCVLoader.initDebug()) {
	    Log.d(TAG,
		    "Internal OpenCV library not found. Using OpenCV Manager for initialization");
	    OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this,
		    mLoaderCallback);
	} else {
	    Log.d(TAG, "OpenCV library found inside package. Using it!");
	    mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
	}
    }

    public void onDestroy() {
	super.onDestroy();
	if (mOpenCvCameraView != null)
	    mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
	return inputFrame.rgba();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	List<String> effects = mOpenCvCameraView.getEffectList();

	if (effects == null) {
	    Log.e(TAG, "Color effects are not supported by device!");
	    return true;
	}

	mColorEffectsMenu = menu.addSubMenu("Color Effect");
	mEffectMenuItems = new MenuItem[effects.size()];

	int idx = 0;
	ListIterator<String> effectItr = effects.listIterator();
	while (effectItr.hasNext()) {
	    String element = effectItr.next();
	    mEffectMenuItems[idx] = mColorEffectsMenu.add(1, idx, Menu.NONE,
		    element);
	    idx++;
	}

	mResolutionMenu = menu.addSubMenu("Resolution");
	mResolutionList = mOpenCvCameraView.getResolutionList();
	mResolutionMenuItems = new MenuItem[mResolutionList.size()];

	ListIterator<android.hardware.Camera.Size> resolutionItr = mResolutionList.listIterator();
	idx = 0;
	while (resolutionItr.hasNext()) {
	    android.hardware.Camera.Size element = resolutionItr.next();
	    mResolutionMenuItems[idx] = mResolutionMenu.add(2, idx, Menu.NONE,
		    Integer.valueOf(element.width).toString() + "x"
			    + Integer.valueOf(element.height).toString());
	    idx++;
	}

	return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
	Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
	if (item.getGroupId() == 1) {
	    mOpenCvCameraView.setEffect((String) item.getTitle());
	    Toast.makeText(this, mOpenCvCameraView.getEffect(),
		    Toast.LENGTH_SHORT).show();
	} else if (item.getGroupId() == 2) {
	    int id = item.getItemId();
	    android.hardware.Camera.Size resolution = mResolutionList.get(id);
	    mOpenCvCameraView.setResolution(resolution);
	    resolution = mOpenCvCameraView.getResolution();
	    String caption = Integer.valueOf(resolution.width).toString() + "x"
		    + Integer.valueOf(resolution.height).toString();
	    Toast.makeText(this, caption, Toast.LENGTH_SHORT).show();
	}

	return true;
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
	Log.i(TAG, "onTouch event");
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
	String currentDateandTime = sdf.format(new Date());
	mPhotoFilename = Environment.getExternalStorageDirectory().getPath()
		+ "/test_" + currentDateandTime + ".jpg";
	mOpenCvCameraView.takePicture(mPhotoFilename);
	Toast.makeText(this, mPhotoFilename + " saved", Toast.LENGTH_SHORT)
		.show();
	return false;
    }

    private String getAndCreateFileName(InputStream is)
    {
    	try{
    		File f = new File(getFilesDir()+"/cascade_greyF.xml");
    		Log.d(classifierTAG, f.getAbsolutePath());
    		OutputStream os = new FileOutputStream(f);
    		byte buff[] = new byte[1024];
    		int length = 0;
    		while((length = is.read(buff)) > 0 ){
    			os.write(buff, 0, length);
    		}
    		os.close();
    		is.close();
    		//Log.i(classifierTAG, f.getAbsolutePath());
    		return f.getAbsolutePath();
    	}catch(IOException e){
    		Log.e(classifierTAG, "Could not write file");
    	}
    	return "";
    }
    
    public void binaryThresholdingImg() {
	// mImgMat = Imgcodecs.imread(mPhotoFilename,
	// Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);

	// Load/run black scurf test images against diseased templates

	// Convert test image to binary
	mImgMatBin = new Mat();
	double threshVal = Imgproc.threshold(mImgMat, mImgMatBin, 127, 255,
		Imgproc.THRESH_BINARY);
	Log.d("binaryThresholdingImg", "Applied binary image");
	// Convert template to binary
	// | Imgproc.THRESH_OTSU.
	// Imgcodecs.imwrite(mPhotoFilename, mImgMat);
	// }
	// Convert mat to bmp Bitmap mImgBmp =
	// Bitmap.createBitmap(mImgMat.cols(), mImgMat.rows(),
	// Bitmap.Config.ARGB_8888);
	// Utils.matToBitmap(mImgMat, mImgBmp);
	// Log.d("binaryThresholding", "bmp h=" + mImgBmp.getHeight() + " w=" +
	// mImgBmp.getWidth());
	// Close camera view
	// mOpenCvCameraView.setVisibility(SurfaceView.GONE);

	// Display bmp in image view mBinaryImageView = (ImageView)
	// mBinaryImageView = (ImageView) findViewById(R.id.image_view);
	// mBinaryImageView.setImageBitmap(mImgBmp);

	// templateMatching();
    }

    public void binaryThresholdingTempl() {
	mTemplMatBin = new Mat();
	Imgproc.threshold(mTemplMat, mTemplMatBin, 127, 255,
		Imgproc.THRESH_BINARY);
	Log.d("binaryThresholdingTempl", "Applied binary image");
    }

    public void templateMatching(Mat imgMat, Mat templMat) {
	/*
	 * // Load photo taken from camera app imgMat =
	 * Imgcodecs.imread(mPhotoFilename);
	 */
	Log.d("templateMatching", "Start");
	// Apply template to mat image
	Imgproc.matchTemplate(imgMat, templMat, mResultMat,
		Imgproc.TM_CCOEFF_NORMED);
	Log.d("templateMatching", "Applied template matching");

	// Save max/location into respective arrays
	MinMaxLocResult mmr = Core.minMaxLoc(mResultMat);
	
	// Normalize black scurf and common scab values
	if(x >= 0 &&  x < 6)
	    mMaxValues[x] = mmr.maxVal - 0.1;
	else
	    mMaxValues[x] = mmr.maxVal;
	Log.d("templateMatching", "Saved max into array");
	
	Log.d("All maxes",
		mImageNameTag + mTemplNameTag + Integer.toString(mTemplNumTag)
			+ "=" + Double.toString(mMaxValues[x]));

	x += 1;

	// Normalize
	Core.normalize(mResultMat, mResultMat, 0, 1, Core.NORM_MINMAX, -1,
		new Mat());
	Log.d("templateMatching", "Normalized");
    }

    public void extractSaturationChannelImg() {
	// Convert mat test image to HSV
	mImgMatSat = new Mat();
	Imgproc.cvtColor(mImgMat, mImgMatSat, Imgproc.COLOR_BGR2HSV);
	Log.d("extractSaturationChannelImg", "Applied HSV to test image");
	//convertToGrayscaleImg();

	// Split hue, saturation, and value
	List<Mat> imgChannels = new ArrayList<Mat>();
	for (int i = 0; i < mImgMatSat.channels(); i++) {
	    Mat channel = new Mat();
	    imgChannels.add(channel);
	}
	Core.split(mImgMatSat, imgChannels);
	mImgMatSat = imgChannels.get(1); // Get saturation channel for image
    }

    public void extractSaturationChannelTempl() {
	// Convert mat template to HSV
	mTemplMatSat = new Mat();
	Imgproc.cvtColor(mTemplMat, mTemplMatSat, Imgproc.COLOR_BGR2HSV);
	Log.d("extractSaturationChannelTempl", "Applied HSV to template image");
	//convertToGrayscaleTempl();


	List<Mat> templChannels = new ArrayList<Mat>();
	for (int i = 0; i < mTemplMatSat.channels(); i++) {
	    Mat channel = new Mat();
	    templChannels.add(channel);
	}
	Core.split(mTemplMatSat, templChannels);
	mTemplMatSat = templChannels.get(1); // Get saturation channel for
					     // template
	Log.d("extractSaturationChannelTempl", "Done");
    }

    public void imageAcquisition() { // FOR TESTING PURPOSES
	Log.d("imageAcquisition", "Start");
	Context c = getApplicationContext();

	// Load/run black scurf test images against diseased templates
	String diseaseName = "potato_blackscurf_";
	int imgNum = 0;
	mImageNameTag = "BS_";
	while (imgNum < 18) {
	    int idName = c.getResources().getIdentifier((diseaseName + imgNum),
		    "drawable", c.getPackageName());
	    mImgBmp = BitmapFactory.decodeResource(getResources(), idName);
	    Log.d("imageAcquisition", diseaseName + Integer.toString(imgNum));
	    setupMatObjectsImg();
	    getTemplates();
	    determinePossibleDisease();
	    x = 0;
	    imgNum += 1;
	}
	// Load/run common scab test images against diseased templates
	diseaseName = "potato_commonscab_";
	imgNum = 0;
	mImageNameTag = "CS_";
	while (imgNum < 18) {
	    int idName = c.getResources().getIdentifier((diseaseName + imgNum),
		    "drawable", c.getPackageName());
	    mImgBmp = BitmapFactory.decodeResource(getResources(), idName);
	    Log.d("imageAcquisition", diseaseName + Integer.toString(imgNum));
	    setupMatObjectsImg();
	    // convertToGrayscaleImg();
	    getTemplates();
	    determinePossibleDisease();
	    x = 0;
	    imgNum += 1;

	}
	// Load/run healthy test images against templates
	diseaseName = "potato_healthy_";
	mImageNameTag = "H";
	imgNum = 1;
	while (imgNum < 18) {
	    int idName = c.getResources().getIdentifier((diseaseName + imgNum),
		    "drawable", c.getPackageName());
	    mImgBmp = BitmapFactory.decodeResource(getResources(), idName);
	    Log.d("imageAcquisition", diseaseName + Integer.toString(imgNum));
	    setupMatObjectsImg();
	    // convertToGrayscaleImg();
	    getTemplates();
	    determinePossibleDisease();
	    x = 0;
	    imgNum += 1;
	}

	// Load/run silver scurf test images against templates
	diseaseName = "potato_silverscurf_";
	mImageNameTag = "SS";
	imgNum = 1;
	while (imgNum < 18) {
	    int idName = c.getResources().getIdentifier((diseaseName + imgNum),
		    "drawable", c.getPackageName());
	    mImgBmp = BitmapFactory.decodeResource(getResources(), idName);
	    Log.d("imageAcquisition", diseaseName + Integer.toString(imgNum));
	    setupMatObjectsImg();
	    getTemplates();
	    determinePossibleDisease();
	    x = 0;
	    imgNum += 1;
	}
    }

    public void convertToGrayscaleImg() {
	mImgMatGray = new Mat();
	Imgproc.cvtColor(mImgMatSat, mImgMatGray, Imgproc.COLOR_BGR2GRAY);
	Log.d("convertToGrayscaleImg", "Clear");
    }

    public void convertToGrayscaleTempl() {
	mTemplMatGray = new Mat();
	Imgproc.cvtColor(mTemplMatSat, mTemplMatGray, Imgproc.COLOR_BGR2GRAY);
	Log.d("convertToGrayscaleTempl", "Clear");
    }

    public void displayResult(int index) {
	/*
	 * // Draw a rectangle around the match Imgproc.rectangle(mImgMat,
	 * mMaxLoc[index], new Point(mMaxLoc[index].x + mTemplMat.cols(),
	 * mMaxLoc[index].y + mTemplMat.rows()), new Scalar(255,255,255));
	 */

	// Convert final mat image to bitmap
	mResultBmp = Bitmap.createBitmap(mImgMat.cols(), mImgMat.rows(),
		Bitmap.Config.ARGB_8888);
	Utils.matToBitmap(mImgMat, mResultBmp);

	// Close camera view
	mOpenCvCameraView.setVisibility(SurfaceView.GONE);

	// Display bmp in image view
	mBinaryImageView = (ImageView) findViewById(R.id.image_view);
	mBinaryImageView.setImageBitmap(mResultBmp);

	CharSequence text = null;
	if (index >= 0 || index <= 2) { // Indicates highest value was found to
					// be black scurf
	    text = "Disease detected: black scurf";
	} else if (index >= 3 || index <= 5) { // Indicates highest value was
					       // found to be common scab
	    text = "Disease detected: common scab";
	} else if (index >= 6 || index <= 9) { // Indicates highest value was
					       // found to be silver scurf
	    text = "Disease detected: silver scurf";
	}
	Toast toast = Toast.makeText(getApplicationContext(), text,
		Toast.LENGTH_LONG);
	toast.show();
    }

    public void determinePossibleDisease() {
	double maxSoFar = mMaxValues[0];
	for (int i = 1; i < 8; i++) {
	    if (mMaxValues[i] > maxSoFar) {
		maxSoFar = mMaxValues[i];
	    }
	}
	Log.d("MAX VALUE", Double.toString(maxSoFar));
	int maxIndex = 0;
	for (int j = 0; j < 8; j++) {
	    if (mMaxValues[j] == maxSoFar)
		maxIndex = j;
	}

	if (maxIndex >= 0 && maxIndex <= 2) {
	    Log.d("DISEASE DETECTED", mImageNameTag + " Index " + maxIndex
		    + " Black scurf");
	} else if (maxIndex >= 3 && maxIndex <= 5) {
	    Log.d("DISEASE DETECTED", mImageNameTag + " Index " + maxIndex
		    + " Common scab");
	} else if (maxIndex == 7) {
	    Log.d("DISEASE DETECTED", mImageNameTag + " Index " + maxIndex
		    + " Silver scurf");
	} else {
	    Log.d("DISEASE DETECTED", mImageNameTag + " Healthy");
	}
    }

    public void getTemplates() {
	Log.d("getTemplates", "Start");
	Context c = getApplicationContext();

	// Loop through templates
	// Run through black scurf templates
	String templName = "potato_blackscurf_templ_";
	mTemplNameTag = templName;
	int imgNum = 1;
	
	while (imgNum < 4) {
	    mTemplNumTag = imgNum;
	    int idName = c.getResources().getIdentifier((templName + imgNum),
		    "drawable", c.getPackageName());
	    mTemplBmp = BitmapFactory.decodeResource(getResources(), idName);
	    Log.d("getTemplates", templName + Integer.toString(imgNum));
	    setupMatObjectsTempl();
	    templateMatching(mImgMat, mTemplMat);
	    imgNum++;
	}

	// Run through common scab templates
	templName = "potato_commonscab_templ_";
	mTemplNameTag = templName;
	imgNum = 1;
	while (imgNum < 4) {
	    mTemplNumTag = imgNum;
	    int idName = c.getResources().getIdentifier((templName + imgNum),
		    "drawable", c.getPackageName());
	    mTemplBmp = BitmapFactory.decodeResource(getResources(), idName);
	    Log.d("getTemplates", templName + Integer.toString(imgNum));
	    setupMatObjectsTempl();
	    templateMatching(mImgMat, mTemplMat);
	    imgNum++;
	}
	
	// Run through healthy template
	templName = "potato_healthy_";
	mTemplNameTag = templName;
	imgNum = 1;
	mTemplNumTag = imgNum;
	
	extractSaturationChannelImg();
	
	int idName = c.getResources().getIdentifier((templName + imgNum),
		"drawable", c.getPackageName());
	mTemplBmp = BitmapFactory.decodeResource(getResources(), idName);
	Log.d("getTemplates", templName + Integer.toString(imgNum));
	setupMatObjectsTempl();
	extractSaturationChannelTempl();
	templateMatching(mImgMatSat, mTemplMatSat);

	// Run through silver scurf templates
	templName = "potato_silverscurf_templ_";
	mTemplNameTag = templName;
	imgNum = 1;
	
	//extractSaturationChannelImg();
	
	while (imgNum < 2) {
	    mTemplNumTag = imgNum;
	    idName = c.getResources().getIdentifier((templName + imgNum),
		    "drawable", c.getPackageName());
	    mTemplBmp = BitmapFactory.decodeResource(getResources(), idName);
	    Log.d("getTemplates", templName + Integer.toString(imgNum));
	    setupMatObjectsTempl();
	    extractSaturationChannelTempl();
	    templateMatching(mImgMatSat, mTemplMatSat);
	    imgNum++;
	}

	/*
	 * FOR TESTING PURPOSES, NO NEED TO DISPLAY int index =
	 * determinePossibleDisease(); displayResult(index);
	 */
    }

    public void setupMatObjectsImg() {
	// Create new mat object for image
	mImgMat = new Mat();
	Log.d("setupMatObjectsImg", "Created mat object for test");

	// Convert bitmap test image to OpenCV mat
	Utils.bitmapToMat(mImgBmp, mImgMat);
	Log.d("setupMatObjectsImg", "Converted test bitmap to OpenCV mat");

    }

    public void setupMatObjectsTempl() {
	// Create new mat object for template
	mTemplMat = new Mat();
	Log.d("setupMatObjectsTempl", "Created mat object for template");

	// Create new mat object for results matrix
	int resultCols = mImgMat.cols() - mTemplMat.cols() + 1;
	int resultRows = mImgMat.rows() - mTemplMat.rows() + 1;
	mResultMat = new Mat(resultRows, resultCols, CvType.CV_32FC1);

	// Convert bitmap template to OpenCV mat
	Utils.bitmapToMat(mTemplBmp, mTemplMat);
	Log.d("setupMatObjectsTempl", "Converted template bitmap to OpenCV mat");

    }

}
