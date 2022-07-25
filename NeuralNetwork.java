package src;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class NeuralNetwork {
	Matrix biasH[];
	Matrix biasI;
	Matrix biasO;
	int count;
	double learningRate;
	int numH;
	int numI;
	int numO;
	Matrix wH[]; // additional hidden layer weights
	Matrix wI; // input weights
	Matrix wO; // output layer weights

	NeuralNetwork(int numI, int numH, int numO, double lr) {
		this(numI, new int[] { numH }, numO, lr);
	}

	NeuralNetwork(int numI, int numH[], int numO, double lr) {
		this.numI = numI;
		this.numH = numH[0];
		this.numO = numO;
		this.learningRate = lr;
		this.wI = new Matrix(numH[0], numI);
		this.wH = new Matrix[numH.length - 1];
		this.wO = new Matrix(numO, numH[numH.length - 1]);
		this.biasI = new Matrix(numH[0], 1);
		this.biasH = new Matrix[numH.length - 1];
		this.biasO = new Matrix(numO, 1);

		this.wI.randomize();
		this.wO.randomize();
		this.biasI.randomize();
		this.biasO.randomize();

		for (int i = 1; i < numH.length; i++) {
			this.wH[i - 1] = new Matrix(numH[i], numH[i - 1]);
			this.biasH[i - 1] = new Matrix(numH[i], 1);
			this.wH[i - 1].randomize();
			this.biasH[i - 1].randomize();
		}
	}

	double[] feedForward(double inputArray[]) {
		// calculate outputs at first hidden layer
		Matrix input = new Matrix(inputArray);
		Matrix firstHidden = Matrix.product(this.wI, input);
		firstHidden.add(this.biasI);
		firstHidden = Matrix.sigmoid(firstHidden);

		// loop though and calculate outputs for additional hidden layers
		Matrix prevHidden = firstHidden;
		for (int i = 0; i < this.wH.length; i++) {
			Matrix nextHidden = Matrix.product(this.wH[i], prevHidden);
			nextHidden.add(this.biasH[i]);

			// set previous
			prevHidden = Matrix.sigmoid(nextHidden);
		}

		// calculate outputs at output layer
		Matrix output = Matrix.product(this.wO, prevHidden);
		output.add(this.biasO);
		output = Matrix.sigmoid(output);

		// convert to and return array
		return Matrix.toArray(output);
	}

	void train(double inputArray[], double target[]) {
		// FEEDFORWARD

		// calculate outputs at first hidden layer
		Matrix input = new Matrix(inputArray);
		Matrix firstHidden = Matrix.product(this.wI, input);
		firstHidden.add(this.biasI);
		firstHidden = Matrix.sigmoid(firstHidden);

		// loop though and calculate outputs for additional hidden layers
		Matrix prevHidden = firstHidden;
		Matrix additionalLayers[] = new Matrix[this.wH.length];
		for (int i = 0; i < this.wH.length; i++) {
			// prevHidden.shape();
			Matrix nextHidden = Matrix.product(this.wH[i], prevHidden);
			nextHidden.add(this.biasH[i]);

			// set previous, add to hidden layers list
			prevHidden = Matrix.sigmoid(nextHidden);
			additionalLayers[i] = prevHidden;
		}

		// calculate outputs at output layer
		Matrix output = Matrix.product(this.wO, prevHidden);
		output.add(this.biasO);
		output = Matrix.sigmoid(output);

		// TRAIN

		// calculate output error
		Matrix outError = new Matrix(target);
		outError.sub(output);

		// calculate output gradient
		Matrix outGradient = Matrix.dsigmoid(output);
		outGradient.multiply(outError);
		outGradient.multiply(this.learningRate);

		// apply gradient to weights
		Matrix hiddenT = Matrix.transpose(prevHidden);
		Matrix wODelta = Matrix.product(outGradient, hiddenT);
		this.wO.add(wODelta);
		this.biasO.add(outGradient);

		// loop though, calculate, and apply gradient for additional hidden layers
		// (backwards)
		Matrix nextWeights = this.wO;
		Matrix nextError = outError;
		Matrix nextHidden = prevHidden;
		for (int i = this.wH.length - 1; i >= 0; i--) {
			// calculate first hidden error
			Matrix nextWeightsT = Matrix.transpose(nextWeights);
			Matrix nextHiddenError = Matrix.product(nextWeightsT, nextError);

			// calculate first hidden layer gradient
			Matrix nextHiddenGradient = Matrix.dsigmoid(nextHidden);
			nextHiddenGradient.multiply(nextHiddenError);
			nextHiddenGradient.multiply(this.learningRate);

			// set next
			nextWeights = this.wH[i];
			nextError = nextHiddenError;
			nextHidden = additionalLayers[i];

			// apply gradient to weights
			Matrix nextHiddenT = Matrix.transpose(nextHidden);
			Matrix nextWeightDelta = Matrix.product(nextHiddenGradient, nextHiddenT);
			nextWeights.add(nextWeightDelta);
			this.biasH[i].add(nextHiddenGradient);
		}

		// calculate first hidden error
		Matrix wHT = Matrix.transpose(nextWeights);
		Matrix hiddenError = Matrix.product(wHT, nextError);

		// calculate first hidden layer gradient
		Matrix firstHiddenGradient = Matrix.dsigmoid(nextHidden);
		firstHiddenGradient.multiply(hiddenError);
		firstHiddenGradient.multiply(this.learningRate);

		// apply gradient to weights
		Matrix inputT = Matrix.transpose(input);
		Matrix wIDelta = Matrix.product(firstHiddenGradient, inputT);
		this.wI.add(wIDelta);
		this.biasI.add(firstHiddenGradient);

		// increase trained count
		this.count++;
	}

	void train(NNData data[], int reps) {
		// Util.println("Training\n........");
		for (int i = 0; i < reps; i++) {
			NNData d = data[ThreadLocalRandom.current().nextInt(data.length)];
			this.train(d.input, d.target);
		}
		// Util.println("Complete");
	}
}

