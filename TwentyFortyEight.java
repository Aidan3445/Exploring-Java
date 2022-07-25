package src;

import java.awt.Color;
import java.util.Comparator;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import javalib.funworld.World;
import javalib.funworld.WorldScene;
import javalib.worldimages.FontStyle;
import javalib.worldimages.OutlineMode;
import javalib.worldimages.OverlayImage;
import javalib.worldimages.RectangleImage;
import javalib.worldimages.TextImage;
import javalib.worldimages.WorldEnd;

abstract class ATiles implements ITiles {
	int boardSize = new GameScale().boardSize; // x by x square board
	Color color;
	int column;
	int row;
	int tileSize = new GameScale().tileSize;
	int value;

	ATiles(int value, int row, int column) {
		this.value = value;
		this.row = row;
		this.column = column;
		this.color = new Orange().shade(Math.log10(value) / Math.log10(2));
	}

	@Override
	public int index() {
		return this.row * this.boardSize + this.column;
	}

	@Override
	public boolean sameTile(ATiles other, int IVB) {
		if (IVB == 0) {
			return other.index() == this.index();
		} else if (IVB == 1) {
			return this.value == other.value;
		} else {
			return other.index() == this.index() && this.value == other.value;
		}
	}
}

class BlankTile extends ATiles {
	BlankTile(int row, int column) {
		super(0, row, column);
	}

	@Override
	public WorldScene draw(WorldScene scene) {
		return scene.placeImageXY(
				new OverlayImage(
						new RectangleImage((int) (tileSize * 0.9), (int) (this.tileSize * 0.9), OutlineMode.SOLID,
								Color.gray),
						new RectangleImage(this.tileSize, tileSize, OutlineMode.SOLID, Color.black)),
				this.column * tileSize + tileSize / 2, this.row * tileSize + tileSize / 2);
	}
}

class BlockedAbove implements BiFunction<ATiles, Boolean, Boolean> {
	ATiles check;

	BlockedAbove(ATiles check) {
		this.check = check;
	}

	@Override
	public Boolean apply(ATiles t, Boolean u) {
		return u || (t.column == this.check.column && this.check.row - t.row == 1 && !t.sameTile(this.check, 1));
	}
}

class BlockedBelow implements BiFunction<ATiles, Boolean, Boolean> {
	ATiles check;

	BlockedBelow(ATiles check) {
		this.check = check;
	}

	@Override
	public Boolean apply(ATiles t, Boolean u) {
		return u || (t.column == this.check.column && t.row - this.check.row == 1 && !t.sameTile(this.check, 1));
	}
}

class BlockedLeft implements BiFunction<ATiles, Boolean, Boolean> {
	ATiles check;

	BlockedLeft(ATiles check) {
		this.check = check;
	}

	@Override
	public Boolean apply(ATiles t, Boolean u) {
		return u || (t.row == this.check.row && this.check.column - t.column == 1 && !t.sameTile(this.check, 1));
	}
}

class BlockedRight implements BiFunction<ATiles, Boolean, Boolean> {
	ATiles check;

	BlockedRight(ATiles check) {
		this.check = check;
	}

	@Override
	public Boolean apply(ATiles t, Boolean u) {
		return u || (t.row == this.check.row && t.column - this.check.column == 1 && !t.sameTile(this.check, 1));
	}
}

// non-empty generic list
class ConsList<T> implements IList<T> {
	T first;
	IList<T> rest;

	ConsList(T first, IList<T> rest) {
		this.first = first;
		this.rest = rest;
	}

	@Override
	public IList<T> filter(Predicate<T> pred) {
		if (pred.test(this.first)) {
			return new ConsList<>(this.first, this.rest.filter(pred));
		} else {
			return this.rest.filter(pred);
		}
	}

	@Override
	public <U> U foldr(BiFunction<T, U, U> f, U base) {
		return f.apply(this.first, this.rest.foldr(f, base));
	}

	@Override
	public int len() {
		return 1 + this.rest.len();
	}

	@Override
	public <U> IList<U> map(Function<T, U> f) {
		return new ConsList<>(f.apply(this.first), this.rest.map(f));
	}

	@Override
	public boolean sameList(IList<T> other, Comparator<T> comp) {
		return other.sameListHelper(this.first, this.rest, comp);
	}

	@Override
	public boolean sameListHelper(T otherFirst, IList<T> otherRest, Comparator<T> comp) {
		return comp.compare(otherFirst, this.first) == 0 && this.rest.sameList(otherRest, comp);
	}
}

class Draw implements BiFunction<ATiles, WorldScene, WorldScene> {
	@Override
	public WorldScene apply(ATiles t, WorldScene u) {
		return t.draw(u);
	}
}

class GameScale {
	int boardSize = 4; // x by x square board
	int tileSize = 100;
}

interface IList<T> {
	IList<T> filter(Predicate<T> pred);

