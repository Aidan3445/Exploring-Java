package src;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;

import javalib.impworld.World;
import javalib.impworld.WorldScene;
import javalib.worldimages.CircleImage;
import javalib.worldimages.LineImage;
import javalib.worldimages.OutlineMode;
import javalib.worldimages.OverlayImage;
import javalib.worldimages.Posn;
import javalib.worldimages.WorldImage;

class Pendulum extends World {
	int worldSize = 800;
  boolean doublePen = false;
  double gravity = 0.25;
  Posn hangPoint = new Posn(this.worldSize / 2, this.worldSize / 2);
  double length = (this.worldSize / 4) - this.radius;
  double dampen = 0.999;
  int radius = 25;
  ArrayList<Posn> path1 = new ArrayList<>();
  ArrayList<Posn> path2 = new ArrayList<>();
  double x1;
  double y1;
	double x2;
  double y2;
  double angle1;
  double angle2;
  double aVel1;
  double aVel2;
  double aAcc1;
  double aAcc2;

	public static void main(String[] args) {
		Pendulum world = new Pendulum();
		world.bigBang(world.worldSize, world.worldSize, 0.01);
	}
  void swing() {
    this.x1 = this.length * Math.sin(this.angle1);
    this.y1 = this.length * Math.cos(this.angle1);
    this.x2 = this.length * Math.sin(this.angle2);
    this.y2 = this.length * Math.cos(this.angle2);
    this.calcAcc();
    this.aVel1 = (this.aVel1 + this.aAcc1) * this.dampen;
    this.aVel2 = (this.aVel2 + this.aAcc2) * this.dampen;
    this.angle1 += this.aVel1;
    this.angle2 += this.aVel2;
    if (!this.doublePen) {
      this.angle2 = Math.PI + this.angle1;
      this.aVel2 = 0;
    }
    this.path1.add(new Posn((int) (this.x1 + this.hangPoint.x), (int) (this.y1 + this.hangPoint.y)));
    this.path2.add(
            new Posn((int) (this.x1 + this.x2 + this.hangPoint.x), (int) (this.y1 + this.y2 + this.hangPoint.y)));
    this.limPaths(50);
  }

  void calcAcc() {
    this.aAcc1 = ((-this.gravity * 3 * Math.sin(this.angle1))
            - (this.gravity * Math.sin(this.angle1 - (2 * this.angle2)))
            - (2 * Math.sin(this.angle1 - this.angle2)) * ((Math.pow(this.aVel2, 2) * this.length)
            + (Math.pow(this.aVel1, 2) * this.length * Math.cos(this.angle1 - this.angle2))))
            / (this.length * (3 - Math.cos(2 * this.angle1 - 2 * this.angle2)));

    this.aAcc2 = ((2 * Math.sin(this.angle1 - this.angle2))
            * ((Math.pow(this.aVel1, 2) * this.length * 2) + (this.gravity * 2 * Math.cos(this.angle1))
            + (Math.pow(this.aVel2, 2) * this.length * Math.cos(this.angle1 - this.angle2))))
            / (this.length * (3 - Math.cos(2 * this.angle1 - 2 * this.angle2)));
  }

  WorldImage balls() {
    return new OverlayImage(new CircleImage(this.radius, OutlineMode.SOLID, Color.RED),
            new CircleImage(this.radius, OutlineMode.SOLID, Color.BLUE).movePinhole(-this.x2, -this.y2))
            .movePinhole(-this.x1, -this.y1);
  }

  WorldImage bars() {
    return new OverlayImage(
            new LineImage(new Posn((int) this.x1, (int) this.y1), Color.BLACK).movePinhole(this.x1 / 2,
                    this.y1 / 2),
            new LineImage(new Posn((int) this.x2, (int) this.y2), Color.BLACK).movePinhole(this.x2 / 2, this.y2 / 2)
                    .movePinhole(-this.x2, -this.y2)).movePinhole(-this.x1, -this.y1);
  }

  void limPaths(int len) {
    while (this.path1.size() > len && this.path2.size() > len) {
      this.path1.remove(0);
      this.path2.remove(0);
    }
  }

  WorldScene draw(WorldScene scene) {
    scene.placeImageXY(new OverlayImage(new OverlayImage(this.balls(), this.bars()),
            new CircleImage(5, OutlineMode.SOLID, Color.BLACK)), this.hangPoint.x, this.hangPoint.y);
    return scene;
  }

  @Override
  public WorldScene makeScene() {
    WorldScene scene = new WorldScene(this.worldSize, this.worldSize);
    scene = new Curve(Color.red, this.path1).draw(scene);
    scene = new Curve(Color.blue, this.path2).draw(scene);
    scene = this.draw(scene);
    return scene;
  }

  @Override
  public void onKeyEvent(String key) {
    if (key.equals(" ")) {
      this.onMousePressed(new Posn(this.hangPoint.x, -1));
      this.aVel2 = 0.15;
    } else if (key.equals("enter")) {
			this.doublePen = !this.doublePen;
		}
  }

  @Override
  public void onMousePressed(Posn pos) {
    this.angle1 = Math.atan2(pos.x - this.hangPoint.x, pos.y - this.hangPoint.y);
		if (this.doublePen) {
			this.angle2 = new Random().nextDouble() * 2 * Math.PI;
			this.aVel2 = 0;
			this.path2.clear();
		} else {
      this.angle2 = Math.PI + this.angle1;
    }
    this.aVel1 = 0;
		this.path1.clear();
	}

  @Override
  public void onTick() {
    this.swing();
  }
}