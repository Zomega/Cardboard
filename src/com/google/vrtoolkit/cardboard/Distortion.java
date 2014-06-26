package com.google.vrtoolkit.cardboard;

public class Distortion {
	private static final float[] DEFAULT_COEFFICIENTS = { 250.0F, 50000.0F };
	private float[] mCoefficients;

	public Distortion() {
		this.mCoefficients = new float[2];
		this.mCoefficients[0] = DEFAULT_COEFFICIENTS[0];
		this.mCoefficients[1] = DEFAULT_COEFFICIENTS[1];
	}

	public Distortion(Distortion other) {
		this.mCoefficients = new float[2];
		this.mCoefficients[0] = other.mCoefficients[0];
		this.mCoefficients[1] = other.mCoefficients[1];
	}

	public void setCoefficients(float[] coefficients) {
		this.mCoefficients[0] = coefficients[0];
		this.mCoefficients[1] = coefficients[1];
	}

	public float[] getCoefficients() {
		return this.mCoefficients;
	}

	public float distortionFactor(float radius) {
		float rSq = radius * radius;
		return 1.0F + this.mCoefficients[0] * rSq + this.mCoefficients[1] * rSq
				* rSq;
	}

	public float distort(float radius) {
		return radius * distortionFactor(radius);
	}

	public float distortInverse(float radius) {
		float r0 = radius / 0.9F;
		float r1 = radius * 0.9F;

		float dr0 = radius - distort(r0);
		while (Math.abs(r1 - r0) > 1.0E-4D) {
			float dr1 = radius - distort(r1);
			float r2 = r1 - dr1 * ((r1 - r0) / (dr1 - dr0));
			r0 = r1;
			r1 = r2;
			dr0 = dr1;
		}
		return r1;
	}

	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (other == this) {
			return true;
		}
		if (!(other instanceof Distortion)) {
			return false;
		}
		Distortion o = (Distortion) other;
		return (this.mCoefficients[0] == o.mCoefficients[0])
				&& (this.mCoefficients[1] == o.mCoefficients[1]);
	}

	public String toString() {
		return "Distortion {" + this.mCoefficients[0] + ", "
				+ this.mCoefficients[1] + "}";
	}
}
