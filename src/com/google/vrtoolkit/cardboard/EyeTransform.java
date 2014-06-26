package com.google.vrtoolkit.cardboard;

import android.opengl.Matrix;

public class EyeTransform {
	private final EyeParams mEyeParams;
	private final float[] mEyeView;
	private final float[] mPerspective;

	public EyeTransform(EyeParams params) {
		this.mEyeParams = params;
		this.mEyeView = new float[16];
		this.mPerspective = new float[16];

		Matrix.setIdentityM(this.mEyeView, 0);
		Matrix.setIdentityM(this.mPerspective, 0);
	}

	public float[] getEyeView() {
		return this.mEyeView;
	}

	public float[] getPerspective() {
		return this.mPerspective;
	}

	public EyeParams getParams() {
		return this.mEyeParams;
	}
}
