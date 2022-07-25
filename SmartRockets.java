package src;

import java.awt.Color;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import javalib.impworld.World;
import javalib.impworld.WorldScene;
import javalib.worldimages.CircleImage;
import javalib.worldimages.Posn;
import javalib.worldimages.RectangleImage;
import javalib.worldimages.RotateImage;
import javalib.worldimages.TextImage;

class SmartRockets extends World {
  static int width = 800;
  static int height = 600;
  static int lifeSpan = 800;
  static int target[] = new int[] {SmartRockets.width / 2, 30};
  public static void main(String args[]) {
    SmartRockets world = new SmartRockets(500);
    world.bigBang(SmartRockets.width, SmartRockets.height, 0.01);
  }
  int generation = 0;
  int generationTick = 0;
  int numFinished = 0;
  Sketch obsticals = new Sketch(SmartRockets.width, SmartRockets.height, 1);
  Curve prevBest = new Curve(Color.blue);
  Rocket rockets[];
  boolean running = false;

  int speed = 1;

  SmartRockets(int numRockets) {
    this.rockets = new Rocket[numRockets];
    this.generateRockets();
  }

  SmartRockets(Rocket rockets[]) {
    this.rockets = rockets;
  }

  Rocket acceptReject(double maxFit) {
    while (true) {
      Rocket parent = this.rockets[ThreadLocalRandom.current().nextInt(this.rockets.length)];
      if (parent.fitness > ThreadLocalRandom.current().nextDouble(maxFit)) {
        return parent;
      }
    }
  }

  boolean allStopped() {
    boolean stopped = true;
    for (Rocket r : this.rockets) {
      stopped = stopped && (r.collide || r.finish);
    }
    return stopped;
  }

  void bestPath() {
    Rocket best = new Rocket();
    ArrayList<Posn> path = new ArrayList<>();
    for (Rocket r : this.rockets) {
      r.calcFitness();
      if (r.fitness > best.fitness) {
        best = r;
      }
    }
    for (int i = 0; i < SmartRockets.lifeSpan; i++) {
      if (best.path[i][0] != 0.0 && best.path[i][1] != 0.0) {
        path.add(new Posn((int) (best.path[i][0]), (int) (best.path[i][1])));
      }
    }
    this.prevBest = new Curve(Color.blue, path);
  }

  void collision() {
    for (AShape s : this.obsticals.shapes) {
      int left = Math.min(s.points.get(0).x, s.points.get(s.points.size() - 1).x);
      int right = Math.max(s.points.get(0).x, s.points.get(s.points.size() - 1).x);
      int top = Math.min(s.points.get(0).y, s.points.get(s.points.size() - 1).y);
      int bottom = Math.max(s.points.get(0).y, s.points.get(s.points.size() - 1).y);
      for (Rocket r : this.rockets) {
        if (Double.isNaN(r.pos[0]) || Double.isNaN(r.pos[1]) || r.pos[0] < 0 || r.pos[0] > SmartRockets.width
            || r.pos[1] < 0 || r.pos[1] > SmartRockets.height
            || (r.pos[0] > left && r.pos[0] < right && r.pos[1] > top && r.pos[1] < bottom)) {
          r.collide = true;
        }
      }
    }
  }

  void collision(Rocket r) {
    for (AShape s : this.obsticals.shapes) {
      int left = Math.min(s.points.get(0).x, s.points.get(s.points.size() - 1).x);
      int right = Math.max(s.points.get(0).x, s.points.get(s.points.size() - 1).x);
      int top = Math.min(s.points.get(0).y, s.points.get(s.points.size() - 1).y);
      int bottom = Math.max(s.points.get(0).y, s.points.get(s.points.size() - 1).y);
      if (Double.isNaN(r.pos[0]) || Double.isNaN(r.pos[1]) || r.pos[0] < 0 || r.pos[0] > SmartRockets.width
          || r.pos[1] < 0 || r.pos[1] > SmartRockets.height
          || (r.pos[0] > left && r.pos[0] < right && r.pos[1] > top && r.pos[1] < bottom)) {
        r.collide = true;
      }
    }
  }

  WorldScene drawRockets(WorldScene scene) {
    for (Rocket r : this.rockets) {
      r.draw(scene);
    }
    return scene;
  }

