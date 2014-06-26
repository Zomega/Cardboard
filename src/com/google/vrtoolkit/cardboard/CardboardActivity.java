package com.google.vrtoolkit.cardboard;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import com.google.vrtoolkit.cardboard.sensors.MagnetSensor;
import com.google.vrtoolkit.cardboard.sensors.MagnetSensor.OnCardboardTriggerListener;
import com.google.vrtoolkit.cardboard.sensors.NfcSensor;
import com.google.vrtoolkit.cardboard.sensors.NfcSensor.OnCardboardNfcListener;

public class CardboardActivity extends Activity implements
		MagnetSensor.OnCardboardTriggerListener,
		NfcSensor.OnCardboardNfcListener {
	private static final int NAVIGATION_BAR_TIMEOUT_MS = 2000;
	private CardboardView mCardboardView;
	private MagnetSensor mMagnetSensor;
	private NfcSensor mNfcSensor;
	private int mVolumeKeysMode;

	public void setCardboardView(CardboardView cardboardView) {
		this.mCardboardView = cardboardView;
		if (cardboardView != null) {
			CardboardDeviceParams cardboardDeviceParams = this.mNfcSensor
					.getCardboardDeviceParams();
			if (cardboardDeviceParams == null) {
				cardboardDeviceParams = new CardboardDeviceParams();
			}
			cardboardView.updateCardboardDeviceParams(cardboardDeviceParams);
		}
	}

	public CardboardView getCardboardView() {
		return this.mCardboardView;
	}

	public void setVolumeKeysMode(int mode) {
		this.mVolumeKeysMode = mode;
	}

	public int getVolumeKeysMode() {
		return this.mVolumeKeysMode;
	}

	public boolean areVolumeKeysDisabled() {
		switch (this.mVolumeKeysMode) {
		case 0:
			return false;
		case 2:
			return isDeviceInCardboard();
		case 1:
			return true;
		}
		throw new IllegalStateException("Invalid volume keys mode "
				+ this.mVolumeKeysMode);
	}

	public boolean isDeviceInCardboard() {
		return this.mNfcSensor.isDeviceInCardboard();
	}

	public void onInsertedIntoCardboard(CardboardDeviceParams deviceParams) {
		if (this.mCardboardView != null) {
			this.mCardboardView.updateCardboardDeviceParams(deviceParams);
		}
	}

	public void onRemovedFromCardboard() {
	}

	public void onCardboardTrigger() {
	}

	protected void onNfcIntent(Intent intent) {
		this.mNfcSensor.onNfcIntent(intent);
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(1);

		getWindow().addFlags(128);

		this.mMagnetSensor = new MagnetSensor(this);
		this.mMagnetSensor.setOnCardboardTriggerListener(this);

		this.mNfcSensor = NfcSensor.getInstance(this);
		this.mNfcSensor.addOnCardboardNfcListener(this);

		onNfcIntent(getIntent());

		setVolumeKeysMode(2);
		if (Build.VERSION.SDK_INT < 19) {
			final Handler handler = new Handler();
			getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(
					new View.OnSystemUiVisibilityChangeListener() {
						public void onSystemUiVisibilityChange(int visibility) {
							if ((visibility & 0x2) == 0) {
								handler.postDelayed(new Runnable() {
									public void run() {
										CardboardActivity.this
												.setFullscreenMode();
									}
								}, 2000L);
							}
						}
					});
		}
	}

	protected void onResume() {
		super.onResume();
		if (this.mCardboardView != null) {
			this.mCardboardView.onResume();
		}
		this.mMagnetSensor.start();
		this.mNfcSensor.onResume(this);
	}

	protected void onPause() {
		super.onPause();
		if (this.mCardboardView != null) {
			this.mCardboardView.onPause();
		}
		this.mMagnetSensor.stop();
		this.mNfcSensor.onPause(this);
	}

	protected void onDestroy() {
		this.mNfcSensor.removeOnCardboardNfcListener(this);
		super.onDestroy();
	}

	public void setContentView(View view) {
		if ((view instanceof CardboardView)) {
			setCardboardView((CardboardView) view);
		}
		super.setContentView(view);
	}

	public void setContentView(View view, ViewGroup.LayoutParams params) {
		if ((view instanceof CardboardView)) {
			setCardboardView((CardboardView) view);
		}
		super.setContentView(view, params);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (((keyCode == 24) || (keyCode == 25)) && (areVolumeKeysDisabled())) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (((keyCode == 24) || (keyCode == 25)) && (areVolumeKeysDisabled())) {
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			setFullscreenMode();
		}
	}

	private void setFullscreenMode() {
		getWindow().getDecorView().setSystemUiVisibility(5894);
	}

	public static class VolumeKeys {
		public static final int NOT_DISABLED = 0;
		public static final int DISABLED = 1;
		public static final int DISABLED_WHILE_IN_CARDBOARD = 2;
	}
}
