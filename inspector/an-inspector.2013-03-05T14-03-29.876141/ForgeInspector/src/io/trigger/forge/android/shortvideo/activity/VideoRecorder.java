package io.trigger.forge.android.shortvideo.activity;

import android.view.View;
import android.view.View.OnClickListener;
import android.app.Activity;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.media.MediaRecorder;
import android.widget.Button;
import android.hardware.Camera;
import android.os.Bundle;

import java.io.File;

import io.trigger.forge.android.core.ForgeApp;

public class VideoRecorder extends Activity implements OnClickListener, SurfaceHolder.Callback {
	private SurfaceHolder mSurfaceHolder;
    private SurfaceView mSurfaceView;
    public MediaRecorder mRecorder = new MediaRecorder();
    private Button mStartButton = null;
    
    private File video;
    private Camera mCamera;
    
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(ForgeApp.getResourceId("video_layout", "layout"));
		mStartButton =  (Button) findViewById(ForgeApp.getResourceId("buttonstart", "view"));
		mSurfaceView = (SurfaceView) findViewById(ForgeApp.getResourceId("surface_camer", "view"));
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
	}
	
	public void onDestroy() {
		
	}
	
	public void onClick(View v) {
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}

	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
	}
}
