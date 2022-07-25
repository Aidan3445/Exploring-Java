package src;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import javalib.impworld.World;
import javalib.impworld.WorldScene;
import javalib.worldimages.AboveImage;
import javalib.worldimages.BesideImage;
import javalib.worldimages.CircleImage;
import javalib.worldimages.EmptyImage;
import javalib.worldimages.FrameImage;
import javalib.worldimages.OverlayImage;
import javalib.worldimages.Posn;
import javalib.worldimages.RectangleImage;
import javalib.worldimages.RotateImage;
import javalib.worldimages.TextImage;
import javalib.worldimages.WorldImage;

class Hardle extends World {
  public static void main(String args[]) {
    Hardle world = new Hardle();
    world.bigBang(world.width, world.height);
  }
  int guessCount; // number of guesses made
  ArrayList<WordGuess> guesses; // guessed words, guesses[guessCount] is the current word
  int height = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight() * 3 / 4; // height of the window
  boolean hideEnd;
  Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
  int secretWordIndex; // index of the current secret word
  int wordLength = 5; // number of letters in the words
  int tileSize = (this.height * 4 / 11) / this.wordLength; // size of each tile
  int totalGuesses = 9; // number if guesses you get;
  WordleWords w = new WordleWords();
  int width = this.height / 2; // width of the window
  boolean win;

  Hardle() {
    this.secretWordIndex = (int) ChronoUnit.DAYS.between(LocalDate.parse("2021-06-19"), LocalDate.now());
    this.resetGuesses();
    // System.out.println(this.w.secretWords.get(this.secretWordIndex));
  }

  void resetGuesses() {
    this.win = false;
    this.hideEnd = true;
    this.guessCount = 0;
    this.guesses = new ArrayList<>();
    for (int i = 0; i < this.totalGuesses; i++) {
      this.guesses.add(new WordGuess(this.w.secretWords.get(this.secretWordIndex)));
    }
  }
  
  String getTimer() {
    String hours = Long.toString(ChronoUnit.SECONDS.between(LocalTime.now(), LocalTime.parse("23:59:59")) / 3600);
    String mins = Long.toString(ChronoUnit.SECONDS.between(LocalTime.now(), LocalTime.parse("23:59:59")) / 60 % 60);
    String secs = Long.toString(ChronoUnit.SECONDS.between(LocalTime.now(), LocalTime.parse("23:59:59")) % 60);
    String time = this.addZero(hours) + ":" + this.addZero(mins) + ":" + this.addZero(secs);
    return "Next Hardle " + time;
  }
  
  String addZero(String time) {
    if (time.length() == 1) {
      time = "0" + time;
    }
    return time;
  }

  String score() {
    String guessOrX;
    if (this.guessCount == this.totalGuesses && !this.win) {
      guessOrX = "X";
    } else {
      guessOrX = Integer.toString(this.guessCount);
    }
    String score = "Hardle " + Integer.toString(this.secretWordIndex) + " " + guessOrX + "/"
        + Integer.toString(this.totalGuesses);
    return score;
  }
  
  @Override
  public WorldScene makeScene() {
    WorldScene scene = new WorldScene(this.width, this.height);
    scene = this.draw(scene);
    scene.placeImageXY(new TextImage("Hardle " + Integer.toString(this.secretWordIndex), this.tileSize * 3 / 5,
        new Color(78, 153, 40)), this.width / 2, this.tileSize / 2);
    scene.placeImageXY(new Keyboard(this.height / 5).draw(), this.width / 2, this.height * 10 / 11);
    if (this.win || this.guessCount == this.totalGuesses) {
      scene = this.drawEndScreen(scene);
    }
    return scene;
  }

  WorldScene draw(WorldScene scene) {
    for (int i = 0; i < this.totalGuesses; i++) {
      scene.placeImageXY(this.guesses.get(i).pegs.draw(this.tileSize), this.width / 2 + 3 * this.tileSize,
          i * this.tileSize * 11 / 10 + this.tileSize * 3 / 2);
      for (int j = 0; j < this.wordLength; j++) {
        scene.placeImageXY(this.guesses.get(i).draw(j, this.tileSize),
            this.width / 2 - this.tileSize * 3 + j * this.tileSize + this.width / 25,
            i * this.tileSize * 11 / 10 + this.tileSize * 3 / 2);
      }
    }
    return scene;
  }

