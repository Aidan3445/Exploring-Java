package src;

import java.awt.Color;
import java.util.ArrayList;

import javalib.impworld.World;
import javalib.impworld.WorldScene;
import javalib.worldimages.CircleImage;
import javalib.worldimages.LineImage;
import javalib.worldimages.Posn;
import javalib.worldimages.RectangleImage;
import javalib.worldimages.TextImage;

abstract class AShape implements IShapes {
	Color color;
	ArrayList<Posn> points;

	AShape(Color color) {
		this.color = color;
		this.points = new ArrayList<>();
	}

	@Override
	public void add(Posn pos) {
		this.points.add(pos);
	}
}

class Circle extends AShape {
	Circle(Color color) {
		super(color);
	}

	Circle(Color color, ArrayList<Posn> points) {
		super(color);
		this.points = points;
	}

	@Override
	public WorldScene draw(WorldScene scene) {
		if (this.points.size() > 1) {
			int radius = (int) (Math.hypot(this.points.get(0).x - this.points.get(this.points.size() - 1).x,
					this.points.get(0).y - this.points.get(this.points.size() - 1).y));
			scene.placeImageXY(new CircleImage(Math.abs(radius), "outline", this.color), this.points.get(0).x,
					this.points.get(0).y);
		}
		return scene;
	}

	@Override
	public AShape dupe() {
		Circle c = new Circle(this.color);
		c.points = this.points;
		return c;
	}
}

class Curve extends AShape {
	Curve(Color color) {
		super(color);
	}

	Curve(Color color, ArrayList<Posn> points) {
		super(color);
		this.points = points;
	}

	@Override
	public WorldScene draw(WorldScene scene) {
		for (int i = 1; i < this.points.size(); i++) {
			int x = this.points.get(i).x - this.points.get(i - 1).x;
			int y = this.points.get(i).y - this.points.get(i - 1).y;
			scene.placeImageXY(new LineImage(new Posn(x, y), this.color).movePinhole(x / 2, y / 2),
					this.points.get(i).x, this.points.get(i).y);
		}
		return scene;
	}

	@Override
	public AShape dupe() {
		Curve c = new Curve(this.color);
		c.points = this.points;
		return c;
	}
}

interface IShapes {

	void add(Posn pos);

	WorldScene draw(WorldScene scene);

	AShape dupe();
}

class Rectangle extends AShape {
	Rectangle(Color color) {
		super(color);
	}

	Rectangle(Color color, ArrayList<Posn> points) {
		super(color);
		this.points = points;
	}

	@Override
	public WorldScene draw(WorldScene scene) {
		if (this.points.size() > 1) {
			int width = this.points.get(0).x - this.points.get(this.points.size() - 1).x;
			int height = this.points.get(0).y - this.points.get(this.points.size() - 1).y;
			scene.placeImageXY(new RectangleImage(Math.abs(width), Math.abs(height), "outline", this.color)
					.movePinhole(width / 2, height / 2), this.points.get(0).x, this.points.get(0).y);
		}
		return scene;
	}

	@Override
	public AShape dupe() {
		Rectangle r = new Rectangle(this.color);
		r.points = this.points;
		return r;
	}
}

class Sketch extends World {
	public static void main(String args[]) {
		Sketch world = new Sketch(800, 800);
		world.bigBang(world.width, world.height, 1);
	}
	Color color;
	AShape current;
	boolean drawing;
	int height;
	int mode;
	ArrayList<AShape> shapes;

	int width;

	Sketch(int width, int height) {
		this.width = width;
		this.height = height;
		this.color = Color.black;
		this.mode = 0;
		this.drawing = false;
		this.current = this.makeShape();
		this.shapes = new ArrayList<>();
	}

	Sketch(int width, int height, AShape shape) {
		this.width = width;
		this.height = height;
		this.color = Color.black;
		this.mode = 0;
		this.drawing = false;
		this.current = shape;
		this.shapes = new ArrayList<>();
	}

	Sketch(int width, int height, int mode) {
		this.width = width;
		this.height = height;
		this.color = Color.black;
		if (mode > 2) {
			throw new IllegalArgumentException("Mode must be from 0-2, only 3 drawing modes.");
		}
		this.mode = mode;
		this.drawing = false;
		this.current = this.makeShape();
		this.shapes = new ArrayList<>();
	}

	WorldScene draw(WorldScene scene) {
		scene = this.current.draw(scene);
		for (AShape shape : this.shapes) {
			scene = shape.draw(scene);
		}
		return scene;
	}

	@Override
	public WorldScene makeScene() {
		WorldScene scene = new WorldScene(this.width, this.height);
		scene = this.draw(scene);
		String modes[] = new String[] { "Pen", "Rectangle", "Circle" };
		scene.placeImageXY(new TextImage(modes[this.mode], this.color), this.width / 2, 20);
		return scene;
	}

	AShape makeShape() {
		if (this.mode == 0) {
			return new Curve(this.color);
		} else if (this.mode == 1) {
			return new Rectangle(this.color);
		} else {
			return new Circle(this.color);
		}
	}

	@Override
	public void onKeyEvent(String key) {
		if (key.equalsIgnoreCase("c")) {
			this.shapes.clear();
		} else if (key.equals("shift")) {
			this.drawing = true;
		} else if (key.equals("tab")) {
			this.mode = (this.mode + 1) % 3;
			this.current = this.makeShape();
		}
	}

	@Override
	public void onKeyReleased(String key) {
		if (key.equals("shift")) {
			this.drawing = false;
			AShape temp = this.current.dupe();
			this.shapes.add(temp);
			this.current = this.makeShape();
		}
	}

	@Override
	public void onMouseMoved(Posn pos) {
		if (this.drawing) {
			this.current.add(pos);
		}
	}
}
