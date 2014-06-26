package com.google.vrtoolkit.cardboard.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import java.util.ArrayList;

public class MagnetSensor {
	private TriggerDetector mDetector;
	private Thread mDetectorThread;

	public MagnetSensor(Context context) {
		this.mDetector = new TriggerDetector(context);
	}

	public void start() {
		this.mDetectorThread = new Thread(this.mDetector);
		this.mDetectorThread.start();
	}

	public void stop() {
		if (this.mDetectorThread != null) {
			this.mDetectorThread.interrupt();
			this.mDetector.stop();
		}
	}

	public void setOnCardboardTriggerListener(
			OnCardboardTriggerListener listener) {
		this.mDetector.setOnCardboardTriggerListener(listener, new Handler());
	}

	private static class TriggerDetector implements Runnable,
			SensorEventListener {
		private static final String TAG = "TriggerDetector";
		private static final int SEGMENT_SIZE = 20;
		private static final int NUM_SEGMENTS = 2;
		private static final int WINDOW_SIZE = 40;
		private static final int T1 = 30;
		private static final int T2 = 130;
		private SensorManager mSensorManager;
		private Sensor mMagnetometer;
		private ArrayList<float[]> mSensorData;
		private float[] mOffsets = new float[20];
		private MagnetSensor.OnCardboardTriggerListener mListener;
		private Handler mHandler;

		public TriggerDetector(Context context) {
			this.mSensorData = new ArrayList();
			this.mSensorManager = ((SensorManager) context
					.getSystemService("sensor"));
			this.mMagnetometer = this.mSensorManager.getDefaultSensor(2);
		}

		public synchronized void setOnCardboardTriggerListener(
				MagnetSensor.OnCardboardTriggerListener listener,
				Handler handler) {
			this.mListener = listener;
			this.mHandler = handler;
		}

		private void addData(float[] values, long time) {
			if (this.mSensorData.size() > 40) {
				this.mSensorData.remove(0);
			}
			this.mSensorData.add(values);

			evaluateModel();
		}

		private void evaluateModel() {
			if (this.mSensorData.size() < 40) {
				return;
			}
			float[] means = new float[2];
			float[] maximums = new float[2];
			float[] minimums = new float[2];

			float[] baseline = (float[]) this.mSensorData.get(this.mSensorData
					.size() - 1);
			for (int i = 0; i < 2; i++) {
				int segmentStart = 20 * i;

				float[] mOffsets = computeOffsets(segmentStart, baseline);

				means[i] = computeMean(mOffsets);
				maximums[i] = computeMaximum(mOffsets);
				minimums[i] = computeMinimum(mOffsets);
			}
			float min1 = minimums[0];
			float max2 = maximums[1];
			if ((min1 < 30.0F) && (max2 > 130.0F)) {
				handleButtonPressed();
			}
		}

		private void handleButtonPressed() {
			this.mSensorData.clear();
			synchronized (this) {
				if (this.mListener != null) {
					this.mHandler.post(new Runnable() {
						public void run() {
							MagnetSensor.TriggerDetector.this.mListener
									.onCardboardTrigger();
						}
					});
				}
			}
		}

		private float[] computeOffsets(int start, float[] baseline) {
			for (int i = 0; i < 20; i++) {
				float[] point = (float[]) this.mSensorData.get(start + i);
				float[] o = { point[0] - baseline[0], point[1] - baseline[1],
						point[2] - baseline[2] };
				float magnitude = (float) Math.sqrt(o[0] * o[0] + o[1] * o[1]
						+ o[2] * o[2]);
				this.mOffsets[i] = magnitude;
			}
			return this.mOffsets;
		}

		private float computeMean(float[] offsets) {
			float sum = 0.0F;
			for (float o : offsets) {
				sum += o;
			}
			return sum / offsets.length;
		}

		private float computeMaximum(float[] offsets) {
			float max = (-1.0F / 0.0F);
			for (float o : offsets) {
				max = Math.max(o, max);
			}
			return max;
		}

		private float computeMinimum(float[] offsets) {
			float min = (1.0F / 0.0F);
			for (float o : offsets) {
				min = Math.min(o, min);
			}
			return min;
		}

		public void run() {
			Process.setThreadPriority(-19);
			Looper.prepare();
			this.mSensorManager.registerListener(this, this.mMagnetometer, 0);
			Looper.loop();
		}

		public void stop() {
			this.mSensorManager.unregisterListener(this);
		}

		public void onSensorChanged(SensorEvent event) {
			if (event.sensor.equals(this.mMagnetometer)) {
				float[] values = event.values;
				if ((values[0] == 0.0F) && (values[1] == 0.0F)
						&& (values[2] == 0.0F)) {
					return;
				}
				addData((float[]) event.values.clone(), event.timestamp);
			}
		}

		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	}

	public static abstract interface OnCardboardTriggerListener {
		public abstract void onCardboardTrigger();
	}
}
