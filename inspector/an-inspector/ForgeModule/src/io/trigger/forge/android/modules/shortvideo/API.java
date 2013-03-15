package io.trigger.forge.android.modules.shortvideo;

import com.google.gson.JsonNull;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.app.Activity;

import io.trigger.forge.android.core.ForgeApp;
import io.trigger.forge.android.core.ForgeIntentResultHandler;
import io.trigger.forge.android.core.ForgeParam;
import io.trigger.forge.android.core.ForgeTask;
import io.trigger.forge.android.shortvideo.activity.VideoRecorder;


public class API {
	public static String VIDEO_KEY = "shortvideo";
	public static String UPLOAD_URL = "http://ec2-184-169-189-88.us-west-1.compute.amazonaws.com:8000/api/clip/";
	private static String TOKEN_KEY = "shortvideotoken";
	
	public static void launch(final ForgeTask task, @ForgeParam("term") String term, @ForgeParam("token") String token) {
		Intent intent = new Intent(ForgeApp.getActivity(), VideoRecorder.class);
		intent.putExtra("term", term);
		intent.putExtra("token", token);
		
		ForgeApp.intentWithHandler(intent, new ForgeIntentResultHandler() {
			@Override
			public void result(int requestCode, int resultCode, Intent data) {
				if (resultCode == Activity.RESULT_CANCELED) {					
					task.error(JsonNull.INSTANCE);
				} else if (resultCode == Activity.RESULT_OK) {
					ForgeApp.event("forge.return", null);
					task.success(JsonNull.INSTANCE);
				}
			}	
		});
	}
	
	public static void getToken(final ForgeTask task) {
		SharedPreferences preferences = ForgeApp.getActivity().getSharedPreferences("Shortvideoprefs", Context.MODE_PRIVATE);
		String token = preferences.getString(TOKEN_KEY, null);
		if ( token != null ) {
			task.success(token);
		} else {
			task.success(JsonNull.INSTANCE);
		}
	}
	
	public static void setToken(final ForgeTask task, @ForgeParam("token") String token) {
		SharedPreferences preferences = ForgeApp.getActivity().getSharedPreferences("Shortvideoprefs", Context.MODE_PRIVATE);		
		Editor editor = preferences.edit();
		editor.putString(TOKEN_KEY, token);
		editor.commit();
		task.success();
	}
}