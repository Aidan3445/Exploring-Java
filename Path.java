package src;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javalib.impworld.World;
import javalib.impworld.WorldScene;
import javalib.worldimages.FrameImage;
import javalib.worldimages.Posn;
import javalib.worldimages.RectangleImage;

public class Path extends World {
  int cols;
  NodeAstar current;
  NodeAstar end;
  ArrayList<ArrayList<NodeAstar>> field;
  int height = 800;
  int loops;
  ABST<NodeAstar> open;
  int rows;
  boolean running;
  int sizeH;
  int sizeW;
  NodeAstar start;
  int width = 1200;

  Path(int resolution) {
    this.cols = this.width / resolution;
    this.rows = this.height / resolution;
    this.sizeW = resolution;
    this.sizeH = resolution;
    this.running = false;
    this.loops = 0;
    this.start = new NodeAstar(0, 0);
    this.end = new NodeAstar(cols - 1, rows - 1);
    this.generateField();
    this.open = new Val<>(start, new FSort());
  }

  public static void main(String args[]) {
    Path world = new Path(20);
    world.bigBang(world.width, world.height, 0.005);
  }

  void generateField() {
    ArrayList<ArrayList<Boolean>> walls = new ArrayList<>();
    if (this.field != null) {
      for (int i = 0; i < cols; i++) {
        walls.add(new ArrayList<Boolean>());
        for (int j = 0; j < rows; j++) {
          walls.get(i).add(this.field.get(i).get(j).wall);
        }
      }
    }
    this.field = new ArrayList<>();
    for (int i = 0; i < cols; i++) {
      this.field.add(new ArrayList<NodeAstar>());
      for (int j = 0; j < rows; j++) {
        this.field.get(i).add(new NodeAstar(i, j));
      }
    }
    int startCol = this.start.col;
    int startRow = this.start.row;
    int endCol = this.end.col;
    int endRow = this.end.row;
    this.field.get(endCol).set(endRow, this.end);
    this.field.get(startCol).set(startRow, this.start);
    this.field.get(startCol).get(startRow).f = 0;
    this.field.get(startCol).get(startRow).g = 0;
    for (int i = 0; i < cols; i++) {
      for (int j = 0; j < rows; j++) {
        this.field.get(i).get(j).setNeighbors(this.field);
        if (walls.size() > 0) {
          this.field.get(i).get(j).wall = walls.get(i).get(j);
        }
      }
    }
  }

  void generateWalls(double wallOdds) {
    for (ArrayList<NodeAstar> row : this.field) {
      for (NodeAstar n : row) {
        n.wall = false;
        n.color = new Color(189, 189, 189);
        double roll = new Random().nextDouble();
        if (roll < wallOdds) {
          n.wall = true;
          n.color = new Color(69, 69, 69);
        }
      }
    }
    this.start.wall = false;
    this.end.wall = false;
  }

  @Override
  public WorldScene makeScene() {
    this.start.color = new Color(79, 85, 143);
    this.end.color = new Color(79, 85, 143);
    WorldScene scene = new WorldScene(this.width, this.height);
    for (ArrayList<NodeAstar> row : this.field) {
      for (NodeAstar node : row) {
        node.draw(scene, this.sizeW, this.sizeH, this.running);
      }
    }
    this.end.draw(scene, this.sizeW, this.sizeH, this.running);
    this.start.draw(scene, this.sizeW, this.sizeH, this.running);
    return scene;
  }

  @Override
  public void onKeyEvent(String key) {
    if (key.equals("enter")) {
      this.loops++;
      this.open = new Val<>(this.start, new FSort());
      this.current = this.start;
      this.running = true;
    }
    if (key.equals(" ")) {
      this.generateField();
      this.generateWalls(0.33);
    }
    if (key.equalsIgnoreCase("l")) {
      if (this.loops == 0) {
        this.loops = 100;
      } else {
        this.loops = 0;
      }
    }
  }

