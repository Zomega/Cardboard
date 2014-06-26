package com.google.vrtoolkit.cardboard;

import android.opengl.Matrix;
import android.util.FloatMath;

public class HeadTransform {
	private static final float GIMBAL_LOCK_EPSILON = 0.01F;
	private static final float PI = 3.1415927F;
	private final float[] mHeadView;

	public HeadTransform() {
		this.mHeadView = new float[16];
		Matrix.setIdentityM(this.mHeadView, 0);
	}

	float[] getHeadView() {
		return this.mHeadView;
	}

	public void getHeadView(float[] headView, int offset) {
		if (offset + 16 > headView.length) {
			throw new IllegalArgumentException(
					"Not enough space to write the result");
		}
		System.arraycopy(this.mHeadView, 0, headView, offset, 16);
	}

	public void getTranslation(float[] translation, int offset) {
		if (offset + 3 > translation.length) {
			throw new IllegalArgumentException(
					"Not enough space to write the result");
		}
		for (int i = 0; i < 3; i++) {
			translation[(i + offset)] = this.mHeadView[(12 + i)];
		}
	}

	public void getForwardVector(float[] forward, int offset) {
		if (offset + 3 > forward.length) {
			throw new IllegalArgumentException(
					"Not enough space to write the result");
		}
		for (int i = 0; i < 3; i++) {
			forward[(i + offset)] = (-this.mHeadView[(8 + i)]);
		}
	}

	public void getUpVector(float[] up, int offset) {
		if (offset + 3 > up.length) {
			throw new IllegalArgumentException(
					"Not enough space to write the result");
		}
		for (int i = 0; i < 3; i++) {
			up[(i + offset)] = this.mHeadView[(4 + i)];
		}
	}

	public void getRightVector(float[] right, int offset) {
		if (offset + 3 > right.length) {
			throw new IllegalArgumentException(
					"Not enough space to write the result");
		}
		for (int i = 0; i < 3; i++) {
			right[(i + offset)] = this.mHeadView[i];
		}
	}

	public void getQuaternion(float[] quaternion, int offset) {
		if (offset + 4 > quaternion.length) {
			throw new IllegalArgumentException(
					"Not enough space to write the result");
		}
		float[] m = this.mHeadView;
		float t = m[0] + m[5] + m[10];
		float x;
		float y;
		float z;
		float w;
		float s;
		if (t >= 0.0F) {
			s = FloatMath.sqrt(t + 1.0F);
			w = 0.5F * s;
			s = 0.5F / s;
			x = (m[9] - m[6]) * s;
			y = (m[2] - m[8]) * s;
			z = (m[4] - m[1]) * s;
		} else {
			if ((m[0] > m[5]) && (m[0] > m[10])) {
				s = FloatMath.sqrt(1.0F + m[0] - m[5] - m[10]);
				x = s * 0.5F;
				s = 0.5F / s;
				y = (m[4] + m[1]) * s;
				z = (m[2] + m[8]) * s;
				w = (m[9] - m[6]) * s;
			} else {
				if (m[5] > m[10]) {
					s = FloatMath.sqrt(1.0F + m[5] - m[0] - m[10]);
					y = s * 0.5F;
					s = 0.5F / s;
					x = (m[4] + m[1]) * s;
					z = (m[9] + m[6]) * s;
					w = (m[2] - m[8]) * s;
				} else {
					s = FloatMath.sqrt(1.0F + m[10] - m[0] - m[5]);
					z = s * 0.5F;
					s = 0.5F / s;
					x = (m[2] + m[8]) * s;
					y = (m[9] + m[6]) * s;
					w = (m[4] - m[1]) * s;
				}
			}
		}
		quaternion[(offset + 0)] = x;
		quaternion[(offset + 1)] = y;
		quaternion[(offset + 2)] = z;
		quaternion[(offset + 3)] = w;
	}

	public void getEulerAngles(float[] eulerAngles, int offset) {
		if (offset + 3 > eulerAngles.length) {
			throw new IllegalArgumentException(
					"Not enough space to write the result");
		}
		float pitch = (float) Math.asin(this.mHeadView[6]);
		float roll;
		float yaw;
		if (FloatMath.sqrt(1.0F - this.mHeadView[6] * this.mHeadView[6]) >= 0.01F) {
			yaw = (float) Math.atan2(-this.mHeadView[2],
					this.mHeadView[10]);
			roll = (float) Math.atan2(-this.mHeadView[4], this.mHeadView[5]);
		} else {
			yaw = 0.0F;
			roll = (float) Math.atan2(this.mHeadView[1], this.mHeadView[0]);
		}
		eulerAngles[(offset + 0)] = (-pitch);
		eulerAngles[(offset + 1)] = (-yaw);
		eulerAngles[(offset + 2)] = (-roll);
	}
}
