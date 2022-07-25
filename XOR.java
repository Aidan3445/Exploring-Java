package src;

import java.awt.Color;
import java.util.Arrays;

import javalib.impworld.World;
import javalib.impworld.WorldScene;
import javalib.worldimages.RectangleImage;
import javalib.worldimages.TextImage;

class XOR extends World {
	int res = 10;
	int width = 600;
	int height = 600;
	int cols = this.width / this.res;
	int rows = this.height / this.res;
	NeuralNetwork nn = new NeuralNetwork(2, 3, 1, 0.1);
	NNData data[] = new NNData[4];

	XOR() {
		this.data[0] = new NNData(new double[] { 0, 1 }, new double[] { 1 }, 1);
		this.data[1] = new NNData(new double[] { 1, 0 }, new double[] { 1 }, 1);
		this.data[2] = new NNData(new double[] { 0, 0 }, new double[] { 0 }, 0);
		this.data[3] = new NNData(new double[] { 1, 1 }, new double[] { 0 }, 0);
	}
	
	public static void main(String args[]) {
		XOR xor = new XOR();
		xor.bigBang(xor.width, xor.height, 0.001);
	}

	@Override
	public WorldScene makeScene() {
		WorldScene scene = new WorldScene(this.width, this.height);
		for (double i = 0; i < this.cols; i++) {
			for (double j = 0; j < this.rows; j++) {
				double x1 = i / this.cols;
				double x2 = j / this.rows;
				double input[] = { x1, x2 };
				int y = (int) (this.nn.feedForward(input)[0] * 255);
				scene.placeImageXY(new RectangleImage(this.res, this.res, "solid", new Color(y, y, y)),
						(int) i * this.res + this.res / 2, (int) j * this.res + this.res / 2);
			}
		}
		scene.placeImageXY(new TextImage(Integer.toString(this.nn.count) + " Trained", Color.red), 300, 300);
		return scene;
	}

	@Override
	public void onTick() {
		nn.train(this.data, 100);
		// this.runData(this.data);
	}

	void runData() {
		for (NNData d : data) {
			Util.print(Arrays.toString(d.input));
			Util.println(Arrays.toString(this.nn.feedForward(d.input)));
		}
	}
}