  void evaluateRockets() {
    double maxFit = 0;
    Rocket pop[] = new Rocket[this.rockets.length];
    for (Rocket rocket : this.rockets) {
      rocket.calcFitness();
      if (rocket.fitness > maxFit) {
        maxFit = rocket.fitness;
      }
    }
    for (int i = 0; i < this.rockets.length; i++) {
      Rocket parentA = this.acceptReject(maxFit);
      Rocket parentB = this.acceptReject(maxFit);
      pop[i] = new Rocket(new RocketDNA(parentA.dna, parentB.dna));
    }
    this.rockets = pop;
  }

  void generateRockets() {
    for (int i = 0; i < this.rockets.length; i++) {
      this.rockets[i] = new Rocket();
    }
  }

  @Override
  public WorldScene makeScene() {
    WorldScene scene = new WorldScene(SmartRockets.width, SmartRockets.height);
    scene.placeImageXY(new TextImage("Gen: " + Integer.toString(this.generation), Color.black),
        4 * this.percentFinished().length(), 20);
    scene.placeImageXY(
        new TextImage("Frame: " + Integer.toString(this.generationTick) + "/"
            + Integer.toString(SmartRockets.lifeSpan), Color.black),
        4 * this.percentFinished().length(), 35);
    scene.placeImageXY(new TextImage(this.percentFinished(), Color.black), 4 * this.percentFinished().length(), 50);
    this.drawRockets(scene);
    scene = this.obsticals.draw(scene);
    scene = this.prevBest.draw(scene);
    scene.placeImageXY(new CircleImage(10, "solid", Color.red), SmartRockets.target[0], SmartRockets.target[1]);
    return scene;
  }

  @Override
  public void onKeyEvent(String key) {
    if (key.equals("tab")) {
      this.speed *= 10;
    } else if (key.equals("enter")) {
      this.speed = 1;
    } else if (key.equals(" ")) {
      this.running = !this.running;
    } else if (!this.running) {
      this.obsticals.onKeyEvent(key);
      this.prevBest = new Curve(Color.blue);
    }
  }

  @Override
  public void onKeyReleased(String key) {
    if (!this.running) {
      this.obsticals.onKeyReleased(key);
      if (this.obsticals.shapes.size() != (SmartRockets.lifeSpan - 800) / 200) {
        SmartRockets.lifeSpan = 800 + 200 * this.obsticals.shapes.size();
        this.generation = 0;
        this.generateRockets();
      }
    }
  }

  @Override
  public void onMouseMoved(Posn pos) {
    this.obsticals.onMouseMoved(pos);
  }

  @Override
  public void onTick() {
    if (this.running) {
      for (int i = 0; i < this.speed; i++) {
        this.collision();
        for (Rocket r : this.rockets) {
          r.update(this.generationTick);
        }
        this.generationTick++;
        if (this.generationTick == SmartRockets.lifeSpan || this.allStopped()) {
          this.numFinished = 0;
          for (Rocket r : this.rockets) {
            if (r.finish) {
              this.numFinished++;
            }
          }
          this.bestPath();
          this.generation++;
          this.generationTick = 0;
          this.evaluateRockets();
        }
      }
    }
  }

  String percentFinished() {
    return "Finished: " + Integer.toString(this.numFinished) + "/" + Integer.toString(this.rockets.length);
  }
}

class Rocket {
  double acc[];
  boolean collide;
  RocketDNA dna;
  boolean finish;
  double fitness;
  double path[][];
  double pos[];
  int tickFinish;
  double vel[];

  Rocket() {
    this.pos = new double[] { SmartRockets.width / 2, SmartRockets.height };
    this.vel = new double[] { 0, 0 };
    this.acc = new double[] { 0, 0 };
    this.path = new double[SmartRockets.lifeSpan][2];
    this.dna = new RocketDNA();
    this.fitness = 0;
    this.collide = false;
    this.finish = false;
    this.tickFinish = SmartRockets.lifeSpan;
  }

  Rocket(RocketDNA dna) {
    this.pos = new double[] { SmartRockets.width / 2, SmartRockets.height };
    this.vel = new double[] { 0, 0 };
    this.acc = new double[] { 0, 0 };
    this.path = new double[SmartRockets.lifeSpan][2];
    this.dna = dna;
    this.fitness = 0;
    this.collide = false;
    this.finish = false;
    this.tickFinish = SmartRockets.lifeSpan;
  }