  WorldScene drawEndScreen(WorldScene scene) {
    if (!this.hideEnd) {
      WordGuess answer = new WordGuess(this.w.secretWords.get(secretWordIndex));
      answer.word.addAll(answer.secretWord);
      answer.win = true;
      scene.placeImageXY(new RectangleImage(this.width * 9 / 10, this.height * 4 / 5, "solid", Color.black),
          this.width / 2, this.height / 2);
      scene.placeImageXY(new TextImage("Hardle", this.tileSize, Color.white), this.width / 2, this.height / 5);
      scene.placeImageXY(new TextImage(this.getTimer(), this.tileSize / 2, new Color(78, 153, 40)),
          this.width / 2, this.height / 2);
      scene.placeImageXY(new TextImage(this.score(), this.tileSize / 2, Color.white), this.width / 2,
          this.height * 3 / 5);
      scene.placeImageXY(
          new OverlayImage(new TextImage("COPY SCORE", this.tileSize / 2, Color.black), new RectangleImage(
              this.width * 4 / 5, this.tileSize * 5 / 4, "solid", new Color(78, 153, 40))),
          this.width / 2, this.height * 2 / 3);
      scene.placeImageXY(
          new OverlayImage(new TextImage("PLAY AGAIN", this.tileSize / 2, Color.black), new RectangleImage(
              this.width * 4 / 5, this.tileSize * 5 / 4, "solid", new Color(78, 153, 40))),
          this.width / 2, this.height * 4 / 5);
      for (int j = 0; j < this.wordLength; j++) {
        scene.placeImageXY(answer.draw(j, this.tileSize),
            j * this.tileSize + this.tileSize / 2 + this.width / 7, this.height / 3);
      }
    }
    return scene;
  }

  @Override
  public void onKeyEvent(String key) {
    if (this.guessCount < this.totalGuesses && !this.win) {
      WordGuess current = this.guesses.get(this.guessCount);
      if (key.equals("backspace")) {
        current.deleteLetter();
      } else if (key.equals("enter")) {
        if (current.guessMade(this.wordLength, this.w.allWords)) {
          this.win = current.win;
          this.guessCount++;
          if (this.win || this.guessCount == this.totalGuesses) {
            this.hideEnd = false;
          }
        }
      } else {
        current.addLetter(key);
      }
    }
  }

  @Override
  public void onMouseClicked(Posn pos) {
    if (this.win || this.guessCount == this.totalGuesses) {
      if (pos.x > this.width * 1 / 10 && pos.x < this.width * 9 / 10
          && pos.y > this.height * 2 / 3 - this.tileSize * 5 / 8
          && pos.y < this.height * 2 / 3 + this.tileSize * 5 / 8) {
        String copyPaste = this.score();
        ArrayList<String> boxes = new ArrayList<>(Arrays.asList(copyPaste));
        ArrayList<String> emoji = new ArrayList<>(Arrays.asList(Character.toString(9_898),
            Character.toString(128_993), Character.toString(128_994)));
        for (int n = 0; n < this.guessCount; n++) {
          WordGuess guess = this.guesses.get(n);
          String tempBoxes = "";
          for (int i = 0; i < 3; i++) {
            for (int j = 0; j < guess.getPegs().get(i); j++) {
              tempBoxes += emoji.get(i);
            }
          }
          boxes.add(tempBoxes);
          copyPaste += "\r" + tempBoxes;
        }
        StringSelection stringSelection = new StringSelection(copyPaste);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
      }
      if (pos.x > this.width * 1 / 10 && pos.x < this.width * 9 / 10
          && pos.y > this.height * 4 / 5 - this.tileSize * 5 / 8
          && pos.y < this.height * 4 / 5 + this.tileSize * 5 / 8) {
        this.secretWordIndex = new Random().nextInt(2309);
        this.resetGuesses();
      }
      if (!this.hideEnd && (pos.x < this.width / 20 || pos.x > this.width * 19 / 20 || pos.y < this.height / 10
          || pos.y > this.height * 9 / 10)) {
        this.hideEnd = true;
      } else if (this.hideEnd) {
        this.hideEnd = false;
      }
    }
    Keyboard keyboard = new Keyboard(this.height / 5);
    if (pos.y > this.height * 33 / 40 && pos.y < this.height * 35 / 40 && pos.x > this.width / 40
        && pos.x < this.width * 39 / 40) {
      onKeyEvent(keyboard.qwerty.get((pos.x - this.width / 40) / (keyboard.tileWidth * 11 / 10)));
    }
    if (pos.y > this.height * 739 / 840 && pos.y < this.height * 781 / 840 && pos.x > this.width * 3 / 42
        && pos.x < this.width * 39 / 42) {
      onKeyEvent(keyboard.asdf.get((pos.x - this.width * 3 / 42) / (keyboard.tileWidth * 11 / 10)));
    }
    if (pos.y > this.height * 1013 / 1080 && pos.y < this.height * 1067 / 1080) {
      if (pos.x > this.width * 7 / 40 && pos.x < this.width * 33 / 40) {
        onKeyEvent(keyboard.zxcv.get(1 + (pos.x - this.width * 7 / 40) / (keyboard.tileWidth * 11 / 10)));
        System.out.println();
      }
      if (pos.x > this.width * 33 / 40) {
        onKeyEvent("backspace");
      }
      if (pos.x < this.width * 7 / 40) {
        onKeyEvent("enter");
      }
    }
  }
}

