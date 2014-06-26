package com.google.vrtoolkit.cardboard;

public class EyeParams {
	private final int mEye;
	private final Viewport mViewport;
	private final FieldOfView mFov;
	private final EyeTransform mEyeTransform;

	public EyeParams(int eye) {
		this.mEye = eye;
		this.mViewport = new Viewport();
		this.mFov = new FieldOfView();
		this.mEyeTransform = new EyeTransform(this);
	}

	public int getEye() {
		return this.mEye;
	}

	public Viewport getViewport() {
		return this.mViewport;
	}

	public FieldOfView getFov() {
		return this.mFov;
	}

	public EyeTransform getTransform() {
		return this.mEyeTransform;
	}

	public static class Eye {
		public static final int MONOCULAR = 0;
		public static final int LEFT = 1;
		public static final int RIGHT = 2;
	}
}