  void applyForce(double force[]) {
    this.acc = Util.add(this.acc, force);
  }

  void calcFitness() {
    this.fitness = 1 / ((Math.pow(this.pos[0] - SmartRockets.target[0], 2)
        + Math.pow(this.pos[1] - SmartRockets.target[1], 2)) * this.tickFinish);
    if (this.finish) {
      this.fitness *= 20;
    }
    if (this.collide) {
      this.fitness /= 10;
    }
  }

  WorldScene draw(WorldScene scene) {
    scene.placeImageXY(new RotateImage(new RectangleImage(5, 20, "solid", new Color(0, 100, 200, 100)),
        -Math.toDegrees(Math.atan2(this.vel[0], this.vel[1]))), (int) this.pos[0], (int) this.pos[1]);
    return scene;
  }

  void regulateVectors() {
    if (Util.getMag(this.vel) <= 0) {
      Util.setMag(this.vel, 0.001);
    } else if (Util.getMag(this.vel) >= 2) {
      Util.setMag(this.vel, 1.999);
    }
    this.acc = new double[] { 0, 0 };
  }

  double totalDist() {
    double dist = 0;
    for (int i = 1; i < SmartRockets.lifeSpan; i++) {
      dist += Math.hypot(this.path[i - 1][0] - this.path[i][0], this.path[i - 1][1] - this.path[i][1]);
    }
    return dist;
  }

  void update(int tick) {
    if (Math.pow(this.pos[0] - SmartRockets.target[0], 2)
        + Math.pow(this.pos[1] - SmartRockets.target[1], 2) < 100) {
      this.finish = true;
      tickFinish = tick;
    }
    if (!this.collide && !this.finish) {
      this.path[tick] = this.pos.clone();
      this.applyForce(this.dna.genes[tick]);
      this.vel = Util.add(this.vel, this.acc);
      this.pos = Util.add(this.pos, this.vel);
      this.regulateVectors();
    }
  }
}

class RocketDNA {
  double genes[][] = new double[SmartRockets.lifeSpan][2];

  RocketDNA() {
    for (int i = 0; i < SmartRockets.lifeSpan; i++) {
      this.genes[i] = new double[] { ThreadLocalRandom.current().nextInt(-50, 50),
          ThreadLocalRandom.current().nextInt(-50, 50) };
      Util.setMag(this.genes[i], 0.05);
    }
  }

  RocketDNA(double[] array) {
    if (array.length != SmartRockets.lifeSpan * 2) {
      throw new IllegalArgumentException("Array must be the same size (x2) as the life span.");
    }
    for (int i = 0; i < SmartRockets.lifeSpan * 2; i += 2) {
      this.genes[i] = new double[] { array[i], array[i + 1] };
    }
  }

  RocketDNA(RocketDNA parentA, RocketDNA parentB) {
    if (parentA.genes.length != SmartRockets.lifeSpan || parentB.genes.length != SmartRockets.lifeSpan) {
      throw new IllegalArgumentException("Arrays must both be the same length as the life span.");
    }
    int startRange = ThreadLocalRandom.current().nextInt(SmartRockets.lifeSpan);
    int endRange = ThreadLocalRandom.current().nextInt(SmartRockets.lifeSpan);
    for (int i = 0; i < SmartRockets.lifeSpan; i++) {
      if (i < startRange || i > endRange) {
        this.genes[i] = parentA.genes[i];
      } else {
        this.genes[i] = parentB.genes[i];
      }
    }
    this.mutation();
  }

  void mutation() {
    for (int i = 0; i < SmartRockets.lifeSpan; i++) {
      if (ThreadLocalRandom.current().nextDouble() < 0.005) {
        this.genes[i] = new double[] { ThreadLocalRandom.current().nextInt(-50, 50),
            ThreadLocalRandom.current().nextInt(-50, 50) };
        Util.setMag(this.genes[i], 0.05);
      }
    }
  }

  double[] toSingleArray() {
    double[] temp = new double[SmartRockets.lifeSpan * 2];
    for (int i = 0; i < SmartRockets.lifeSpan * 2; i += 2) {
      temp[i] = this.genes[i / 2][0];
      temp[i + 1] = this.genes[i / 2][1];
    }
    return temp;
  }
}