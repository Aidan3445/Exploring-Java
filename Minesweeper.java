package src;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;

import javalib.funworld.World;
import javalib.funworld.WorldScene;
import javalib.worldimages.AboveImage;
import javalib.worldimages.AlignModeX;
import javalib.worldimages.AlignModeY;
import javalib.worldimages.CircleImage;
import javalib.worldimages.EquilateralTriangleImage;
import javalib.worldimages.FontStyle;
import javalib.worldimages.OverlayImage;
import javalib.worldimages.OverlayOffsetAlign;
import javalib.worldimages.Posn;
import javalib.worldimages.RectangleImage;
import javalib.worldimages.RotateImage;
import javalib.worldimages.ScaleImage;
import javalib.worldimages.TextImage;
import javalib.worldimages.WorldEnd;
import javalib.worldimages.WorldImage;

interface ITile {

  void clicked();

  WorldImage draw();

  void flagged();

  boolean isClicked();

  boolean isMine();

  boolean isZero();

  boolean mineNeighbor(ATile other);

  void setNum(ArrayList<ATile> tiles);

  Mine toMine();
}

class MSBoard extends World {
  static int tilesX = 9;
  static int tilesY = 9;
  static int boardHeight = MSBoard.setHeight();
  static int boardWidth = MSBoard.setWidth();
  static int num = 10;
  int flagsLeft = 0;
  boolean mineClicked = false;
  int seconds = 0;
  ArrayList<ATile> tiles = new ArrayList<>();

  public static void main(String[] args) {
    MSBoard world = new MSBoard(9, 9, 10);
    world.bigBang(MSBoard.boardWidth * 11 / 10, MSBoard.boardHeight, 0.5);
  }

	MSBoard(int tilesX, int tilesY, int num) {
			if (tilesX * tilesY * num > 0) {
				MSBoard.tilesX = tilesX;
				MSBoard.tilesY = tilesY;
				MSBoard.num = num;
			} else {
				throw new IllegalArgumentException("Invalid tiles.");
			}
		}

		MSBoard() {
			if (tilesX * tilesY * num <= 0) {
				throw new IllegalArgumentException("Invalid tiles.");
			}
		}

  static int setHeight() {
    if ((double) MSBoard.tilesX / MSBoard.tilesY < (double) 1375 / 800) {
      return 800;
    } else {
      return 1375 * MSBoard.tilesY / MSBoard.tilesX;
    }
  }

  static int setWidth() {
    if ((double) MSBoard.tilesX / MSBoard.tilesY > (double) 1375 / 800) {
      return 1375;
    } else {
      return 800 * MSBoard.tilesX / MSBoard.tilesY;
    }
  }

  ArrayList<Integer> clickIsZero(int click) {
    ArrayList<Integer> neighbors = new ArrayList<>();
    ATile clickTile = new Mine(click);
    for (int i = 0; i < MSBoard.tilesX * MSBoard.tilesY; i++) {
      if (new Mine(i).mineNeighbor(clickTile)) {
        neighbors.add(i);
      }
    }
    return neighbors;
  }

  WorldScene draw(WorldScene scene) {
    if (this.tiles.size() == 0) {
      MSBoard temp = new MSBoard();
      for (int i = 0; i < MSBoard.tilesX * MSBoard.tilesY; i++) {
        temp.tiles.add(new Safe(Math.floorDiv(i, MSBoard.tilesX), i % MSBoard.tilesX));
      }
      return temp.draw(scene);
    }
    for (ATile tile : tiles) {
      scene = scene.placeImageXY(tile.draw(), tile.col * MSBoard.boardWidth / MSBoard.tilesX,
              tile.row * MSBoard.boardHeight / MSBoard.tilesY);
    }
    return scene
            .placeImageXY(
                    new ScaleImage(
                            new AboveImage(
                                    new AboveImage(
                                            new ScaleImage(
                                                    new OverlayOffsetAlign(AlignModeX.LEFT, AlignModeY.TOP,
                                                            new AboveImage(
                                                                    new RectangleImage(5, 50, "solid", Color.red),
                                                                    new RectangleImage(20, 5, "solid", Color.red)),
                                                            1, -10,
                                                            new RotateImage(new EquilateralTriangleImage(30,
                                                                    "solid", Color.red), -30)),
                                                    MSBoard.boardWidth * 0.00125),
                                            new TextImage(Integer.toString(this.flagsLeft), 40, Color.red)
                                                    .movePinhole(10, -10)),
                                    this.timer(seconds)),
                            Math.min(MSBoard.boardWidth * 0.001, MSBoard.boardHeight * .005)),
                    MSBoard.boardWidth * 21 / 20, MSBoard.boardHeight / 2);

  }

  boolean gameWin() {
    int numClicked = 0;
    for (ATile tile : this.tiles) {
      if (tile.isClicked()) {
        numClicked++;
      }
    }
    return numClicked == (MSBoard.tilesX * MSBoard.tilesY) - MSBoard.num && this.tiles.size() > 1;
  }

