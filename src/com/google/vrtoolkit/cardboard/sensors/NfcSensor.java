package com.google.vrtoolkit.cardboard.sensors;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Handler;
import android.util.Log;
import com.google.vrtoolkit.cardboard.CardboardDeviceParams;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NfcSensor {
	public static final String NFC_DATA_SCHEME = "cardboard";
	public static final String FIRST_TAG_VERSION = "v1.0.0";
	private static final String TAG = "NfcSensor";
	private static final int MAX_CONNECTION_FAILURES = 1;
	private static final long NFC_POLLING_INTERVAL_MS = 250L;
	private static NfcSensor sInstance;
	private final Context mContext;
	private final NfcAdapter mNfcAdapter;
	private final Object mTagLock;
	private final List<ListenerHelper> mListeners;
	private IntentFilter[] mNfcIntentFilters;
	private volatile Ndef mCurrentTag;
	private Timer mNfcDisconnectTimer;
	private int mTagConnectionFailures;

	public static NfcSensor getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new NfcSensor(context);
		}
		return sInstance;
	}

	private NfcSensor(Context context) {
		this.mContext = context.getApplicationContext();
		this.mNfcAdapter = NfcAdapter.getDefaultAdapter(this.mContext);
		this.mListeners = new ArrayList();
		this.mTagLock = new Object();
		if (this.mNfcAdapter == null) {
			return;
		}
		IntentFilter ndefIntentFilter = new IntentFilter(
				"android.nfc.action.NDEF_DISCOVERED");
		ndefIntentFilter.addDataScheme("cardboard");
		this.mNfcIntentFilters = new IntentFilter[] { ndefIntentFilter };

		this.mContext.registerReceiver(new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				NfcSensor.this.onNfcIntent(intent);
			}
		}, ndefIntentFilter);
	}

	public void addOnCardboardNfcListener(OnCardboardNfcListener listener) {
		if (listener == null) {
			return;
		}
		synchronized (this.mListeners) {
			for (ListenerHelper helper : this.mListeners) {
				if (helper.getListener() == listener) {
					return;
				}
			}
			this.mListeners.add(new ListenerHelper(listener, new Handler()));
		}
	}

	public void removeOnCardboardNfcListener(OnCardboardNfcListener listener) {
		if (listener == null) {
			return;
		}
		synchronized (this.mListeners) {
			for (ListenerHelper helper : this.mListeners) {
				if (helper.getListener() == listener) {
					this.mListeners.remove(helper);
					return;
				}
			}
		}
	}

	public boolean isNfcSupported() {
		return this.mNfcAdapter != null;
	}

	public boolean isNfcEnabled() {
		return (isNfcSupported()) && (this.mNfcAdapter.isEnabled());
	}

	public boolean isDeviceInCardboard() {
		return this.mCurrentTag != null;
	}

	public CardboardDeviceParams getCardboardDeviceParams() {
		NdefMessage tagContents = null;
		synchronized (this.mTagLock) {
			try {
				tagContents = this.mCurrentTag.getCachedNdefMessage();
			} catch (Exception e) {
				return null;
			}
		}
		if (tagContents == null) {
			return null;
		}
		return CardboardDeviceParams.createFromNfcContents(tagContents);
	}

	public void onResume(Activity activity) {
		if (!isNfcEnabled()) {
			return;
		}
		Intent intent = new Intent("android.nfc.action.NDEF_DISCOVERED");
		intent.setPackage(activity.getPackageName());

		PendingIntent pendingIntent = PendingIntent.getBroadcast(this.mContext,
				0, intent, 0);
		this.mNfcAdapter.enableForegroundDispatch(activity, pendingIntent,
				this.mNfcIntentFilters, (String[][]) null);
	}

	public void onPause(Activity activity) {
		if (!isNfcEnabled()) {
			return;
		}
		this.mNfcAdapter.disableForegroundDispatch(activity);
	}

	public void onNfcIntent(Intent intent) {
		if ((!isNfcEnabled())
				|| (intent == null)
				|| (!"android.nfc.action.NDEF_DISCOVERED".equals(intent
						.getAction()))) {
			return;
		}
		Uri uri = intent.getData();
		Tag nfcTag = (Tag) intent.getParcelableExtra("android.nfc.extra.TAG");
		if ((uri == null) || (nfcTag == null)) {
			return;
		}
		Ndef ndef = Ndef.get(nfcTag);
		if ((ndef == null)
				|| (!uri.getScheme().equals("cardboard"))
				|| ((!uri.getHost().equals("v1.0.0")) && (uri.getPathSegments()
						.size() == 2))) {
			return;
		}
		synchronized (this.mTagLock) {
			boolean isSameTag = false;
			if (this.mCurrentTag != null) {
				byte[] tagId1 = nfcTag.getId();
				byte[] tagId2 = this.mCurrentTag.getTag().getId();
				isSameTag = (tagId1 != null) && (tagId2 != null)
						&& (Arrays.equals(tagId1, tagId2));

				closeCurrentNfcTag();
				if (!isSameTag) {
					sendDisconnectionEvent();
				}
			}
			NdefMessage nfcTagContents;
			try {
				ndef.connect();
				nfcTagContents = ndef.getCachedNdefMessage();
			} catch (Exception e) {
				Log.e("NfcSensor", "Error reading NFC tag: " + e.toString());
				if (isSameTag) {
					sendDisconnectionEvent();
				}
				return;
			}
			this.mCurrentTag = ndef;
			if (!isSameTag) {
				synchronized (this.mListeners) {
					for (ListenerHelper listener : this.mListeners) {
						listener.onInsertedIntoCardboard(CardboardDeviceParams
								.createFromNfcContents(nfcTagContents));
					}
				}
			}
			this.mTagConnectionFailures = 0;
			this.mNfcDisconnectTimer = new Timer("NFC disconnect timer");
			this.mNfcDisconnectTimer.schedule(new TimerTask() {
				public void run() {
					synchronized (NfcSensor.this.mTagLock) {
						if (!NfcSensor.this.mCurrentTag.isConnected()) {
							NfcSensor.access$204(NfcSensor.this);
							if (NfcSensor.this.mTagConnectionFailures > 1) {
								NfcSensor.this.closeCurrentNfcTag();
								NfcSensor.this.sendDisconnectionEvent();
							}
						}
					}
				}
			}, 250L, 250L);
		}
	}

	private void closeCurrentNfcTag() {
		if (this.mNfcDisconnectTimer != null) {
			this.mNfcDisconnectTimer.cancel();
		}
		try {
			this.mCurrentTag.close();
		} catch (IOException e) {
			Log.w("NfcSensor", e.toString());
		}
		this.mCurrentTag = null;
	}

	private void sendDisconnectionEvent() {
		synchronized (this.mListeners) {
			for (ListenerHelper listener : this.mListeners) {
				listener.onRemovedFromCardboard();
			}
		}
	}

	private static class ListenerHelper implements
			NfcSensor.OnCardboardNfcListener {
		private NfcSensor.OnCardboardNfcListener mListener;
		private Handler mHandler;

		public ListenerHelper(NfcSensor.OnCardboardNfcListener listener,
				Handler handler) {
			this.mListener = listener;
			this.mHandler = handler;
		}

		public NfcSensor.OnCardboardNfcListener getListener() {
			return this.mListener;
		}

		public void onInsertedIntoCardboard(
				final CardboardDeviceParams deviceParams) {
			this.mHandler.post(new Runnable() {
				public void run() {
					NfcSensor.ListenerHelper.this.mListener
							.onInsertedIntoCardboard(deviceParams);
				}
			});
		}

		public void onRemovedFromCardboard() {
			this.mHandler.post(new Runnable() {
				public void run() {
					NfcSensor.ListenerHelper.this.mListener
							.onRemovedFromCardboard();
				}
			});
		}
	}

	public static abstract interface OnCardboardNfcListener {
		public abstract void onInsertedIntoCardboard(
				CardboardDeviceParams paramCardboardDeviceParams);

		public abstract void onRemovedFromCardboard();
	}
}
