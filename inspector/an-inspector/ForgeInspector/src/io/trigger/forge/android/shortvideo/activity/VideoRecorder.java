package io.trigger.forge.android.shortvideo.activity;

import io.trigger.forge.android.core.ForgeApp;
import io.trigger.forge.android.modules.shortvideo.API;

import java.io.File;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

public class VideoRecorder extends Activity implements SurfaceHolder.Callback {	
	private SurfaceHolder mSurfaceHolder;
    private SurfaceView mSurfaceView;
    private MediaRecorder mRecorder;
    private MediaPlayer mPlayer;
    
    private Button mStartButton = null;
    private boolean mRecording;
    
    private File mVideo;
    private Camera mCamera;
    
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		mRecorder = new MediaRecorder();
		mPlayer = new MediaPlayer();
		
		setContentView(ForgeApp.getResourceId("video_layout", "layout"));
		mStartButton =  (Button) findViewById(ForgeApp.getResourceId("buttonstart", "id"));
		mSurfaceView = (SurfaceView) findViewById(ForgeApp.getResourceId("surface_camera", "id"));
	}
	
	
	public void onResume() {
		super.onResume();
		mCamera = Camera.open();
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
		
		mStartButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (!mRecording) {
					try {
						startRecording();
						mRecording = true;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						mRecording = false;
						e.printStackTrace();
					}
				} else {
					stopRecording();
					mRecording = false;
				}				
			}
			
		});
	}
	
	public void onDestroy() {
		mPlayer.release();
		mCamera.release();
		super.onDestroy();
	}
	
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}


	public void surfaceCreated(SurfaceHolder holder) {
		if (mCamera != null){
            Parameters params = mCamera.getParameters();
            mCamera.setParameters(params);
        }
		
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		if (mRecording) {
			mRecorder.stop();
			mRecording = false;
		}
		mCamera.stopPreview();
		mCamera.release();
		mRecorder.release();
		finish();
	}
	
	@SuppressLint("InlinedApi")
	private void startRecording() throws IOException 
    {
		mStartButton.setVisibility(View.INVISIBLE);
        mRecorder = new MediaRecorder();
        mRecorder.setCamera(mCamera);

        mCamera.unlock();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        
        mRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
        mRecorder.setMaxDuration(1000);
     
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        mRecorder.setOnInfoListener(new ShortVideoInfoListener());
        
        mVideo = File.createTempFile("shortvid", ".mp4",getApplicationContext().getCacheDir());
        mRecorder.setOutputFile(mVideo.getAbsolutePath()); 
        mRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
        
        mRecorder.prepare();
        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.reset();
        mRecorder.release();
        mCamera.lock();
        mRecording = false;
        mStartButton.setVisibility(View.VISIBLE);
    }
    
    private void sendResult() {
    	Intent data = new Intent();
    	data.putExtra(API.VIDEO_KEY, Uri.fromFile(mVideo).toString());
    	this.setResult(RESULT_OK, data);
    	finish();
    }
    
    private class ShortVideoInfoListener implements MediaRecorder.OnInfoListener {
		public void onInfo(MediaRecorder mr, int what, int extra) {
			if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
				stopRecording();
				sendResult();
				mCamera.stopPreview();
			}
		}
    }
}
