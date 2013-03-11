package io.trigger.forge.android.modules.shortvideo;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;

import io.trigger.forge.android.core.ForgeApp;
import io.trigger.forge.android.core.ForgeIntentResultHandler;
import io.trigger.forge.android.core.ForgeParam;
import io.trigger.forge.android.core.ForgeTask;
import io.trigger.forge.android.shortvideo.activity.VideoRecorder;


public class API {
	public static String VIDEO_KEY = "shortvideo";
	public static String UPLOAD_URL = "http://uploadbin.net/";
	
	public static void launch(final ForgeTask task, @ForgeParam("category") String category, @ForgeParam("token") String token) {
		Intent intent = new Intent(ForgeApp.getActivity(), VideoRecorder.class);
		ForgeApp.intentWithHandler(intent, new ForgeIntentResultHandler() {
			@Override
			public void result(int requestCode, int resultCode, Intent data) {
				if (resultCode == Activity.RESULT_CANCELED) { 
					task.error(JsonNull.INSTANCE);
				} else if (resultCode == Activity.RESULT_OK) {
					Bundle extras = data.getExtras();
					JsonObject file = new JsonObject();					
					String fileUri = (String) extras.get(VIDEO_KEY);
					file.addProperty("uri", fileUri);
					task.success(file);
				}
			}	
		});
	}
}