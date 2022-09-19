package src;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BuildList {
	public static void main(String args[]) {
		List<Integer> x = new ArrayList<>();
		for (int i = 1; x.size() < 10; i += i) {
			x.add(i * i);
		}
		System.out.println(x);

		List<Integer> y = new ArrayList<>();
		for (int j = 0; y.size() < 10; j++) {
			Random t = new Random();
			y.add(t.nextInt(100));
		}
		System.out.println(y);
	}
}
