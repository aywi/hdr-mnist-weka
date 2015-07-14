package hdr.mnist.weka;

public class OtsuThresholding {
	public static int[] imageHistogram(int[][] image, int numRows, int numCols) {
		int[] histogram = new int[256];
		for (int i = 0; i < histogram.length; i++)
			histogram[i] = 0;
		for (int row = 0; row < image.length; row++) {
			for (int col = 0; col < image[row].length; col++)
				histogram[image[row][col]]++;
		}
		return histogram;
	}

	public static int otsuThreshold(int[][] image, int numRows, int numCols) {
		int[] histogram = imageHistogram(image, numRows, numCols);
		int total = numRows * numCols;
		float sum = 0;
		for (int i = 0; i < histogram.length; i++)
			sum += i * histogram[i];
		float sumB = 0;
		int wB = 0;
		int wF = 0;
		float varMax = 0;
		int threshold = 0;
		for (int i = 0; i < histogram.length; i++) {
			wB += histogram[i];
			if (wB == 0)
				continue;
			wF = total - wB;
			if (wF == 0)
				break;
			sumB += (float) (i * histogram[i]);
			float mB = sumB / wB;
			float mF = (sum - sumB) / wF;
			float varBetween = (float) wB * (float) wF * (mB - mF) * (mB - mF);
			if (varBetween > varMax) {
				varMax = varBetween;
				threshold = i;
			}
		}
		return threshold;
	}

	public static int[][] binarize(int[][] image, int numRows, int numCols) {
		int threshold = otsuThreshold(image, numRows, numCols);
		for (int row = 0; row < image.length; row++) {
			for (int col = 0; col < image[row].length; col++) {
				if (image[row][col] > threshold) {
					image[row][col] = 1;
				} else {
					image[row][col] = 0;
				}
			}
		}
		return image;
	}
}
