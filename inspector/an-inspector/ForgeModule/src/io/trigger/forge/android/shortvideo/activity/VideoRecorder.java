package io.trigger.forge.android.shortvideo.activity;

import io.trigger.forge.android.core.ForgeApp;
import io.trigger.forge.android.core.ForgeLog;
import io.trigger.forge.android.modules.shortvideo.API;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;

public class VideoRecorder extends Activity implements SurfaceHolder.Callback {	
	private SurfaceHolder mSurfaceHolder;
    private SurfaceView mSurfaceView;
    private MediaRecorder mRecorder;
    private MediaPlayer mPlayer;
    
    private ImageButton mStartButton = null;
    private ImageButton mReturnButton = null;
    private ImageButton mRedoButton = null;
    private ProgressBar mUploadProgress = null;
    private boolean mRecording;
    private boolean mUploading;
    
    private File mVideo;
    private Camera mCamera;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
	            WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		mRecorder = new MediaRecorder();
		mPlayer = new MediaPlayer();
		
		setContentView(ForgeApp.getResourceId("video_layout", "layout"));			        
		
		SurfaceView surfaceGrid = (SurfaceView) findViewById(ForgeApp.getResourceId("surface_grid", "id"));
		surfaceGrid.setBackgroundResource(ForgeApp.getResourceId("grid", "drawable"));
		
		mSurfaceView = (SurfaceView) findViewById(ForgeApp.getResourceId("surface_camera", "id"));
		
		mUploadProgress = (ProgressBar) findViewById(ForgeApp.getResourceId("upload_progress", "id"));
		mUploadProgress.setProgressDrawable(getApplicationContext().getResources().getDrawable(ForgeApp.getResourceId("progress", "drawable")));
		mUploadProgress.setIndeterminateDrawable(getApplicationContext().getResources().getDrawable(ForgeApp.getResourceId("progress", "drawable")));
		
		mStartButton =  (ImageButton) findViewById(ForgeApp.getResourceId("buttonstart", "id"));		
		Bitmap buttonText = BitmapFactory.decodeResource(getResources(), ForgeApp.getResourceId("record", "drawable"));
		mStartButton.setImageBitmap(Bitmap.createScaledBitmap(buttonText, buttonText.getWidth()/2, buttonText.getHeight()/2, false));		
		mStartButton.setBackgroundColor(android.graphics.Color.TRANSPARENT);
		buttonText.recycle();
		
		mReturnButton =  (ImageButton) findViewById(ForgeApp.getResourceId("returnbutton", "id"));
		mReturnButton.setImageDrawable(getResources().getDrawable(ForgeApp.getResourceId("returnbtn", "drawable")));
		buttonText = BitmapFactory.decodeResource(getResources(), ForgeApp.getResourceId("returnbtn", "drawable"));
		mReturnButton.setImageBitmap(Bitmap.createScaledBitmap(buttonText, buttonText.getWidth()/2, buttonText.getHeight()/2, false));
		mReturnButton.setBackgroundResource(ForgeApp.getResourceId("button", "drawable"));			
		buttonText.recycle();
		
