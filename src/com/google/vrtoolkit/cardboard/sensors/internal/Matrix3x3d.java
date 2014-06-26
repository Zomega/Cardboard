package com.google.vrtoolkit.cardboard.sensors.internal;

public class Matrix3x3d {
	public double[] m = new double[9];

	public Matrix3x3d() {
	}

	public Matrix3x3d(double m00, double m01, double m02, double m10,
			double m11, double m12, double m20, double m21, double m22) {
		this.m[0] = m00;
		this.m[1] = m01;
		this.m[2] = m02;
		this.m[3] = m10;
		this.m[4] = m11;
		this.m[5] = m12;
		this.m[6] = m20;
		this.m[7] = m21;
		this.m[8] = m22;
	}

	public Matrix3x3d(Matrix3x3d o) {
		this.m[0] = o.m[0];
		this.m[1] = o.m[1];
		this.m[2] = o.m[2];
		this.m[3] = o.m[3];
		this.m[4] = o.m[4];
		this.m[5] = o.m[5];
		this.m[6] = o.m[6];
		this.m[7] = o.m[7];
		this.m[8] = o.m[8];
	}

	public void set(double m00, double m01, double m02, double m10, double m11,
			double m12, double m20, double m21, double m22) {
		this.m[0] = m00;
		this.m[1] = m01;
		this.m[2] = m02;
		this.m[3] = m10;
		this.m[4] = m11;
		this.m[5] = m12;
		this.m[6] = m20;
		this.m[7] = m21;
		this.m[8] = m22;
	}

	public void set(Matrix3x3d o) {
		this.m[0] = o.m[0];
		this.m[1] = o.m[1];
		this.m[2] = o.m[2];
		this.m[3] = o.m[3];
		this.m[4] = o.m[4];
		this.m[5] = o.m[5];
		this.m[6] = o.m[6];
		this.m[7] = o.m[7];
		this.m[8] = o.m[8];
	}

	public void setZero() {
		double tmp63_62 = (this.m[2] = this.m[3] = this.m[4] = this.m[5] = this.m[6] = this.m[7] = this.m[8] = 0.0D);
		this.m[1] = tmp63_62;
		this.m[0] = tmp63_62;
	}

	public void setIdentity() {
		double tmp41_40 = (this.m[3] = this.m[5] = this.m[6] = this.m[7] = 0.0D);
		this.m[2] = tmp41_40;
		this.m[1] = tmp41_40;
		double tmp63_62 = (this.m[8] = 1.0D);
		this.m[4] = tmp63_62;
		this.m[0] = tmp63_62;
	}

	public void setSameDiagonal(double d) {
		double tmp19_18 = (this.m[8] = d);
		this.m[4] = tmp19_18;
		this.m[0] = tmp19_18;
	}

	public double get(int row, int col) {
		return this.m[(3 * row + col)];
	}

	public void set(int row, int col, double value) {
		this.m[(3 * row + col)] = value;
	}

	public void getColumn(int col, Vector3d v) {
		v.x = this.m[col];
		v.y = this.m[(col + 3)];
		v.z = this.m[(col + 6)];
	}

	public void setColumn(int col, Vector3d v) {
		this.m[col] = v.x;
		this.m[(col + 3)] = v.y;
		this.m[(col + 6)] = v.z;
	}

	public void scale(double s) {
		this.m[0] *= s;
		this.m[1] *= s;
		this.m[2] *= s;
		this.m[3] *= s;
		this.m[4] *= s;
		this.m[5] *= s;
		this.m[6] *= s;
		this.m[7] *= s;
		this.m[8] *= s;
	}

	public void plusEquals(Matrix3x3d b) {
		this.m[0] += b.m[0];
		this.m[1] += b.m[1];
		this.m[2] += b.m[2];
		this.m[3] += b.m[3];
		this.m[4] += b.m[4];
		this.m[5] += b.m[5];
		this.m[6] += b.m[6];
		this.m[7] += b.m[7];
		this.m[8] += b.m[8];
	}

	public void minusEquals(Matrix3x3d b) {
		this.m[0] -= b.m[0];
		this.m[1] -= b.m[1];
		this.m[2] -= b.m[2];
		this.m[3] -= b.m[3];
		this.m[4] -= b.m[4];
		this.m[5] -= b.m[5];
		this.m[6] -= b.m[6];
		this.m[7] -= b.m[7];
		this.m[8] -= b.m[8];
	}

