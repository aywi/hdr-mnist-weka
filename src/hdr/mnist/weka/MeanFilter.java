package hdr.mnist.weka;

public class MeanFilter {
	public static int[][] blur(int[][] image, int numRows, int numCols, int filterSize) {
		int[][] padding = new int[numRows + filterSize / 2 * 2][numCols + filterSize / 2 * 2];
		for (int row = filterSize / 2; row < padding.length - filterSize / 2; row++) {
			for (int col = filterSize / 2; col < padding[row].length - filterSize / 2; col++)
				padding[row][col] = image[row - filterSize / 2][col - filterSize / 2];
		}
		for (int row = 0; row < image.length; row++) {
			for (int col = 0; col < image[row].length; col++) {
				int sum = 0;
				for (int i = 0; i < filterSize; i++) {
					for (int j = 0; j < filterSize; j++)
						sum += padding[row + i][col + j];
				}
				image[row][col] = sum / (filterSize * filterSize);
			}
		}
		return image;
	}
}