  void generate(int click) {
    ArrayList<Integer> mines = new ArrayList<>();

    for (int i = 0; i < MSBoard.tilesX * MSBoard.tilesY; i++) {
      mines.add(i);
    }
    while (mines.size() - 1 > MSBoard.num) {
      mines.remove(new Random().nextInt(mines.size()));
    }
    mines.removeAll(this.clickIsZero(click));
    for (int i = 0; i < MSBoard.tilesX * MSBoard.tilesY; i++) {
      this.tiles.add(new Safe(Math.floorDiv(i, MSBoard.tilesX), i % MSBoard.tilesX));
      if (mines.contains(i)) {
        this.tiles.set(i, this.tiles.get(i).toMine());
      }
    }
    MSBoard.num = 0;
    for (ATile tile : this.tiles) {
      tile.setNum(tiles);
      if (tile.isMine()) {
        MSBoard.num++;
      }
    }
    this.seconds = 0;
  }

  WorldScene makeLossScene() {
    int misFlagged = 0;
    for (ATile tile : tiles) {
      if (tile.isFlagged() && !tile.isMine()) {
        misFlagged++;
      }
      tile.clicked();
    }
    this.flagsLeft = misFlagged;
    return this.draw(getEmptyScene())
            .placeImageXY(new TextImage("GAME OVER!",
                            Math.min(MSBoard.boardWidth / 10, MSBoard.boardHeight * 8 / 10), FontStyle.BOLD, Color.red),
                    MSBoard.boardWidth / 2, MSBoard.boardHeight / 2);
  }

  @Override
  public WorldScene makeScene() {
    WorldScene scene = new WorldScene(MSBoard.boardWidth * 11 / 10, MSBoard.boardHeight);
    return this.draw(scene);
  }

  WorldScene makeWinScene() {
    for (ATile tile : tiles) {
      tile.clicked();
    }
    return this.draw(getEmptyScene()).placeImageXY(new TextImage("YOU WIN!",
                    Math.min(MSBoard.boardWidth / 10, MSBoard.boardHeight * 8 / 10), FontStyle.BOLD, Color.green),
            MSBoard.boardWidth / 2, MSBoard.boardHeight / 2);
  }

  @Override
  public MSBoard onMouseClicked(Posn pos, String buttonName) {
    int i = (int) (Math.floor((double) pos.x / MSBoard.boardWidth * MSBoard.tilesX)
            + MSBoard.tilesX * Math.floor((double) pos.y / MSBoard.boardHeight * MSBoard.tilesY));

    if (pos.x > MSBoard.boardWidth) {
      return this;
    }
    if (this.tiles.size() == 0) {
      this.generate(i);
    }
    if (buttonName.equals("LeftButton")) {
      this.mineClicked = this.tiles.get(i).isMine();
      this.tiles.get(i).clicked();
      if (this.tiles.get(i).isZero()) {
        this.showAllNeighbors(i);
      }
    } else if (buttonName.equals("RightButton") && (this.flagsLeft > 0 || this.tiles.get(i).isFlagged())) {
      this.tiles.get(i).flagged();
    }
    this.flagsLeft = MSBoard.num;

    for (ATile tile : this.tiles) {
      if (tile.isFlagged()) {
        this.flagsLeft--;
      }
    }
    return this;
  }

  @Override
  public MSBoard onTick() {
    this.seconds++;
    return this;
  }

  void showAllNeighbors(int i) {
    for (ATile tile : this.tiles) {
      if (this.tiles.get(i).toMine().mineNeighbor(tile) && !tile.isClicked()) {
        tile.clicked();
        if (tile.isZero()) {
          this.showAllNeighbors(tile.row * MSBoard.tilesX + tile.col);
        }
      }
    }
  }

  WorldImage timer(int sec) {
    String secTxt;
    String minTxt = Integer.toString(sec / 60);
    if (sec % 60 < 10) {
      secTxt = "0" + sec % 60;
    } else {
      secTxt = Integer.toString(sec % 60);
    }
    String timerTxt = minTxt + ":" + secTxt;
    double scale = 1;
    if (seconds > 600) {
      scale -= 0.2 * Math.floor(Math.log10(sec / 60));
    }
    return new ScaleImage(new TextImage(timerTxt, 40, Color.red).movePinhole(10, -20), scale);
  }

  @Override // tests for world end state
  public WorldEnd worldEnds() {
    if (this.mineClicked) {
      return new WorldEnd(true, this.makeLossScene());
    } else if (this.gameWin()) {
      return new WorldEnd(true, this.makeWinScene());
    } else {
      return new WorldEnd(false, this.makeScene());
    }
  }
}

class Safe extends ATile {
  int num;

