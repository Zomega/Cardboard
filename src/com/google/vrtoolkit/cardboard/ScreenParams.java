package com.google.vrtoolkit.cardboard;

import android.util.DisplayMetrics;
import android.view.Display;

public class ScreenParams {
	public static final float METERS_PER_INCH = 0.0254F;
	private static final float DEFAULT_BORDER_SIZE_METERS = 0.0030F;
	private int mWidth;
	private int mHeight;
	private float mXMetersPerPixel;
	private float mYMetersPerPixel;
	private float mBorderSizeMeters;

	public ScreenParams(Display display) {
		DisplayMetrics metrics = new DisplayMetrics();
		try {
			display.getRealMetrics(metrics);
		} catch (NoSuchMethodError e) {
			display.getMetrics(metrics);
		}
		this.mXMetersPerPixel = (0.0254F / metrics.xdpi);
		this.mYMetersPerPixel = (0.0254F / metrics.ydpi);
		this.mWidth = metrics.widthPixels;
		this.mHeight = metrics.heightPixels;
		this.mBorderSizeMeters = 0.0030F;
		if (this.mHeight > this.mWidth) {
			int tempPx = this.mWidth;
			this.mWidth = this.mHeight;
			this.mHeight = tempPx;

			float tempMetersPerPixel = this.mXMetersPerPixel;
			this.mXMetersPerPixel = this.mYMetersPerPixel;
			this.mYMetersPerPixel = tempMetersPerPixel;
		}
	}

	public ScreenParams(ScreenParams params) {
		this.mWidth = params.mWidth;
		this.mHeight = params.mHeight;
		this.mXMetersPerPixel = params.mXMetersPerPixel;
		this.mYMetersPerPixel = params.mYMetersPerPixel;
		this.mBorderSizeMeters = params.mBorderSizeMeters;
	}

	public void setWidth(int width) {
		this.mWidth = width;
	}

	public int getWidth() {
		return this.mWidth;
	}

	public void setHeight(int height) {
		this.mHeight = height;
	}

	public int getHeight() {
		return this.mHeight;
	}

	public float getWidthMeters() {
		return this.mWidth * this.mXMetersPerPixel;
	}

	public float getHeightMeters() {
		return this.mHeight * this.mYMetersPerPixel;
	}

	public void setBorderSizeMeters(float screenBorderSize) {
		this.mBorderSizeMeters = screenBorderSize;
	}

	public float getBorderSizeMeters() {
		return this.mBorderSizeMeters;
	}

	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (other == this) {
			return true;
		}
		if (!(other instanceof ScreenParams)) {
			return false;
		}
		ScreenParams o = (ScreenParams) other;

		return (this.mWidth == o.mWidth) && (this.mHeight == o.mHeight)
				&& (this.mXMetersPerPixel == o.mXMetersPerPixel)
				&& (this.mYMetersPerPixel == o.mYMetersPerPixel)
				&& (this.mBorderSizeMeters == o.mBorderSizeMeters);
	}
}
