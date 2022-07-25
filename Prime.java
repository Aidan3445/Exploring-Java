package src;

import java.util.ArrayList;
import java.util.List;

public class Prime {
	public static void main(String args[]) {
		long startTime = System.currentTimeMillis();
		for (int k = 0; k < 1; k++) {
			List<Integer> x = new ArrayList<>();
			x.add(2);
			for (int i = 3; i < 100000; i += 2) {
				for (int j = 0; x.get(j) <= i * i; j++) {
					// System.out.println(String.valueOf(i) + " % " + String.valueOf(x.get(j)) + " =
					// " + String.valueOf(i % x.get(j)));
					// System.out.println(x.size());
					if (i % x.get(j) == 0) {
						break;
					} else if (j + 1 == x.size()) {
						x.add(i);
						// System.out.println(x);
					}
				}
			}
			System.out.println(x);
		}
		long endTime = System.currentTimeMillis();
		long duration = (endTime - startTime);
		System.out.println(duration);
	}
}