	<U> U foldr(BiFunction<T, U, U> f, U base);

	int len();

	// map over a list, and produce a new list with a (possibly different)
	// element type
	<U> IList<U> map(Function<T, U> f);

	boolean sameList(IList<T> other, Comparator<T> comp);

	boolean sameListHelper(T otherFirst, IList<T> otherRest, Comparator<T> comp);
}

interface ITiles {

	WorldScene draw(WorldScene scene);

	int index();

	boolean sameTile(ATiles other, int IVB); // IVB: 0 = index only, 1 = value only, other = both
}

class MoveDown implements Function<ATiles, ATiles> {
	IList<ATiles> tiles;

	MoveDown(IList<ATiles> tiles) {
		this.tiles = tiles;
	}

	@Override
	public ATiles apply(ATiles t) {
		if (t.row == t.boardSize - 1 || this.tiles.foldr(new BlockedBelow(t), false)) {
			return t;
		} else
			return new NumberTile(t.value, t.row + 1, t.column);
	}
}

class MoveLeft implements Function<ATiles, ATiles> {
	IList<ATiles> tiles;

	MoveLeft(IList<ATiles> tiles) {
		this.tiles = tiles;
	}

	@Override
	public ATiles apply(ATiles t) {
		if (t.column == 0 || this.tiles.foldr(new BlockedLeft(t), false)) {
			return t;
		} else
			return new NumberTile(t.value, t.row, t.column - 1);
	}
}

class MoveRight implements Function<ATiles, ATiles> {
	IList<ATiles> tiles;

	MoveRight(IList<ATiles> tiles) {
		this.tiles = tiles;
	}

	@Override
	public ATiles apply(ATiles t) {
		if (t.column == t.boardSize - 1 || this.tiles.foldr(new BlockedRight(t), false)) {
			return t;
		} else
			return new NumberTile(t.value, t.row, t.column + 1);
	}
}

class MoveUp implements Function<ATiles, ATiles> {
	IList<ATiles> tiles;

	MoveUp(IList<ATiles> tiles) {
		this.tiles = tiles;
	}

	@Override
	public ATiles apply(ATiles t) {
		if (t.row == 0 || this.tiles.foldr(new BlockedAbove(t), false)) {
			return t;
		} else
			return new NumberTile(t.value, t.row - 1, t.column);
	}
}

// empty generic list
class MtList<T> implements IList<T> {
	MtList() {
	}

	@Override
	public IList<T> filter(Predicate<T> pred) {
		return this;
	}

	@Override
	public <U> U foldr(BiFunction<T, U, U> f, U base) {
		return base;
	}

	@Override
	public int len() {
		return 0;
	}

	@Override
	public <U> IList<U> map(Function<T, U> f) {
		return new MtList<>();
	}

	@Override
	public boolean sameList(IList<T> other, Comparator<T> comp) {
		return this.sameListHelper(null, other, comp);
	}

	@Override
	public boolean sameListHelper(T otherFirst, IList<T> otherRest, Comparator<T> comp) {
		return otherFirst == null;
	}
}

class NumberTile extends ATiles {
	NumberTile(int value, int row, int column) {
		super(value, row, column);
	}

	@Override
	public WorldScene draw(WorldScene scene) {
		return scene.placeImageXY(
				new OverlayImage(
						new TextImage(Integer.toString(this.value), this.fontSize(), FontStyle.BOLD, Color.white),
						new OverlayImage(
								new RectangleImage((int) (tileSize * 0.9), (int) (tileSize * 0.9), OutlineMode.SOLID,
										this.color),
								new RectangleImage(this.tileSize, tileSize, OutlineMode.SOLID, Color.black))),
				this.column * tileSize + tileSize / 2, this.row * tileSize + tileSize / 2);
	}

	int fontSize() {
		return this.tileSize / Integer.toString(this.value).length() + 1;
	}
}

class Orange {
	Color color;

	Orange() {
		this.color = Color.ORANGE;
	}

	Orange(Color color) {
		this.color = color;
	}

	Color shade(double value) {
		if (value > 1) {
			return new Orange(new Color(this.color.getRed(), (this.color.getGreen() + 241) % 256,
					(this.color.getBlue() + 9) % 256)).shade(value - 1);
		} else {
			return this.color;
		}
	}
}

class RemTile implements Predicate<ATiles> {
	ATiles check;

	RemTile(ATiles check) {
		this.check = check;
	}

	@Override
	public boolean test(ATiles t) {
		return !check.sameTile(t, 2);
	}

}

class Smash implements BiFunction<ATiles, IList<ATiles>, IList<ATiles>> {
	IList<ATiles> tiles;

	Smash(IList<ATiles> tiles) {
		this.tiles = tiles;
	}

