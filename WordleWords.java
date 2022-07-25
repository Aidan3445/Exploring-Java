package src;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class WordleWords {
	ArrayList<String> allWords = new ArrayList<>();
	ArrayList<String> secretWords = new ArrayList<>();

	WordleWords() {
		try {
			this.setSecretWords();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			this.setAllWords();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void setAllWords() throws IOException {
		File stream = new File(
						"src/resources/fiveLetterWords.txt");
		Scanner input = null;
		try {
			input = new Scanner(stream);
		} catch (Exception e) {
			// TODO Auto-generated catch block
		}
		while (input.hasNextLine()) {
			this.allWords.add(input.next());
		}
	}

	void setSecretWords() throws IOException {
		File stream = new File(
						"src/resources/secretWords.txt");
		Scanner input = null;
		try {
			input = new Scanner(stream);
		} catch (Exception e) {
			// TODO Auto-generated catch block
		}
		while (input.hasNextLine()) {
			this.secretWords.add(input.next());
		}
	}
}