  @Override
  public void onMouseClicked(Posn pos, String c) {
    int clickCol = Math.floorDiv(pos.x, this.sizeW);
    int clickRow = Math.floorDiv(pos.y, this.sizeH);
    if (!this.field.get(clickCol).get(clickRow).wall) {
      if (c.equals("LeftButton")) {
        this.end = new NodeAstar(clickCol, clickRow);
      } else if (c.equals("RightButton")) {
        this.start = new NodeAstar(clickCol, clickRow);
      }
      this.running = false;
    }
  }

  @Override
  public void onTick() {
    this.search();
  }

  void recreate(NodeAstar current, Color color) {
    current.color = color;
    if (current.prev != null) {
      this.recreate(current.prev, color);
    }
  }

  void search() {
    if (this.running) {
      if (this.open.size() > 0) {
        this.current = this.open.getLeftmost();
        this.recreate(this.current, new Color(128, 176, 120));
        this.open = this.open.getRight();
        for (NodeAstar n : this.current.n) {
          if (!n.blocked(this.current, this.field)) {
            if (n.row == this.end.row && n.col == this.end.col) {
              this.running = false;
              System.out.println("DONE");
              this.loops--;
              return;
            }
            double tempG = this.current.g + Math.hypot(this.current.col - n.col, this.current.row - n.row);
            if (tempG < n.g) {
              n.g = tempG;
              n.f = tempG + Math.hypot(this.end.col - n.col, this.end.row - n.row);
              n.prev = this.current;
              if (!this.open.present(n, new PSort())) {
                this.open = this.open.insert(n);
              }
            }
          }
        }
      } else {
        System.out.println("FAILED");
        this.running = false;
        this.loops--;
        return;
      }
    } else {
      if (loops > 0) {
        try {
          TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        loops--;
        this.onKeyEvent(" ");
        this.onKeyEvent("enter");
      }
    }
  }
}

class NodeAstar {
  int col;
  Color color;
  double f;
  double g;
  ArrayList<NodeAstar> n;
  NodeAstar prev;
  int row;
  boolean wall = false;

  NodeAstar(int col, int row) {
    this.col = col;
    this.row = row;
    this.g = Double.POSITIVE_INFINITY;
    this.f = Double.POSITIVE_INFINITY;
    this.n = new ArrayList<>();
    this.color = new Color(189, 189, 189);
  }

  boolean blocked(NodeAstar current, ArrayList<ArrayList<NodeAstar>> field) {
    return this.wall || (field.get(this.col).get(current.row).wall && field.get(current.col).get(this.row).wall);
  }

  void draw(WorldScene scene, int sizeW, int sizeH, boolean running) {
    scene.placeImageXY(new FrameImage(new RectangleImage(sizeW, sizeH, "solid", this.color), Color.black),
            this.col * sizeW + sizeW / 2, this.row * sizeH + sizeH / 2);
    if (this.color.equals(new Color(128, 176, 120)) && running) {
      this.color = new Color(191, 103, 103);
    }
  }

  void setNeighbors(ArrayList<ArrayList<NodeAstar>> field) {
    ArrayList<NodeAstar> neighbors = new ArrayList<>();
    for (int i = -1; i <= 1; i++) { // horizontal shift loop
      for (int j = -1; j <= 1; j++) { // vertical shift loop
        if (!(i == 0 && j == 0) && this.col + i >= 0 && this.col + i < field.size() && this.row + j >= 0
                && this.row + j < field.get(0).size()) {
          neighbors.add(field.get(this.col + i).get(this.row + j));
        }
      }
    }
    this.n = neighbors;
  }
}

class PSort implements Comparator<NodeAstar> {
  @Override
  public int compare(NodeAstar a, NodeAstar b) {
    if (Math.hypot(a.row - b.row, a.col - b.col) == 0) {
      return 0;
    }
    if ((double) ((b.row + 1) / (b.col + 1)) - (double) ((a.row + 1) / (a.col + 1)) > 0) {
      return 1;
    } else {
      return -1;
    }
  }
}

class FSort implements Comparator<NodeAstar> {
  @Override
  public int compare(NodeAstar a, NodeAstar b) {
    if (a.f > b.f) {
      return 1;
    }
    if (a.f < b.f) {
      return -1;
    } else {
      return 0;
    }
  }
}