class WordGuess {
  boolean guessed;
  Pegs pegs;
  ArrayList<String> secretWord;
  boolean win;
  ArrayList<String> word;

  WordGuess(String wordleWord) {
    this.word = new ArrayList<>();
    this.guessed = false;
    this.secretWord = this.explode(wordleWord);
    this.pegs = new Pegs(0, 0);
    this.win = false;
  }

  void addLetter(String letter) {
    ArrayList<String> alphabet = new ArrayList<>(Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j",
        "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"));
    if (this.word.size() < this.secretWord.size() && alphabet.indexOf(letter.toLowerCase()) != -1) {
      this.word.add(letter.toLowerCase());
    }
  }

  void deleteLetter() {
    if (this.word.size() > 0) {
      this.word.remove(this.word.size() - 1);
    }
  }

  WorldImage draw(int index, int tileSize) {
    String letter;
    if (this.word.size() > index) {
      letter = this.word.get(index);
    } else {
      letter = " ";
    }
    Color color;
    if (this.win) {
      color = new Color(78, 153, 40);
    } else if (this.guessed) {
      color = Color.gray;
    } else {
      color = Color.white;
    }
    return new OverlayImage(new TextImage(letter.toUpperCase(), tileSize, Color.black),
        new FrameImage(new RectangleImage(tileSize, tileSize, "solid", color), Color.black));
  }

  ArrayList<String> explode(String word) {
    return new ArrayList<>(Arrays.asList(word.split("")));
  }

  ArrayList<Integer> getPegs() {
    int yellow = 0;
    int green = 0;
    ArrayList<String> g = new ArrayList<>();
    ArrayList<String> s = new ArrayList<>();
    g.addAll(this.word);
    s.addAll(this.secretWord);
    for (int i = 0; i < s.size(); i++) {
      if (g.get(i).equals(s.get(i))) {
        green++;
        g.set(i, "0");
        s.set(i, "1");
      }
    }
    for (int i = 0; i < s.size(); i++) {
      if (g.contains(s.get(i))) {
        yellow++;
        g.set(g.indexOf(s.get(i)), "0");
        s.set(i, "1");
      }
    }

    this.pegs = new Pegs(yellow, green);
    if (green == 5) {
      this.win = true;
    }
    return new ArrayList<>(Arrays.asList(5 - yellow - green, yellow, green));
  }

  boolean guessMade(int wordLength, ArrayList<String> allWords) {
    String str = "";
    for (String s : this.word) {
      str += s;
    }
    if (this.word.size() == wordLength && allWords.indexOf(str) != -1) {
      this.guessed = true;
      this.getPegs();
    }
    return this.guessed;
  }
}