class NNData {
	double input[];
	double label;
	double target[];

	NNData(double input[], double target[], double label) {
		this.input = input.clone();
		this.target = target.clone();
		this.label = label;
	}

	NNData(NNData data) {
		this.input = data.input.clone();
		this.target = data.target.clone();
		this.label = data.label;
	}
}

class Matrix {
	static Matrix dsigmoid(Matrix mat) {
		Matrix out = new Matrix(mat.rows, mat.cols);
		for (int i = 0; i < mat.rows; i++) {
			for (int j = 0; j < mat.cols; j++) {
				out.m[i][j] = mat.m[i][j] * (1 - mat.m[i][j]);
			}
		}
		return out;
	}

	static Matrix product(Matrix a, Matrix b) {
		if (a.cols != b.rows) {
			throw new IllegalArgumentException(
					"Matricies must be (a, m) • (m, b): (" + Integer.toString(a.rows) + ", " + Integer.toString(a.cols)
							+ ") (" + Integer.toString(b.rows) + ", " + Integer.toString(b.cols) + ")");
		}
		Matrix out = new Matrix(a.rows, b.cols);
		for (int i = 0; i < out.rows; i++) {
			for (int j = 0; j < out.cols; j++) {
				for (int k = 0; k < a.cols; k++) {
					out.m[i][j] += a.m[i][k] * b.m[k][j];
				}
			}
		}
		return out;
	}

	static Matrix sigmoid(Matrix mat) {
		Matrix out = new Matrix(mat.rows, mat.cols);
		for (int i = 0; i < mat.rows; i++) {
			for (int j = 0; j < mat.cols; j++) {
				out.m[i][j] = Util.sigmoid(mat.m[i][j]);
			}
		}
		return out;
	}

	static double[] toArray(Matrix mat) {
		ArrayList<Double> out = new ArrayList<>();
		for (int i = 0; i < mat.rows; i++) {
			for (int j = 0; j < mat.cols; j++) {
				out.add(mat.m[i][j]);
			}
		}
		double outArray[] = new double[out.size()];
		for (int i = 0; i < outArray.length; i++) {
			outArray[i] = out.get(i);
		}
		return outArray;
	}

	static Matrix transpose(Matrix mat) {
		Matrix out = new Matrix(mat.cols, mat.rows);
		for (int i = 0; i < mat.rows; i++) {
			for (int j = 0; j < mat.cols; j++) {
				out.m[j][i] = mat.m[i][j];
			}
		}
		return out;
	}

	int cols;

	double m[][];

	int rows;

	Matrix(double array[]) {
		this.rows = array.length;
		this.cols = 1;
		this.m = new double[array.length][1];

		for (int i = 0; i < array.length; i++) {
			this.m[i][0] = array[i];
		}
	}

	Matrix(double array[][]) {
		this.rows = array.length;
		this.cols = array[0].length;
		this.m = array;
	}

	Matrix(int rows, int cols) {
		this.rows = rows;
		this.cols = cols;
		this.m = new double[rows][cols];
	}

	void add(double s) {
		for (int i = 0; i < this.rows; i++) {
			for (int j = 0; j < this.cols; j++) {
				this.m[i][j] += s;
			}
		}
	}

	void add(Matrix b) {
		if (this.rows != b.rows || this.cols != b.cols) {
			throw new IllegalArgumentException("Matricies must be the same dimensions: (" + Integer.toString(this.rows)
					+ ", " + Integer.toString(this.cols) + ") (" + Integer.toString(b.rows) + ", "
					+ Integer.toString(b.cols) + ")");
		}
		for (int i = 0; i < this.rows; i++) {
			this.m[i] = Util.add(this.m[i], b.m[i]);
		}
	}

	void multiply(double s) {
		for (int i = 0; i < this.rows; i++) {
			this.m[i] = Util.multiply(this.m[i], s);
		}
	}

	void multiply(Matrix b) {
		if (this.rows != b.rows || this.cols != b.cols) {
			throw new IllegalArgumentException("Matricies must be the same dimensions: (" + Integer.toString(this.rows)
					+ ", " + Integer.toString(this.cols) + ") (" + Integer.toString(b.rows) + ", "
					+ Integer.toString(b.cols) + ")");
		}
		for (int i = 0; i < this.rows; i++) {
			for (int j = 0; j < this.cols; j++) {
				this.m[i][j] *= b.m[i][j];
			}
		}
	}

	void print() {
		Util.println("---");
		for (int i = 0; i < this.rows; i++) {
			Util.println(Arrays.toString(m[i]));
		}
		Util.println("---");
	}

	void randomize() {
		for (int i = 0; i < this.rows; i++) {
			for (int j = 0; j < this.cols; j++) {
				this.m[i][j] = ThreadLocalRandom.current().nextDouble(-1, 1);
			}
		}
	}

	void shape() {
		Util.println("(" + Integer.toString(this.rows) + ", " + Integer.toString(this.cols) + ")");
	}

	void sub(Matrix b) {
		if (this.rows != b.rows || this.cols != b.cols) {
			throw new IllegalArgumentException("Matricies must be the same dimensions: (" + Integer.toString(this.rows)
					+ ", " + Integer.toString(this.cols) + ") (" + Integer.toString(b.rows) + ", "
					+ Integer.toString(b.cols) + ")");
		}
		for (int i = 0; i < this.rows; i++) {
			this.m[i] = Util.sub(this.m[i], b.m[i]);
		}
	}
}


