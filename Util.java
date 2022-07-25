package src;

import java.util.Arrays;

class Util {
	// vectors
	static double[] add(double a[], double b[]) {
		if (a.length != b.length) {
			throw new IllegalArgumentException("Arrays must have the same length.");
		}
		double c[] = new double[a.length];
		for (int i = 0; i < a.length; i++) {
			c[i] = a[i] + b[i];
		}
		return c;
	}

	static double dot(double a[], double b[]) {
		if (a.length != b.length) {
			throw new IllegalArgumentException("Arrays must have the same length.");
		}
		double c = 0;
		for (int i = 0; i < a.length; i++) {
			c += a[i] * b[i];
		}
		return c;
	}

	static double getMag(double v[]) {
		double mag = 0;
		for (double i : v) {
			mag += i * i;
		}
		return Math.sqrt(mag);
	}

	// testing
	public static void main(String args[]) {
		long startTime = System.currentTimeMillis();
		NeuralNetwork nn = new NeuralNetwork(2, new int[] { 3 }, 3, 0.1);
		Util.println(Arrays.toString(nn.feedForward(new double[] { 1, 1 })));
		long endTime = System.currentTimeMillis();
		long duration = (endTime - startTime);
		System.out.println(duration);
	}

	static double[] multiply(double a[], double scalor) {
		double b[] = new double[a.length];
		for (int i = 0; i < a.length; i++) {
			b[i] = a[i] * scalor;
		}
		return b;
	}

	static String percent(double numerator, double denominator, int decimal) {
		return Double.toString(Math.floor(numerator / denominator * Math.pow(10, decimal)) / Math.pow(10, decimal));
	}

	static void print(Object obj) {
		System.out.print(obj);
	}

	static void println(Object obj) {
		System.out.println(obj);
	}

	static void setMag(double v[], double mag) {
		double ratio = 0;
		for (double i : v) {
			ratio += i * i;
		}
		ratio = mag / Math.sqrt(ratio);
		for (int j = 0; j < v.length; j++) {
			v[j] *= ratio;
		}
	}

	// miscellaneous
	static double sigmoid(double x) {
		return 1 / (1 + Math.exp(-x));
	}

	static double[] sub(double a[], double b[]) {
		if (a.length != b.length) {
			throw new IllegalArgumentException("Arrays must have the same length.");
		}
		double c[] = new double[a.length];
		for (int i = 0; i < a.length; i++) {
			c[i] = a[i] - b[i];
		}
		return c;
	}
}
