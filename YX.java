package src;

import java.awt.Color;
import java.util.concurrent.ThreadLocalRandom;
import javalib.impworld.World;
import javalib.impworld.WorldScene;
import javalib.worldimages.CircleImage;
import javalib.worldimages.LineImage;
import javalib.worldimages.Posn;

class Point {
	int height;
	double label;
	double pos[] = new double[2];
	int width;

	Point(int width, int height, double slope) {
		this.width = width;
		this.height = height;
		this.pos[0] = ThreadLocalRandom.current().nextInt(-this.width / 2, this.width / 2);
		this.pos[1] = ThreadLocalRandom.current().nextInt(-this.height / 2, this.height / 2);
		this.update(slope);
	}

	void draw(WorldScene scene) {
		String mode;
		if (this.label > 0) {
			mode = "solid";
		} else {
			mode = "outline";
		}
		scene.placeImageXY(new CircleImage(10, mode, Color.black), (int) pos[0] + this.width / 2,
				(int) pos[1] + this.height / 2);
	}

	void update(double slope) {
		if (this.pos[1] > slope * this.pos[0]) {
			this.label = 1;
		} else {
			this.label = -1;
		}
	}
}

class YX extends World {
	public static void main(String args[]) {
		YX world = new YX();
		world.bigBang(world.width, world.height, 0.001);
	}
	int height = 600;
	NeuralNetwork nn = new NeuralNetwork(2, new int[] { 3 }, 2, 0.1);
	SimplePerceptron p = new SimplePerceptron(2);
	Point points[] = new Point[1000];
	double slope;

	int width = 600;

	YX() {
		this.slope = ThreadLocalRandom.current().nextDouble(-2, 2);
		for (int i = 0; i < this.points.length; i++) {
			this.points[i] = new Point(this.width, this.height, this.slope);
		}
		Util.println(this.slope);
	}

	YX(double slope) {
		this.slope = slope;
		for (int i = 0; i < this.points.length; i++) {
			this.points[i] = new Point(this.width, this.height, this.slope);
		}
	}

	NNData[] makeDataSet(int size) {
		NNData data[] = new NNData[size];
		for (int i = 0; i < size; i++) {
			Point pt = new Point(this.width, this.height, this.slope);
			double target[] = new double[2];
			if (pt.label > 0) {
				target[0] = 1;
			} else {
				target[1] = 1;
			}
			pt.pos[0] /= this.width;
			pt.pos[1] /= this.height;
			data[i] = new NNData(pt.pos, target, pt.label);
		}
		return data;
	}

	@Override
	public WorldScene makeScene() {
		WorldScene scene = new WorldScene(this.width, this.height);
		scene.placeImageXY(new LineImage(new Posn(this.width, (int) (this.slope * this.width)), Color.black),
				this.width / 2, this.height / 2);
		// scene.placeImageXY(new LineImage(
		// new Posn(this.width, (int) -(this.p.weights[0] / this.p.weights[1] *
		// this.width)),
		// Color.blue), this.width / 2, this.height / 2);
		for (Point pt : this.points) {
			pt.draw(scene);
			double g = p.guess(pt.pos);
			double gArray[] = this.nn.feedForward(pt.pos);
			if (gArray[0] > gArray[1]) {
				g = 1;
			} else {
				g = -1;
			}
			Color correct = Color.green;
			if (Math.signum(g) != Math.signum(pt.label)) {
				correct = Color.red;
			}
			scene.placeImageXY(new CircleImage(5, "solid", correct), (int) pt.pos[0] + this.width / 2,
					(int) pt.pos[1] + this.height / 2);
		}
		return scene;
	}

	@Override
	public void onKeyEvent(String key) {
		if (key.equals(" ")) {
			this.slope = Math.tan(ThreadLocalRandom.current().nextDouble(Math.PI));
			this.points = new YX(this.slope).points;
		}
		if (key.equals("enter")) {
			int size = 100000;
			int reps = 100;
			this.nn.train(this.makeDataSet(size), reps);
			// Util.println("Epoch of size " + size * reps + " trained.");
			this.testDataSet(size / 4);
		}
	}

	@Override
	public void onTick() {
		// this.slope = Math.tan(Math.atan(this.slope) + 0.001);
		for (Point pt : this.points) {
			// this.p.train(pt.pos, pt.label);
			pt.update(this.slope);
		}
		this.onKeyEvent("enter");
	}

	void testDataSet(int size) {
		NNData test[] = this.makeDataSet(size);
		//int correct = 0;
		for (int i = 0; i < size; i++) {
			//double g = 0;
			//double gArray[] = 
			this.nn.feedForward(test[i].input);
			//if (gArray[0] > gArray[1]) {
				//g = 1;
			//} else {
				//g = -1;
			//}
			//if (g == test[i].label) {
				//correct++;
			//}
		}
		//Util.println(size + " tested:");
		//Util.println(correct * 100.0 / size + "% correct");
	}
}
