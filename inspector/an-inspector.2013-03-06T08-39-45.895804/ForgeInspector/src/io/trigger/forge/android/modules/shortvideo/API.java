package io.trigger.forge.android.modules.shortvideo;

import android.content.Intent;

import io.trigger.forge.android.core.ForgeApp;
import io.trigger.forge.android.core.ForgeIntentResultHandler;
import io.trigger.forge.android.core.ForgeTask;
import io.trigger.forge.android.shortvideo.activity.VideoRecorder;


public class API {
	
	private static ForgeTask sActiveTask; 
	
	public static void launch(final ForgeTask task) {
		Intent intent = new Intent(ForgeApp.getActivity(), VideoRecorder.class);
		ForgeApp.intentWithHandler(intent, new ForgeIntentResultHandler() {
			@Override
			public void result(int arg0, int arg1, Intent arg2) {
				task.success();
			}
			
		});
	}
}
// onesecond.capture()
// file_data = onesecond.getFileData(); // static boid API::getFileData();