	public void transpose() {
		double tmp = this.m[1];
		this.m[1] = this.m[3];
		this.m[3] = tmp;

		tmp = this.m[2];
		this.m[2] = this.m[6];
		this.m[6] = tmp;

		tmp = this.m[5];
		this.m[5] = this.m[7];
		this.m[7] = tmp;
	}

	public void transpose(Matrix3x3d result) {
		double m1 = this.m[1];
		double m2 = this.m[2];
		double m5 = this.m[5];
		result.m[0] = this.m[0];
		result.m[1] = this.m[3];
		result.m[2] = this.m[6];
		result.m[3] = m1;
		result.m[4] = this.m[4];
		result.m[5] = this.m[7];
		result.m[6] = m2;
		result.m[7] = m5;
		result.m[8] = this.m[8];
	}

	public static void add(Matrix3x3d a, Matrix3x3d b, Matrix3x3d result) {
		a.m[0] += b.m[0];
		a.m[1] += b.m[1];
		a.m[2] += b.m[2];
		a.m[3] += b.m[3];
		a.m[4] += b.m[4];
		a.m[5] += b.m[5];
		a.m[6] += b.m[6];
		a.m[7] += b.m[7];
		a.m[8] += b.m[8];
	}

	public static void mult(Matrix3x3d a, Matrix3x3d b, Matrix3x3d result) {
		result.set(a.m[0] * b.m[0] + a.m[1] * b.m[3] + a.m[2] * b.m[6], a.m[0]
				* b.m[1] + a.m[1] * b.m[4] + a.m[2] * b.m[7], a.m[0] * b.m[2]
				+ a.m[1] * b.m[5] + a.m[2] * b.m[8], a.m[3] * b.m[0] + a.m[4]
				* b.m[3] + a.m[5] * b.m[6], a.m[3] * b.m[1] + a.m[4] * b.m[4]
				+ a.m[5] * b.m[7], a.m[3] * b.m[2] + a.m[4] * b.m[5] + a.m[5]
				* b.m[8], a.m[6] * b.m[0] + a.m[7] * b.m[3] + a.m[8] * b.m[6],
				a.m[6] * b.m[1] + a.m[7] * b.m[4] + a.m[8] * b.m[7], a.m[6]
						* b.m[2] + a.m[7] * b.m[5] + a.m[8] * b.m[8]);
	}

	public static void mult(Matrix3x3d a, Vector3d v, Vector3d result) {
		double x = a.m[0] * v.x + a.m[1] * v.y + a.m[2] * v.z;
		double y = a.m[3] * v.x + a.m[4] * v.y + a.m[5] * v.z;
		double z = a.m[6] * v.x + a.m[7] * v.y + a.m[8] * v.z;
		result.x = x;
		result.y = y;
		result.z = z;
	}

	public double determinant() {
		return get(0, 0) * (get(1, 1) * get(2, 2) - get(2, 1) * get(1, 2))
				- get(0, 1) * (get(1, 0) * get(2, 2) - get(1, 2) * get(2, 0))
				+ get(0, 2) * (get(1, 0) * get(2, 1) - get(1, 1) * get(2, 0));
	}

	public boolean invert(Matrix3x3d result) {
		double d = determinant();
		if (d == 0.0D) {
			return false;
		}
		double invdet = 1.0D / d;

		result.set((this.m[4] * this.m[8] - this.m[7] * this.m[5]) * invdet,
				-(this.m[1] * this.m[8] - this.m[2] * this.m[7]) * invdet,
				(this.m[1] * this.m[5] - this.m[2] * this.m[4]) * invdet,
				-(this.m[3] * this.m[8] - this.m[5] * this.m[6]) * invdet,
				(this.m[0] * this.m[8] - this.m[2] * this.m[6]) * invdet,
				-(this.m[0] * this.m[5] - this.m[3] * this.m[2]) * invdet,
				(this.m[3] * this.m[7] - this.m[6] * this.m[4]) * invdet,
				-(this.m[0] * this.m[7] - this.m[6] * this.m[1]) * invdet,
				(this.m[0] * this.m[4] - this.m[3] * this.m[1]) * invdet);

		return true;
	}
}