	@Override
	public IList<ATiles> apply(ATiles t, IList<ATiles> u) {
		if (u.foldr(new SpaceTaken(t.row, t.column), false)) {
			return new ConsList<>(new NumberTile(t.value * 2, t.row, t.column), u).filter(new RemTile(t));
		} else {
			return new ConsList<>(t, u);
		}
	}
}

class SpaceTaken implements BiFunction<ATiles, Boolean, Boolean> {
	int column;
	int row;

	SpaceTaken(int row, int column) {
		this.row = row;
		this.column = column;
	}

	@Override
	public Boolean apply(ATiles t, Boolean u) {
		return u || (t.row == row && t.column == column);
	}
}

class TFBoard extends World {
	public static void main(String args[]) {
		TFBoard world = new TFBoard().spawn().spawn();
		world.bigBang(world.boardSize * world.tileSize, world.boardSize * world.tileSize, 0.01);
	}
	int boardSize = new GameScale().boardSize; // x by x square board
	IList<ATiles> tiles;

	int tileSize = new GameScale().tileSize;

	TFBoard() {
		this.tiles = new MtList<>();
	}

	TFBoard(boolean blank) {
		this.tiles = this.blankBoard((this.boardSize * this.boardSize) - 1);
	}

	TFBoard(IList<ATiles> tiles) {
		this.tiles = tiles;
	}

	IList<ATiles> blankBoard(int size) {
		if (size >= 0) {
			return new ConsList<>(new BlankTile(Math.floorDiv(size, this.boardSize), size % this.boardSize),
					this.blankBoard(size - 1));
		} else {
			return new MtList<>();
		}
	}

	WorldScene draw(WorldScene scene) {
		return this.tiles.foldr(new Draw(), scene);
	}

	boolean gameEnd() {
		return this.tiles.len() == this.boardSize * this.boardSize
				&& this.tiles.map(new MoveUp(this.tiles)).map(new MoveDown(this.tiles)).map(new MoveLeft(this.tiles))
						.map(new MoveRight(this.tiles)).sameList(this.tiles, new TileChecker());
	}

	// defines end scene
	public WorldScene makeEndScene() {
		WorldScene endScene = this.draw(new WorldScene(this.boardSize * this.tileSize, this.boardSize * this.tileSize));
		return endScene.placeImageXY(new TextImage("GAME OVER", this.tileSize / 2, FontStyle.BOLD, Color.red),
				this.boardSize * this.tileSize / 2, this.boardSize * this.tileSize / 2);
	}

	@Override
	public WorldScene makeScene() {
		WorldScene scene = new WorldScene(this.boardSize * tileSize, this.boardSize * tileSize);
		scene = new TFBoard(true).draw(scene);

		if (!this.gameEnd()) {
			scene = this.draw(scene);
		}

		return scene;
	}

	TFBoard move(String dir) {
		TFBoard moved = this;
		if (dir.equals("up")) {
			moved = new TFBoard(this.tiles.map(new MoveUp(this.tiles)));
		} else if (dir.equals("down")) {
			moved = new TFBoard(this.tiles.map(new MoveDown(this.tiles)));
		} else if (dir.equals("left")) {
			moved = new TFBoard(this.tiles.map(new MoveLeft(this.tiles)));
		} else if (dir.equals("right")) {
			moved = new TFBoard(this.tiles.map(new MoveRight(this.tiles)));
		}

		if (this.tiles.sameList(moved.tiles, new TileChecker())) {
			return moved.spawn();
		} else {
			return moved.smash().move(dir);
		}
	}

	@Override
	public TFBoard onKeyEvent(String key) {
		if (key.equals("up") || key.equals("down") || key.equals("left") || key.equals("right")) {
			return this.move(key);
		} else if (key.equals(" ")) {
			return this.spawn();
		} else {
			return this;
		}
	}

	@Override // tester
	public TFBoard onTick() {
		// return this.move("right").move("up");
		return this;
	}

	TFBoard smash() {
		return new TFBoard(this.tiles.foldr(new Smash(this.tiles), new MtList<ATiles>()));
	}

	TFBoard spawn() {
		int row = new Random().nextInt(this.boardSize);
		int column = new Random().nextInt(this.boardSize);
		int val = new Random().nextInt(10);

		if (val == 0) {
			val = 4;
		} else {
			val = 2;
		}

		if (this.tiles.foldr(new SpaceTaken(row, column), false)) {
			return this.spawn();
		} else {
			return new TFBoard(new ConsList<>(new NumberTile(val, row, column), this.tiles));
		}
	}

	@Override // tests for world end state
	public WorldEnd worldEnds() {
		if (this.gameEnd()) {
			return new WorldEnd(true, this.makeEndScene());
		} else {
			return new WorldEnd(false, this.makeScene());
		}
	}
}

class TileChecker implements Comparator<ATiles> {
	@Override
	public int compare(ATiles a, ATiles b) {
		if (a.sameTile(b, 0)) {
			return 0;
		} else {
			return 1;
		}
	}
}
