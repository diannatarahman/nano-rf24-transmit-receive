package processing;

public class Statistics {
	
	private Statistics() {
	}
	
	public static int maxIndex(double[] array, boolean max, int from, int to) {
		if (array == null || array.length == 0) {
			return -1;
		}
		if (from > to) {
			int temp = from;
			from = to;
			to = temp;
		}
		if (from < 0) {
			from = 0;
		}
		else if (from >= array.length) {
			return -1;
		}
		if (to >= array.length) {
			to = array.length;
		}
		else if (to < 0) {
			return -1;
		}
		int mi = from;
		for(int i=from+1; i<to; i++) {
			if ((!Double.isNaN(array[i]) &&
					Double.compare(array[i], array[mi]) == (max ? 1 : -1)) ||
					Double.isNaN(array[mi])) {
				mi = i;
			}
		}
		return mi;
	}

	public static int maxIndex(double[] array, boolean max) {
		return maxIndex(array, max, 0, array.length);
	}

	public static double average(double[] array, int from, int to) {
		if (array == null || array.length == 0) {
			return Double.NaN;
		}
		if (from > to) {
			int temp = from;
			from = to;
			to = temp;
		}
		if (from < 0) {
			from = 0;
		}
		else if (from >= array.length) {
			return Double.NaN;
		}
		if (to >= array.length) {
			to = array.length;
		}
		else if (to < 0) {
			return Double.NaN;
		}
		double a1 = Double.NaN;
		int a2 = 1;
		for(int i=from; i<to; i++) {
			if (Double.isNaN(a1)) {
				a2 = 1;
				a1 = array[i];
			}
			else if (!Double.isNaN(array[i])) {
				a2++;
				a1 += array[i];
			}
		}
		return a1/a2;
	}

	public static double average(double[] array) {
		return average(array, 0, array.length);
	}

}
