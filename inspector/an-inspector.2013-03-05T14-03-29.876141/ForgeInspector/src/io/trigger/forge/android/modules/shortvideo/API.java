package io.trigger.forge.android.modules.shortvideo;

import android.content.Intent;

import io.trigger.forge.android.core.ForgeApp;
import io.trigger.forge.android.core.ForgeTask;
import io.trigger.forge.android.shortvideo.activity.VideoRecorder;


public class API {
	public static void launch(final ForgeTask task) {
		Intent intent = new Intent(ForgeApp.getActivity(), VideoRecorder.class);
		ForgeApp.getActivity().startActivity(intent);
	}
}