class Pegs {
  ArrayList<Color> colors;

  Pegs(int y, int g) {
    this.colors = new ArrayList<>(Arrays.asList(Color.white, Color.white, Color.white, Color.white, Color.white));
    for (int i = 0; i < y; i++) {
      this.colors.set(i, new Color(255, 224, 71));
    }
    for (int i = 0; i < g; i++) {
      this.colors.set(i + y, new Color(78, 153, 40));
    }
  }

  WorldImage draw(int tileSize) {
    return new OverlayImage(
        new RotateImage(new OverlayImage(new CircleImage(tileSize / 10, "outline", Color.black),
            new CircleImage(tileSize / 10, "solid", this.colors.get(0))).movePinhole(0,
                tileSize / 4),
            0),
        new OverlayImage(
            new RotateImage(new OverlayImage(new CircleImage(tileSize / 10, "outline", Color.black),
                new CircleImage(tileSize / 10, "solid", this.colors.get(1))).movePinhole(0,
                    tileSize / 4),
                360 / 5),
            new OverlayImage(
                new RotateImage(new OverlayImage(new CircleImage(tileSize / 10, "outline", Color.black),
                    new CircleImage(tileSize / 10, "solid", this.colors.get(2))).movePinhole(0,
                        tileSize / 4),
                    2 * 360 / 5),
                new OverlayImage(
                    new RotateImage(
                        new OverlayImage(new CircleImage(tileSize / 10, "outline", Color.black),
                            new CircleImage(tileSize / 10, "solid", this.colors.get(3)))
                        .movePinhole(0, tileSize / 4),
                        3 * 360 / 5),
                    new RotateImage(
                        new OverlayImage(new CircleImage(tileSize / 10, "outline", Color.black),
                            new CircleImage(tileSize / 10, "solid", this.colors.get(4)))
                        .movePinhole(0, tileSize / 4),
                        4 * 360 / 5)))));
  }
}

class Keyboard {
  ArrayList<String> asdf = new ArrayList<>(Arrays.asList("A", "S", "D", "F", "G", "H", "J", "K", "L"));
  int height;
  ArrayList<String> qwerty = new ArrayList<>(Arrays.asList("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"));

  int tileHeight;
  int tileWidth;
  ArrayList<String> zxcv = new ArrayList<>(
      Arrays.asList("ENTER", "Z", "X", "C", "V", "B", "N", "M", Character.toString(9_003)));

  Keyboard(int height) {
    this.height = height;
    this.tileWidth = height * 2 / 9;
    this.tileHeight = height / 4;
  }

  WorldImage draw() {
    return new AboveImage(this.makeKeyboardLine(this.qwerty),
        new RectangleImage(this.tileWidth, this.tileHeight / 10, "outline", Color.white),
        this.makeKeyboardLine(this.asdf),
        new RectangleImage(this.tileWidth, this.tileHeight / 10, "outline", Color.white),
        this.makeKeyboardLine(this.zxcv),
        new RectangleImage(this.tileWidth, this.tileHeight / 10, "outline", Color.white));
  }

  WorldImage makeKey(String letter) {
    if (letter.equals("ENTER") || letter.equals(Character.toString(9_003))) {
      return new OverlayImage(new TextImage(letter, this.tileWidth * 2 / 5, Color.white),
          new RectangleImage(this.tileWidth * 3 / 2, this.tileHeight, "solid", Color.lightGray));
    }
    return new OverlayImage(new TextImage(letter, this.tileWidth * 9 / 10, Color.white),
        new RectangleImage(this.tileWidth, this.tileHeight, "solid", Color.lightGray));
  }

  WorldImage makeKeyboardLine(ArrayList<String> line) {
    if (line.size() > 0) {
      String letter = "";
      letter += line.get(0);
      line.remove(0);
      return new BesideImage(this.makeKey(letter),
          new BesideImage(new RectangleImage(this.tileWidth / 10, this.tileHeight, "outline", Color.white),
              this.makeKeyboardLine(line)));
    } else {
      return new EmptyImage();
    }
  }
}


