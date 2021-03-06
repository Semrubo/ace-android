package org.linphone;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.LinearLayout;

import java.util.List;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	private SurfaceHolder mHolder;
	private Camera mCamera;
	private List<Camera.Size> mSupportedPreviewSizes;
	private Camera.Size mPreviewSize;

	public CameraPreview(Context context, Camera camera) {
		super(context);
		Log.d("CameraPreview constructor","constructor");
		mCamera = camera;
		mHolder = getHolder();
		mHolder.addCallback(this);
		// deprecated setting, but required on Android versions prior to 3.0
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		Log.d("surfaceCreated", "surfaceCreated");
		try {
			// create the surface and start camera preview
			Log.d("mCamera", String.valueOf(mCamera));
			if (mCamera == null) {
				mCamera.setPreviewDisplay(mHolder);
				mCamera.startPreview();
			}
		} catch (Throwable e) {
			Log.d(VIEW_LOG_TAG, "Error setting camera preview: " + e.getMessage());
		}
	}
	 public static int findFrontFacingCamera() {

		 /*Return 0 if no front facing camera is found. 
		 Going forward we should return -1 if no front facing camera is found 
		 and then proceed to check for a back camera.*/
		 int cameraId = 0;
		// Search for the front facing camera
		int numberOfCameras = Camera.getNumberOfCameras();
		for (int i = 0; i < numberOfCameras; i++) {
			Camera.CameraInfo info = new Camera.CameraInfo();
			try { /*Catch any camera lock exceptions. Going forward we should return some value indicating an error
				and then proceed to check for a back facing camera.*/
				Camera.getCameraInfo(i, info);
				if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
					cameraId = i;
					//cameraFront = true;
					break;
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		return cameraId;
	}
	public void refreshCamera(SurfaceHolder holder) {
		Log.d("refreshCamera","refreshCamera");
		mHolder=holder;
		if (mHolder.getSurface() == null) {
				Log.d("mHolder.getSurface() == null","preview surface does not exist");
			// preview surface does not exist
			return;
		}
		// stop preview before making changes

		try{
            if( mCamera!= null)
                mCamera.stopPreview();
		}catch(Throwable e){
			e.printStackTrace();
		}

		try {
            if( mCamera == null)
                mCamera = Camera.open(findFrontFacingCamera());
		}catch(Throwable e){
			e.printStackTrace();
		}
		if(mCamera != null){
			mCamera.stopPreview();
		}
		Log.d("mCamera.stopPreview()", "mCamera.stopPreview()");

		// set preview size and make any resize, rotate or
		// reformatting changes here
		// start preview with new settings

		Log.d("setCamera","setCamera");
		//setCamera(mCamera);
		try {
			int app_orientation=LinphoneActivity.instance().getWindowManager().getDefaultDisplay().getRotation();
			if(!LinphoneActivity.instance().isTablet()||(LinphoneActivity.instance().isTablet()&&(app_orientation==1||app_orientation==3))) {
				int layoutWidth = this.getWidth();
				int layoutHeight = this.getHeight();

				setCameraDisplayOrientation(LinphoneActivity.instance(), findFrontFacingCamera(), mCamera);

				mCamera.setPreviewDisplay(mHolder);
				Camera.Parameters parameters = mCamera.getParameters();
				Camera.Size size = getBestPreviewSize(layoutWidth, layoutHeight);
				parameters.setPreviewSize(size.width, size.height);
				int max_fps=30;
				parameters.setPreviewFrameRate(getHighestPreviewFramerate(max_fps));
				mCamera.setParameters(parameters);

				if (LinphoneActivity.instance().isTablet()) {
					Display display = LinphoneActivity.instance().getWindowManager().getDefaultDisplay();
					Point screen_size = new Point();
					display.getSize(screen_size);
					int width = screen_size.x;
					int height = screen_size.y;


					layoutWidth = 0;
					layoutHeight = 0;
					if(width>height){
						layoutWidth=width;
						layoutHeight=height;
					}else{
						layoutHeight=width;
						layoutWidth=height;
					}


					setCameraDisplayOrientation(LinphoneActivity.instance(), findFrontFacingCamera(), mCamera);

					mCamera.setPreviewDisplay(mHolder);
					parameters = mCamera.getParameters();
					size = getBestPreviewSize(layoutWidth, layoutHeight);
					parameters.setPreviewSize(size.width, size.height);
					max_fps=30;
					parameters.setPreviewFrameRate(getHighestPreviewFramerate(max_fps));
					mCamera.setParameters(parameters);

					if (LinphoneActivity.instance().isTablet()) {

						int aspect_width = width;
						int aspect_height = height;
						this.setLayoutParams(new LinearLayout.LayoutParams(aspect_width, aspect_height));
					}
				}
			}else{//We are on a tablet in portrait

				Display display = LinphoneActivity.instance().getWindowManager().getDefaultDisplay();
				Point screen_size = new Point();
				display.getSize(screen_size);
				int width = screen_size.x;
				int height = screen_size.y;


				int layoutWidth = 0;
				int layoutHeight = 0;
				if(width>height){
					layoutWidth=width;
					layoutHeight=height;
				}else{
					layoutHeight=width;
					layoutWidth=height;
				}


				setCameraDisplayOrientation(LinphoneActivity.instance(), findFrontFacingCamera(), mCamera);

				mCamera.setPreviewDisplay(mHolder);
				Camera.Parameters parameters = mCamera.getParameters();
				Camera.Size size = getBestPreviewSize(layoutWidth, layoutHeight);
				parameters.setPreviewSize(size.width, size.height);
				int max_fps=30;
				parameters.setPreviewFrameRate(getHighestPreviewFramerate(max_fps));
				mCamera.setParameters(parameters);

				if (LinphoneActivity.instance().isTablet()) {

					int aspect_width = width;
					int aspect_height = height;
					this.setLayoutParams(new LinearLayout.LayoutParams(aspect_width, aspect_height));
				}
			}
			mCamera.startPreview();
		} catch (Exception e) {
			Log.d(VIEW_LOG_TAG, "Error starting camera preview: " + e.getMessage());
		}
	}
	private Camera.Size getBestPreviewSize(int width, int height)
	{
		Camera.Size result=null;
		Camera.Parameters p = mCamera.getParameters();
		for (Camera.Size size : p.getSupportedPreviewSizes()) {
			if (size.width<=width && size.height<=height) {
				if (result==null) {
					result=size;
				} else {
					int resultArea=result.width*result.height;
					int newArea=size.width*size.height;

					if (newArea>resultArea) {
						result=size;
					}
				}
			}
		}
		return result;

	}
	private int getHighestPreviewFramerate(int upper_limit)
	{
		//initialize to 0, but it can change based off of highest available fps capture
		int framerate=0;
		Camera.Parameters p = mCamera.getParameters();
		for (int fps : p.getSupportedPreviewFrameRates()) {
			if(fps>framerate&&!(fps>upper_limit)){
				framerate=fps;
			}
		}
		return framerate;

	}

	public static void setCameraDisplayOrientation(Activity activity, int icameraId, Camera camera)
	{

		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

		Camera.getCameraInfo(icameraId, cameraInfo);

		int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();


		Log.d("setting camera display orientation",String.valueOf(rotation));
		int degrees = 0; // k

		switch (rotation)
		{
			case Surface.ROTATION_0:
				degrees = 0;
				break;
			case Surface.ROTATION_90:
				degrees = 90;
				break;
			case Surface.ROTATION_180:
				degrees = 180;
				break;
			case Surface.ROTATION_270:
				degrees = 270;
				break;

		}

		int result;

		if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
		{
			// cameraType=CAMERATYPE.FRONT;

			result = (cameraInfo.orientation + degrees) % 360;
			result = (360 - result) % 360; // compensate the mirror

		}
		else
		{ // back-facing

			result = (cameraInfo.orientation - degrees + 360) % 360;

		}
		// displayRotate=result;
		camera.setDisplayOrientation(result);


	}
//	@Override
//	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//		final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
//		final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
//		setMeasuredDimension(width, height);
//
//		if (mSupportedPreviewSizes != null) {
//			mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
//		}
//	}
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// If your preview can change or rotate, take care of those events here.
		// Make sure to stop the preview before resizing or reformatting it.
		Log.d("surfaceChanged","surfaceChanged");

		//executed twice,
		refreshCamera(holder);
	}

	public void setCamera(Camera camera) {
		//method to set a camera instance
		Log.d("setCamera","setCamera");
		mCamera = camera;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)  {
		Log.w("Camera surface destroy", "destroyed");
		if (mCamera != null) {
			Log.w("Camera surface destroy", "destroyed");
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
	}
}