		mRedoButton = (ImageButton) findViewById(ForgeApp.getResourceId("redobutton", "id"));
		buttonText = BitmapFactory.decodeResource(getResources(), ForgeApp.getResourceId("redo", "drawable"));
		mRedoButton.setImageBitmap(Bitmap.createScaledBitmap(buttonText, buttonText.getWidth()/2, buttonText.getHeight()/2, false));		
		mRedoButton.setBackgroundResource(ForgeApp.getResourceId("ltgrey", "color"));
		buttonText.recycle();
		buttonText = null;
	}
	
	
	public void onResume() {
		super.onResume();		
		mRecording = false;
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
		
		mStartButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (!mRecording && mVideo == null) {
					try {				
						stopPlaying();
						startRecording();
						mRecording = true;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						mRecording = false;
						e.printStackTrace();
					}
				} else if (mVideo != null) {
					uploadVideo();					
				}
			}
			
		});
		
		mReturnButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				makeResult();
				cleanUp();
				finish();
			}			
		});
	}

	private void uploadVideo() {
		if (!mUploading) {
			UploadTask task = new UploadTask();
			task.execute(mVideo);
		}
	}
	
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	public void surfaceCreated(SurfaceHolder holder) {
		initCamera();
		try {
			mCamera.setPreviewDisplay(mSurfaceHolder);
		} catch (IOException e) {		
			e.printStackTrace();
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		ForgeLog.d("DESTROOOOOYED");
	}
	
	public void onDestroy() {
		stopPlaying();
		stopRecording();
		super.onDestroy();
	}
	
	private void onUploadFailure() {
    	mUploadProgress.setProgress(0);
    	mUploadProgress.setSecondaryProgress(100);
    	Bitmap buttonText = BitmapFactory.decodeResource(getResources(), ForgeApp.getResourceId("tryagain", "drawable"));
		mStartButton.setImageBitmap(Bitmap.createScaledBitmap(buttonText, buttonText.getWidth()/2, buttonText.getHeight()/2, false));		    		
		buttonText.recycle();
		buttonText = null;
    }
    
    private void onUploadSuccess() {
    	ForgeLog.d("SUCCESS");
    	mUploadProgress.setProgress(100);
    	Bitmap buttonText = BitmapFactory.decodeResource(getResources(), ForgeApp.getResourceId("successbtn", "drawable"));
		mStartButton.setImageBitmap(Bitmap.createScaledBitmap(buttonText, buttonText.getWidth()/2, buttonText.getHeight()/2, false));
		buttonText.recycle();
		buttonText = null;
		
		mStartButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				makeResult();
				cleanUp();
				finish();
			}			
		});
    }
    
	@SuppressLint({ "InlinedApi", "NewApi" })
	private void startRecording() throws IOException 
    {		        
		mUploadProgress.setProgress(100);
		mUploadProgress.setSecondaryProgress(0);
		
		if (mCamera == null) {			
			initCamera();
		}
		mCamera.unlock();
        Runnable r = new Runnable() {
        	public void run() {
        		try {        			
        			mRecorder = new MediaRecorder();
        			mRecorder.setCamera(mCamera);
        	        mRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        	        mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        	        
        	        mRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
        	        mRecorder.setMaxDuration(1000);
        	     
        	        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        	        mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        	        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        	        mRecorder.setOnInfoListener(new ShortVideoInfoListener());
        	        
        	        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
        	                Environment.DIRECTORY_MOVIES), "ShortVideo");
        	        if (! mediaStorageDir.exists()){
        	            if (! mediaStorageDir.mkdirs()){
        	                ForgeLog.d("failed to create directory");                
        	            }
        	        }
        	        mVideo = File.createTempFile("shortvideo", ".mp4", mediaStorageDir);
        	        
        	        mRecorder.setOutputFile(mVideo.getAbsolutePath()); 
        	        mRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
        	        mRecorder.setOrientationHint(90);
					mRecorder.prepare();
					mRecorder.start();
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}        		
        	}
        };
        new Thread(r).run();   
    }

    private void stopRecording() {  
    	if (mRecording) {
    		mRecorder.stop();
    		mRecorder.reset();
    	}
    	mRecorder.release();
    	if (mCamera != null) {
    		mCamera.stopPreview();  
    		mCamera.lock();
    		mCamera.release();
    	}
        mCamera = null;
        mRecording = false;
    }
    
    private void playVideo() {
    	mPlayer = new MediaPlayer();
    	try {
    		final Runnable startVideo = new Runnable() {
				public void run() {					
					mPlayer.start();
				}
    		};
    		mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mPlayer.setDataSource(mVideo.getAbsolutePath());
			mPlayer.setDisplay(mSurfaceHolder);			
			mPlayer.setLooping(true);
			mPlayer.prepare();
			new Thread(startVideo).run();			
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	ForgeLog.d("started video");
    }
    
    private void stopPlaying() {  
    	if (mPlayer != null) {    	
    		if (mPlayer.isPlaying()) {
    			mPlayer.stop();
    		}
    		mPlayer.reset();
    		mPlayer.release();
    		mPlayer = null;
    	}
    }
    
    private void makeResult() {    	
    	Intent data = new Intent();
    	if (mVideo != null) {
    		data.putExtra(API.VIDEO_KEY, Uri.fromFile(mVideo).toString());
    		this.setResult(RESULT_OK, data);
    	}
    	mVideo = null;
    	finish();
    }
    
    @SuppressLint("NewApi")
	private void initCamera() {
    	mCamera = Camera.open();
    	setCameraDisplayOrientation(ForgeApp.getActivity(), 0, mCamera);    	
    	try {    		
            mCamera.setPreviewDisplay(mSurfaceHolder);
            Parameters parameters = mCamera.getParameters();
            parameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            mCamera.setParameters(parameters);
            mCamera.startPreview();
        } catch (IOException e) {
        	e.printStackTrace();
            mCamera.release();
            mCamera = null;
        }
    }
    
    private void cleanUp() {
    	if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		} 
		
		if (mRecorder != null) {
			if (mRecording) {
				mRecorder.stop();
				mRecording = false;
			}			
			mRecorder.release();
		} 		
		if (mPlayer != null) {
			if (mPlayer.isPlaying()) {
				mPlayer.stop();
			}
			mPlayer.reset();			
			mPlayer.release();			
			mPlayer = null;			
		}
		if (mVideo != null) {
			mVideo = null;
		}
    }
    
    private class ShortVideoInfoListener implements MediaRecorder.OnInfoListener {
		public void onInfo(MediaRecorder mr, int what, int extra) {
			if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
				stopRecording();				
				playVideo();
				mRedoButton.setBackgroundResource(ForgeApp.getResourceId("drkblue", "color"));
				mRedoButton.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						try {				
							stopPlaying();
							startRecording();
							mRecording = true;
						} catch (IOException e) {
							// TODO Auto-generated catch block
							mRecording = false;
							e.printStackTrace();
						}
					}					
				});
				Bitmap buttonText = BitmapFactory.decodeResource(getResources(), ForgeApp.getResourceId("upload", "drawable"));		
				mStartButton.setImageBitmap(Bitmap.createScaledBitmap(buttonText, buttonText.getWidth()/2, buttonText.getHeight()/2, false));
				buttonText.recycle();
				buttonText = null;
			}
		}
    }
    
    @SuppressLint("NewApi")
	public static void setCameraDisplayOrientation(Activity activity,
            int cameraId, Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }
    
    private class UploadTask extends AsyncTask<File, Integer, URL> {
        @Override
        protected void onPreExecute() {   
        	mUploading = true;
        	Bitmap buttonText = BitmapFactory.decodeResource(getResources(), ForgeApp.getResourceId("uploading", "drawable"));
    		mStartButton.setImageBitmap(Bitmap.createScaledBitmap(buttonText, buttonText.getWidth()/2, buttonText.getHeight()/2, false));		    		
    		mUploadProgress.setProgress(0);
    		mUploadProgress.setSecondaryProgress(0);
    		buttonText.recycle();
    		buttonText = null;
        }

        @Override
        protected URL doInBackground(File... files) {	
        	for (int i =0; i<100; i++) {        		
        		try {
					Thread.sleep(100);
					publishProgress(i);
				} catch (InterruptedException e) {					
					e.printStackTrace();
				}        		        		
        	}
        	Random r = new Random();
        	if (r.nextBoolean()) {
        		try {
					return new URL("http://www.google.com");
				} catch (MalformedURLException e) {					
					e.printStackTrace();
				}
    		}
        	return null;    		
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            mUploadProgress.setProgress(progress[0]);            
        }
                        
        @Override
        protected void onPostExecute(URL result) {        	
        	if (result != null) {
        		onUploadSuccess();
        	} else {
        		onUploadFailure();
        	}        	
        	mUploading = false;
        }
    }
}
