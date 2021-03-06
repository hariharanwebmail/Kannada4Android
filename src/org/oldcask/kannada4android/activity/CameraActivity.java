package org.oldcask.kannada4android.activity;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

public class CameraActivity extends Activity implements SurfaceHolder.Callback {

	private static final String LOG_TAG = "Kannada4AndroidCamera";
	private Camera camera;
	private static final String PIC_DATA = "PIC_DATA";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera);
		setUpSurface();

		Button takePicButton = (Button) findViewById(R.id.TakePicture);
		takePicButton.setOnClickListener(new CameraClickListener());
	}

	private void setUpSurface() {
		SurfaceView cameraSurface = (SurfaceView) findViewById(R.id.cameraSurface);
		SurfaceHolder surfaceHolder = cameraSurface.getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		camera.startPreview();		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		camera = Camera.open();
		Parameters cameraParameters = camera.getParameters();
		cameraParameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
		cameraParameters.setFlashMode(Parameters.FLASH_MODE_AUTO);
		Size minimumSize = getMinimumCameraSize(cameraParameters);
		cameraParameters.setPictureSize(minimumSize.width,minimumSize.height);
		camera.setParameters(cameraParameters);
		try {
			camera.setPreviewDisplay(holder);
		} catch (IOException e) {
			Log.e(LOG_TAG, "Somethings gone a bit wrong..." + e);
		}

	}

	private Size getMinimumCameraSize(Parameters cameraParameters) {
		List<Size> supportedPictureSizes = cameraParameters
				.getSupportedPictureSizes();
		Size minimumSize = supportedPictureSizes.get(0);
		long minimumTotalSize = minimumSize.width * minimumSize.height;

		for (int i = 1; i < supportedPictureSizes.size(); i++) {
			Size size = supportedPictureSizes.get(i);
			int width = size.width;
			int height = size.height;
			Log.d("KANNADA4ANDROID", "Available size:"+width+" "+height);
			int totalPixels = width * height;
			if (totalPixels > 115200 && totalPixels < minimumTotalSize && (((float)width/height)>1.5)) {
				minimumSize = size;
				minimumTotalSize = totalPixels;
			}
		}
		Log.d("KANNADA4ANDROID", "Camera resolution choosen:"+minimumSize.width+" "+minimumSize.height);
		return minimumSize;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		camera.release();
	}

	private final class CameraClickListener implements View.OnClickListener {
		private ShutterCallback shutterCallback = new ShutterCallbackListener();
		private PictureCallback rawCallback = new RawImageCallbackListener();
		private PictureCallback jpegCallback = new JPEGImageCallbackListener();

		public void onClick(View v) {
			camera.autoFocus(new Camera.AutoFocusCallback() {
				public void onAutoFocus(boolean success, Camera camera) {
					camera.takePicture(shutterCallback, rawCallback, jpegCallback);
				}
			});
		}
	}

	private final class JPEGImageCallbackListener implements PictureCallback {

		public void onPictureTaken(byte[] data, Camera camera) {
			Bundle bundle = new Bundle();
			bundle.putByteArray(PIC_DATA, data);

			Intent resultIntent = new Intent(getBaseContext(),
					ProcessingActivity.class);
			resultIntent.putExtras(bundle);

			startActivity(resultIntent);
		}
	}

	// do nothing
	private final class RawImageCallbackListener implements PictureCallback {
		public void onPictureTaken(byte[] data, Camera camera) {
		}
	}

	private final class ShutterCallbackListener implements ShutterCallback {
		public void onShutter() {
		}
	}
}
