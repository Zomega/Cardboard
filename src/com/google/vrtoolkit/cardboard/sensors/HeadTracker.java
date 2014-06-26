package com.google.vrtoolkit.cardboard.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.Looper;
import com.google.vrtoolkit.cardboard.sensors.internal.OrientationEKF;

public class HeadTracker {
	private static final String TAG = "HeadTracker";
	private static final double NS2S = 1.0E-9D;
	private static final int[] INPUT_SENSORS = { 1, 4 };
	private final Context mContext;
	private final float[] mEkfToHeadTracker = new float[16];
	private final float[] mTmpHeadView = new float[16];
	private final float[] mTmpRotatedEvent = new float[3];
	private Looper mSensorLooper;
	private SensorEventListener mSensorEventListener;
	private volatile boolean mTracking;
	private final OrientationEKF mTracker = new OrientationEKF();
	private long mLastGyroEventTimeNanos;

	public HeadTracker(Context context) {
		this.mContext = context;
		Matrix.setRotateEulerM(this.mEkfToHeadTracker, 0, -90.0F, 0.0F, 0.0F);
	}

	public void startTracking() {
		if (this.mTracking) {
			return;
		}
		this.mTracker.reset();

		this.mSensorEventListener = new SensorEventListener() {
			public void onSensorChanged(SensorEvent event) {
				HeadTracker.this.processSensorEvent(event);
			}

			public void onAccuracyChanged(Sensor sensor, int accuracy) {
			}
		};
		Thread sensorThread = new Thread(new Runnable() {
			public void run() {
				Looper.prepare();

				HeadTracker.this.mSensorLooper = Looper.myLooper();
				Handler handler = new Handler();

				SensorManager sensorManager = (SensorManager) HeadTracker.this.mContext
						.getSystemService("sensor");
				for (int sensorType : HeadTracker.INPUT_SENSORS) {
					Sensor sensor = sensorManager.getDefaultSensor(sensorType);
					sensorManager.registerListener(
							HeadTracker.this.mSensorEventListener, sensor, 0,
							handler);
				}
				Looper.loop();
			}
		});
		sensorThread.start();
		this.mTracking = true;
	}

	public void stopTracking() {
		if (!this.mTracking) {
			return;
		}
		SensorManager sensorManager = (SensorManager) this.mContext
				.getSystemService("sensor");

		sensorManager.unregisterListener(this.mSensorEventListener);
		this.mSensorEventListener = null;

		this.mSensorLooper.quit();
		this.mSensorLooper = null;
		this.mTracking = false;
	}

	public void getLastHeadView(float[] headView, int offset) {
		if (offset + 16 > headView.length) {
			throw new IllegalArgumentException(
					"Not enough space to write the result");
		}
		synchronized (this.mTracker) {
			double secondsSinceLastGyroEvent = (System.nanoTime() - this.mLastGyroEventTimeNanos) * 1.0E-9D;

			double secondsToPredictForward = secondsSinceLastGyroEvent + 0.03333333333333333D;
			double[] mat = this.mTracker
					.getPredictedGLMatrix(secondsToPredictForward);
			for (int i = 0; i < headView.length; i++) {
				this.mTmpHeadView[i] = ((float) mat[i]);
			}
		}
		Matrix.multiplyMM(headView, offset, this.mTmpHeadView, 0,
				this.mEkfToHeadTracker, 0);
	}

	private void processSensorEvent(SensorEvent event) {
		long timeNanos = System.nanoTime();

		this.mTmpRotatedEvent[0] = (-event.values[1]);
		this.mTmpRotatedEvent[1] = event.values[0];
		this.mTmpRotatedEvent[2] = event.values[2];
		synchronized (this.mTracker) {
			if (event.sensor.getType() == 1) {
				this.mTracker
						.processAcc(this.mTmpRotatedEvent, event.timestamp);
			} else if (event.sensor.getType() == 4) {
				this.mLastGyroEventTimeNanos = timeNanos;
				this.mTracker.processGyro(this.mTmpRotatedEvent,
						event.timestamp);
			}
		}
	}
}