  Safe(int row, int col) {
    super(row, col, false, false, false);
    this.num = 0;
  }

  Safe(int row, int col, boolean clicked, boolean flagged, int num) {
    super(row, col, false, clicked, flagged);
    this.num = num;
  }

  @Override
  public WorldImage draw() {
    if (isClicked) {
      return new OverlayImage(
              new TextImage(Integer.toString(this.num), Math.min(ATile.width, ATile.height) * 4 / 10,
                      ATile.colors.get(num)),
              new OverlayImage(new RectangleImage(ATile.width, ATile.height, "outline", Color.black),
                      new RectangleImage(ATile.width, ATile.height, "solid", Color.lightGray)))
              .movePinhole(-ATile.width / 2, -ATile.height / 2);
    } else {
      return this.unclicked();
    }
  }

  @Override
  public boolean isZero() {
    return this.num == 0;
  }

  @Override
  public boolean mineNeighbor(ATile other) {
    return false;
  }

  @Override
  public void setNum(ArrayList<ATile> tiles) {
    for (ATile tile : tiles) {
      if (tile.mineNeighbor(this)) {
        this.num++;
      }
    }
  }
}

abstract class ATile implements ITile {
  static ArrayList<Color> colors = ATile.setColors();
  static int width = Math.min(MSBoard.boardWidth / MSBoard.tilesX, MSBoard.boardHeight / MSBoard.tilesY);
  static int height = ATile.width;
  int col;
  boolean isClicked;
  boolean isFlagged;
  boolean isMine;
  int row;

  ATile(int row, int col, boolean mine, boolean clicked, boolean flagged) {
    this.row = row;
    this.col = col;
    this.isMine = mine;
    this.isClicked = clicked;
    this.isFlagged = flagged;
  }

  static ArrayList<Color> setColors() {
    ArrayList<Color> colors = new ArrayList<>();
    colors.add(Color.lightGray);
    colors.add(Color.blue);
    colors.add(Color.green);
    colors.add(Color.red);
    colors.add(Color.magenta);
    colors.add(Color.orange.darker());
    colors.add(Color.cyan);
    colors.add(Color.white);
    colors.add(Color.black);
    return colors;
  }

  @Override
  public void clicked() {
    this.isClicked = true;
    this.isFlagged = false;
  }

  @Override
  public void flagged() {
    if (!this.isClicked) {
      this.isFlagged = !this.isFlagged;
    }
  }

  @Override
  public boolean isClicked() {
    return this.isClicked;
  }

  public boolean isFlagged() {
    return this.isFlagged;
  }

  @Override
  public boolean isMine() {
    return false;
  }

  @Override
  public boolean isZero() {
    return false;
  }

  @Override
  public Mine toMine() {
    return new Mine(this.row, this.col);
  }

  public WorldImage unclicked() {
    WorldImage tile = new OverlayImage(new RectangleImage(ATile.width, ATile.height, "outline", Color.BLACK),
            new RectangleImage(ATile.width, ATile.height, "solid", Color.gray)).movePinhole(-ATile.width / 2,
            -ATile.height / 2);
    if (this.isFlagged) {
      tile = new OverlayImage(new ScaleImage(new OverlayOffsetAlign(AlignModeX.LEFT, AlignModeY.TOP,
              new AboveImage(new RectangleImage(5, 50, "solid", Color.red),
                      new RectangleImage(20, 5, "solid", Color.red)),
              1, -10, new RotateImage(new EquilateralTriangleImage(30, "solid", Color.red), -30)).movePinhole(-40,
              -40),
              ATile.height * 0.0125), tile);
    }
    return tile;
  }
}

class Mine extends ATile {
  Mine(int i) {
    super(Math.floorDiv(i, MSBoard.tilesX), i % MSBoard.tilesX, true, false, false);
  }

  Mine(int row, int col) {
    super(row, col, true, false, false);
  }

  Mine(int row, int col, boolean clicked, boolean flagged) {
    super(row, col, true, clicked, flagged);
  }

  @Override
  public WorldImage draw() {
    if (isClicked) {
      return new OverlayImage(new CircleImage(Math.min(ATile.width, ATile.height) * 3 / 10, "solid", Color.black),
              new OverlayImage(new RectangleImage(ATile.width, ATile.height, "outline", Color.black),
                      new RectangleImage(ATile.width, ATile.height, "solid", Color.gray)))
              .movePinhole(-ATile.width / 2, -ATile.height / 2);
    } else {
      return this.unclicked();
    }
  }

  @Override
  public boolean isMine() {
    return true;
  }

  @Override
  public boolean mineNeighbor(ATile other) {
    return (this.row == other.row || this.row == other.row + 1 || this.row == other.row - 1)
            && (this.col == other.col || this.col == other.col + 1 || this.col == other.col - 1);
  }

  @Override
  public void setNum(ArrayList<ATile> tiles) {
    return;
  }
}
