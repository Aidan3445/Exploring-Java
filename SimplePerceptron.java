package src;

import java.util.concurrent.ThreadLocalRandom;

class SimplePerceptron {
	double learningRate = 0.1;
	double weights[];

	SimplePerceptron(int inputNum) {
		this.weights = new double[inputNum + 1];
		for (int i = 0; i < this.weights.length; i++) {
			this.weights[i] = ThreadLocalRandom.current().nextDouble(-1, 1);
		}
	}

	double[] addBias(double inputs[]) {
		double inputsB[] = new double[inputs.length + 1];
		for (int i = 0; i < inputs.length; i++) {
			inputsB[i] = inputs[i];
		}
		inputsB[inputs.length] = 1;
		return inputsB;
	}

	double guess(double[] inputs) {
		double inputsB[] = this.addBias(inputs);
		double sum = 0;
		for (int i = 0; i < this.weights.length; i++) {
			sum += inputsB[i] * this.weights[i];
		}
		return Math.signum(sum);
	}

	void train(double inputs[], double target) {
		double inputsB[] = this.addBias(inputs);
		double guess = this.guess(inputs);
		double error = target - guess;
		for (int i = 0; i < this.weights.length; i++) {
			this.weights[i] += error * inputsB[i] * this.learningRate;
		}
	}
}