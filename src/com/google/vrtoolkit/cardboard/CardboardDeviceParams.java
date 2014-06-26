package com.google.vrtoolkit.cardboard;

import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.util.Log;
import java.util.List;

public class CardboardDeviceParams {
	private static final String TAG = "CardboardDeviceParams";
	private static final String DEFAULT_VENDOR = "com.google";
	private static final String DEFAULT_MODEL = "cardboard";
	private static final String DEFAULT_VERSION = "1.0";
	private static final float DEFAULT_INTERPUPILLARY_DISTANCE = 0.06F;
	private static final float DEFAULT_VERTICAL_DISTANCE_TO_LENS_CENTER = 0.035F;
	private static final float DEFAULT_LENS_DIAMETER = 0.025F;
	private static final float DEFAULT_SCREEN_TO_LENS_DISTANCE = 0.037F;
	private static final float DEFAULT_EYE_TO_LENS_DISTANCE = 0.011F;
	private static final float DEFAULT_VISIBLE_VIEWPORT_MAX_SIZE = 0.06F;
	private static final float DEFAULT_FOV_Y = 65.0F;
	private NdefMessage mNfcTagContents;
	private String mVendor;
	private String mModel;
	private String mVersion;
	private float mInterpupillaryDistance;
	private float mVerticalDistanceToLensCenter;
	private float mLensDiameter;
	private float mScreenToLensDistance;
	private float mEyeToLensDistance;
	private float mVisibleViewportSize;
	private float mFovY;
	private Distortion mDistortion;

	public CardboardDeviceParams() {
		this.mVendor = "com.google";
		this.mModel = "cardboard";
		this.mVersion = "1.0";

		this.mInterpupillaryDistance = 0.06F;
		this.mVerticalDistanceToLensCenter = 0.035F;
		this.mLensDiameter = 0.025F;
		this.mScreenToLensDistance = 0.037F;
		this.mEyeToLensDistance = 0.011F;

		this.mVisibleViewportSize = 0.06F;
		this.mFovY = 65.0F;

		this.mDistortion = new Distortion();
	}

	public CardboardDeviceParams(CardboardDeviceParams params) {
		this.mNfcTagContents = params.mNfcTagContents;

		this.mVendor = params.mVendor;
		this.mModel = params.mModel;
		this.mVersion = params.mVersion;

		this.mInterpupillaryDistance = params.mInterpupillaryDistance;
		this.mVerticalDistanceToLensCenter = params.mVerticalDistanceToLensCenter;
		this.mLensDiameter = params.mLensDiameter;
		this.mScreenToLensDistance = params.mScreenToLensDistance;
		this.mEyeToLensDistance = params.mEyeToLensDistance;

		this.mVisibleViewportSize = params.mVisibleViewportSize;
		this.mFovY = params.mFovY;

		this.mDistortion = new Distortion(params.mDistortion);
	}

	public static CardboardDeviceParams createFromNfcContents(
			NdefMessage tagContents) {
		if (tagContents == null) {
			Log.w("CardboardDeviceParams",
					"Could not get contents from NFC tag.");
			return null;
		}
		CardboardDeviceParams deviceParams = new CardboardDeviceParams();
		for (NdefRecord record : tagContents.getRecords()) {
			if (deviceParams.parseNfcUri(record)) {
				break;
			}
		}
		return deviceParams;
	}

	public NdefMessage getNfcTagContents() {
		return this.mNfcTagContents;
	}

	public void setVendor(String vendor) {
		this.mVendor = vendor;
	}

	public String getVendor() {
		return this.mVendor;
	}

	public void setModel(String model) {
		this.mModel = model;
	}

	public String getModel() {
		return this.mModel;
	}

	public void setVersion(String version) {
		this.mVersion = version;
	}

	public String getVersion() {
		return this.mVersion;
	}

	public void setInterpupillaryDistance(float interpupillaryDistance) {
		this.mInterpupillaryDistance = interpupillaryDistance;
	}

	public float getInterpupillaryDistance() {
		return this.mInterpupillaryDistance;
	}

	public void setVerticalDistanceToLensCenter(
			float verticalDistanceToLensCenter) {
		this.mVerticalDistanceToLensCenter = verticalDistanceToLensCenter;
	}

	public float getVerticalDistanceToLensCenter() {
		return this.mVerticalDistanceToLensCenter;
	}

	public void setVisibleViewportSize(float visibleViewportSize) {
		this.mVisibleViewportSize = visibleViewportSize;
	}

	public float getVisibleViewportSize() {
		return this.mVisibleViewportSize;
	}

	public void setFovY(float fovY) {
		this.mFovY = fovY;
	}

	public float getFovY() {
		return this.mFovY;
	}

	public void setLensDiameter(float lensDiameter) {
		this.mLensDiameter = lensDiameter;
	}

	public float getLensDiameter() {
		return this.mLensDiameter;
	}

	public void setScreenToLensDistance(float screenToLensDistance) {
		this.mScreenToLensDistance = screenToLensDistance;
	}

	public float getScreenToLensDistance() {
		return this.mScreenToLensDistance;
	}

	public void setEyeToLensDistance(float eyeToLensDistance) {
		this.mEyeToLensDistance = eyeToLensDistance;
	}

	public float getEyeToLensDistance() {
		return this.mEyeToLensDistance;
	}

	public Distortion getDistortion() {
		return this.mDistortion;
	}

	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (other == this) {
			return true;
		}
		if (!(other instanceof CardboardDeviceParams)) {
			return false;
		}
		CardboardDeviceParams o = (CardboardDeviceParams) other;

		return (this.mVendor == o.mVendor)
				&& (this.mModel == o.mModel)
				&& (this.mVersion == o.mVersion)
				&& (this.mInterpupillaryDistance == o.mInterpupillaryDistance)
				&& (this.mVerticalDistanceToLensCenter == o.mVerticalDistanceToLensCenter)
				&& (this.mLensDiameter == o.mLensDiameter)
				&& (this.mScreenToLensDistance == o.mScreenToLensDistance)
				&& (this.mEyeToLensDistance == o.mEyeToLensDistance)
				&& (this.mVisibleViewportSize == o.mVisibleViewportSize)
				&& (this.mFovY == o.mFovY)
				&& (this.mDistortion.equals(o.mDistortion));
	}

	private boolean parseNfcUri(NdefRecord record) {
		Uri uri = record.toUri();
		if (uri == null) {
			return false;
		}
		if (uri.getHost().equals("v1.0.0")) {
			this.mVendor = "com.google";
			this.mModel = "cardboard";
			this.mVersion = "1.0";
			return true;
		}
		List<String> segments = uri.getPathSegments();
		if (segments.size() != 2) {
			return false;
		}
		this.mVendor = uri.getHost();
		this.mModel = ((String) segments.get(0));
		this.mVersion = ((String) segments.get(1));

		return true;
	}
}
