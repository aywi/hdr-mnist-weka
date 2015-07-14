package hdr.mnist.weka;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class WekaTesting {
	public static void main(String[] args) throws IOException, Exception {
		FileOutputStream results = new FileOutputStream("data/results.txt", true);
		PrintStream toResults = new PrintStream(results);
		System.out.println("=== Weka Testing ===");
		toResults.println("=== Weka Testing ===");
		System.out.println("Loading...");
		long start = System.currentTimeMillis();
		Instances trainingSet = DataSource.read("data/train.arff");
		trainingSet.setClassIndex(trainingSet.numAttributes() - 1);
		Instances testSet = DataSource.read("data/t10k.arff");
		testSet.setClassIndex(testSet.numAttributes() - 1);
		long end = System.currentTimeMillis();
		System.out.println("Loaded datasets in " + (end - start) / 1000. + "s.");
		System.out.println("Instances in Training Set: " + trainingSet.numInstances());
		toResults.println("Instances in Training Set: " + trainingSet.numInstances());
		System.out.println("Instances in Test Set: " + testSet.numInstances());
		toResults.println("Instances in Test Set: " + testSet.numInstances());
		System.out.println("=== Naive Bayes ===");
		toResults.println("=== Naive Bayes ===");
		NaiveBayes naiveBayes = new NaiveBayes();
		evaluate(naiveBayes, trainingSet, testSet, toResults);
		System.out.println("=== Decision Tree (C4.5) ===");
		toResults.println("=== Decision Tree (C4.5) ===");
		J48 decisionTree = new J48();
		evaluate(decisionTree, trainingSet, testSet, toResults);
		for (int i = 10; i <= 200; i += 10) {
			System.out.println("=== Random Forest (with " + i + " Trees) ===");
			toResults.println("=== Random Forest (with " + i + " Trees) ===");
			RandomForest randomForest = new RandomForest();
			randomForest.setNumFeatures((int) Math.floor(Math.sqrt(trainingSet.numAttributes() - 1)));
			randomForest.setNumTrees(i);
			evaluate(randomForest, trainingSet, testSet, toResults);
		}
		toResults.println();
		toResults.close();
	}

	public static void evaluate(Classifier classifier, Instances trainingSet, Instances testSet, PrintStream toResults)
			throws Exception {
		System.gc();
		System.out.println("Training...");
		long start = System.currentTimeMillis();
		classifier.buildClassifier(trainingSet);
		long end = System.currentTimeMillis();
		double trainingTime = (end - start) / 1000.;
		System.out.println("Training Time: " + trainingTime + "s");
		toResults.println("Training Time: " + trainingTime + "s");
		System.gc();
		Evaluation evaluation = new Evaluation(trainingSet);
		System.out.println("Evaluating...");
		start = System.currentTimeMillis();
		evaluation.evaluateModel(classifier, testSet);
		end = System.currentTimeMillis();
		double evaluationTime = (end - start) / 1000.;
		System.out.println("Evaluation Time: " + evaluationTime + "s");
		toResults.println("Evaluation Time: " + evaluationTime + "s");
		System.out.println("Total Time: " + (trainingTime + evaluationTime) + "s");
		toResults.println("Total Time: " + (trainingTime + evaluationTime) + "s");
		System.out.println("Correctly Classified Instances: " + (int) evaluation.correct());
		toResults.println("Correctly Classified Instances: " + (int) evaluation.correct());
		System.out.println("Incorrectly Classified Instances: " + (int) evaluation.incorrect());
		toResults.println("Incorrectly Classified Instances: " + (int) evaluation.incorrect());
		System.out.println("Total Number of Instances: " + (int) evaluation.numInstances());
		toResults.println("Total Number of Instances: " + (int) evaluation.numInstances());
		System.out.println("Accuracy: " + evaluation.pctCorrect() + "%");
		toResults.println("Accuracy: " + evaluation.pctCorrect() + "%");
		System.gc();
	}
}
