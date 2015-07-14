package hdr.mnist.weka;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import javax.imageio.ImageIO;

public class MNISTPreprocessing {
	public static void main(String[] args) throws IOException {
		String dataset = "train"; // "train" for the training set and "t10k" for the test set.
		int firstExample = 5001; // Index of the first output example.
		int lastExample = 5100; // Index of the last output example.
		int meanFilterSize = 3; // Size of the mean filter.
		long start = System.currentTimeMillis();
		DataInputStream labels = new DataInputStream(new FileInputStream("data/" + dataset + "-labels.idx1-ubyte"));
		DataInputStream images = new DataInputStream(new FileInputStream("data/" + dataset + "-images.idx3-ubyte"));
		int magicNumber = labels.readInt();
		if (magicNumber != 2049) {
			System.err.println("The label file has wrong magic number: " + magicNumber + " (should be 2049)");
			System.exit(0);
		}
		magicNumber = images.readInt();
		if (magicNumber != 2051) {
			System.err.println("The image file has wrong magic number: " + magicNumber + " (should be 2051)");
			System.exit(0);
		}
		int numLabels = labels.readInt();
		int numImages = images.readInt();
		int numRows = images.readInt();
		int numCols = images.readInt();
		if (numLabels != numImages) {
			System.err.println("The label file and the image file do not contain the same number of items.");
			System.err.println("The label file contains: " + numLabels);
			System.err.println("The image file contains: " + numImages);
			System.exit(0);
		}
		int numLabelsRead = 0;
		int numImagesRead = 0;
		FileOutputStream arff = new FileOutputStream("data/" + dataset + ".arff");
		PrintStream toArff = new PrintStream(arff);
		toArff.println("@relation " + dataset);
		toArff.println();
		for (int pixel = 1; pixel <= numRows * numCols; pixel++) {
			toArff.println("@attribute pixel" + pixel + " {0,1}");
		}
		toArff.println("@attribute label {0,1,2,3,4,5,6,7,8,9}");
		toArff.println();
		toArff.println("@data");
		BufferedImage originalExample = new BufferedImage(numCols, numRows, BufferedImage.TYPE_BYTE_GRAY);
		BufferedImage processedExample = new BufferedImage(numCols, numRows, BufferedImage.TYPE_BYTE_BINARY);
		while (labels.available() > 0 && numLabelsRead < numLabels) {
			byte label = labels.readByte();
			numLabelsRead++;
			int[][] image = new int[numRows][numCols];
			for (int row = 0; row < image.length; row++) {
				for (int col = 0; col < image[row].length; col++) {
					image[row][col] = images.readUnsignedByte();
					if (numLabelsRead >= firstExample && numLabelsRead <= lastExample)
						originalExample.setRGB(col, row, 0xffffffff - image[row][col] * 0x010101);
				}
			}
			numImagesRead++;
			int[][] blurred = MeanFilter.blur(image, numRows, numCols, meanFilterSize);
			int[][] binarized = OtsuThresholding.binarize(blurred, numRows, numCols);
			for (int row = 0; row < binarized.length; row++) {
				for (int col = 0; col < binarized[row].length; col++) {
					toArff.print(binarized[row][col] + ",");
					if (numLabelsRead >= firstExample && numLabelsRead <= lastExample)
						processedExample.setRGB(col, row, 0xffffffff - binarized[row][col] * 0xffffff);
				}
			}
			if (numLabelsRead == numImagesRead)
				toArff.println(label);
			if (numLabelsRead >= firstExample && numLabelsRead <= lastExample) {
				File originalOutput = new File("data/examples/" + dataset + "_original/" + numLabelsRead + ".bmp");
				if (!originalOutput.getParentFile().exists()) {
					originalOutput.getParentFile().mkdirs();
				}
				ImageIO.write(originalExample, "BMP", originalOutput);
				File processedOutput = new File("data/examples/" + dataset + "_processed/" + numLabelsRead + ".bmp");
				if (!processedOutput.getParentFile().exists()) {
					processedOutput.getParentFile().mkdirs();
				}
				ImageIO.write(processedExample, "BMP", processedOutput);
			}
			if (numLabelsRead % 20 == 0) {
				System.out.print(">");
			}
			if (numLabelsRead % 1000 == 0) {
				System.out.print(" " + numLabelsRead + "/" + numLabels);
				long end = System.currentTimeMillis();
				long elapsed = end - start;
				long minutes = elapsed / (1000 * 60);
				long seconds = elapsed / 1000 - minutes * 60;
				System.out.println(" " + minutes + "'" + seconds + "''");
			}
		}
		labels.close();
		images.close();
		arff.close();
		long end = System.currentTimeMillis();
		long elapsed = end - start;
		long minutes = elapsed / (1000 * 60);
		long seconds = elapsed / 1000 - minutes * 60;
		System.out.println("Preprocessed " + numLabelsRead + " items in " + minutes + "'" + seconds + "''.");
	}
}
