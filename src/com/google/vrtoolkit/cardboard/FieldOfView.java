package com.google.vrtoolkit.cardboard;

import android.opengl.Matrix;

public class FieldOfView {
	private float mLeft;
	private float mRight;
	private float mBottom;
	private float mTop;

	public FieldOfView() {
	}

	public FieldOfView(float left, float right, float bottom, float top) {
		this.mLeft = left;
		this.mRight = right;
		this.mBottom = bottom;
		this.mTop = top;
	}

	public FieldOfView(FieldOfView other) {
		this.mLeft = other.mLeft;
		this.mRight = other.mRight;
		this.mBottom = other.mBottom;
		this.mTop = other.mTop;
	}

	public void setLeft(float left) {
		this.mLeft = left;
	}

	public float getLeft() {
		return this.mLeft;
	}

	public void setRight(float right) {
		this.mRight = right;
	}

	public float getRight() {
		return this.mRight;
	}

	public void setBottom(float bottom) {
		this.mBottom = bottom;
	}

	public float getBottom() {
		return this.mBottom;
	}

	public void setTop(float top) {
		this.mTop = top;
	}

	public float getTop() {
		return this.mTop;
	}

	public void toPerspectiveMatrix(float near, float far, float[] perspective,
			int offset) {
		if (offset + 16 > perspective.length) {
			throw new IllegalArgumentException(
					"Not enough space to write the result");
		}
		float l = (float) -Math.tan(Math.toRadians(this.mLeft)) * near;
		float r = (float) Math.tan(Math.toRadians(this.mRight)) * near;
		float b = (float) -Math.tan(Math.toRadians(this.mBottom)) * near;
		float t = (float) Math.tan(Math.toRadians(this.mTop)) * near;
		Matrix.frustumM(perspective, offset, l, r, b, t, near, far);
	}

	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (other == this) {
			return true;
		}
		if (!(other instanceof FieldOfView)) {
			return false;
		}
		FieldOfView o = (FieldOfView) other;
		return (this.mLeft == o.mLeft) && (this.mRight == o.mRight)
				&& (this.mBottom == o.mBottom) && (this.mTop == o.mTop);
	}

	public String toString() {
		return "FieldOfView {left:" + this.mLeft + " right:" + this.mRight
				+ " bottom:" + this.mBottom + " top:" + this.mTop + "}";
	}
}
