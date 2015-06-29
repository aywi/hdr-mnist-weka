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
		String name = "train";
		int numOutputImages = 100;
		DataInputStream labels = new DataInputStream(new FileInputStream(
				"data/" + name + "-labels.idx1-ubyte"));
		DataInputStream images = new DataInputStream(new FileInputStream(
				"data/" + name + "-images.idx3-ubyte"));
		FileOutputStream array = new FileOutputStream(name + ".arff");
		PrintStream parray = new PrintStream(array);
		int magicNumber = labels.readInt();
		if (magicNumber != 2049) {
			System.err.println("Label file has wrong magic number: "
					+ magicNumber + " (should be 2049)");
			System.exit(0);
		}
		magicNumber = images.readInt();
		if (magicNumber != 2051) {
			System.err.println("Image file has wrong magic number: "
					+ magicNumber + " (should be 2051)");
			System.exit(0);
		}
		int numLabels = labels.readInt();
		int numImages = images.readInt();
		int numRows = images.readInt();
		int numCols = images.readInt();
		if (numLabels != numImages) {
			System.err
					.println("Image file and label file do not contain the same number of entries.");
			System.err.println("  Label file contains: " + numLabels);
			System.err.println("  Image file contains: " + numImages);
			System.exit(0);
		}
		long start = System.currentTimeMillis();
		int numLabelsRead = 0;
		int numImagesRead = 0;
		parray.println("@relation " + name);
		parray.println();
		for (int pix = 0; pix < (numCols / 2) * (numRows / 2); pix++) {
			parray.println("@attribute pix" + pix + " {0,1}");
		}
		parray.println("@attribute label {0,1,2,3,4,5,6,7,8,9}");
		parray.println();
		parray.println("@data");
		BufferedImage origin = new BufferedImage(numCols, numRows,
				BufferedImage.TYPE_BYTE_GRAY);
		BufferedImage processed = new BufferedImage(numCols / 2, numRows / 2,
				BufferedImage.TYPE_BYTE_BINARY);
		while (labels.available() > 0 && numLabelsRead < numLabels) {
			byte label = labels.readByte();
			numLabelsRead++;
			int[][] image = new int[numCols][numRows];
			for (int rowIdx = 0; rowIdx < numCols; rowIdx++) {
				for (int colIdx = 0; colIdx < numRows; colIdx++) {
					image[colIdx][rowIdx] = images.readUnsignedByte();
					if (numLabelsRead < numOutputImages)
						origin.setRGB(colIdx, rowIdx,
								image[colIdx][rowIdx] * 0x010101);
				}
			}
			numImagesRead++;
			int[][] process = new int[numCols / 2][numRows / 2];
			for (int rowIdx = 0; rowIdx < numCols / 2; rowIdx++) {
				for (int colIdx = 0; colIdx < numRows / 2; colIdx++) {
					if (image[colIdx * 2][rowIdx * 2]
							+ image[colIdx * 2 + 1][rowIdx * 2]
							+ image[colIdx * 2][rowIdx * 2 + 1]
							+ image[colIdx * 2 + 1][rowIdx * 2 + 1] > 509)
						process[colIdx][rowIdx] = 1;
					else
						process[colIdx][rowIdx] = 0;
					parray.print(process[colIdx][rowIdx] + ",");
					if (numLabelsRead < numOutputImages)
						processed.setRGB(colIdx, rowIdx,
								process[colIdx][rowIdx] * 0xffffff);
				}
			}
			if (numLabelsRead % 10 == 0) {
				System.out.print(".");
			}
			if ((numLabelsRead % 800) == 0) {
				System.out.print(" " + numLabelsRead + " / " + numLabels);
				long end = System.currentTimeMillis();
				long elapsed = end - start;
				long minutes = elapsed / (1000 * 60);
				long seconds = (elapsed / 1000) - (minutes * 60);
				System.out.println("  " + minutes + " m " + seconds + " s ");
			}
			if (numLabelsRead == numImagesRead)
				parray.println(label);
			if (numLabelsRead < numOutputImages) {
				File originalimg = new File("data/" + name + "/in/"
						+ numLabelsRead + ".bmp");
				if (!originalimg.getParentFile().exists()) {
					originalimg.getParentFile().mkdirs();
				}
				ImageIO.write(origin, "BMP", originalimg);
				File processedimg = new File("data/" + name + "/out/"
						+ numLabelsRead + ".bmp");
				if (!processedimg.getParentFile().exists()) {
					processedimg.getParentFile().mkdirs();
				}
				ImageIO.write(processed, "BMP", processedimg);
			}
		}
		labels.close();
		images.close();
		array.close();
		System.out.println();
		long end = System.currentTimeMillis();
		long elapsed = end - start;
		long minutes = elapsed / (1000 * 60);
		long seconds = (elapsed / 1000) - (minutes * 60);
		System.out.println("Read " + numLabelsRead + " samples in " + minutes
				+ " m " + seconds + " s ");
	}
}
