/*package es.amplia.oda.subsystem.predicter;

import org.datavec.api.records.Record;
import org.datavec.api.records.metadata.RecordMetaData;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.datavec.api.writable.Writable;
import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.modelimport.keras.exceptions.InvalidKerasConfigurationException;
import org.deeplearning4j.nn.modelimport.keras.exceptions.UnsupportedKerasConfigurationException;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.datavec.api.records.Record;
import org.datavec.api.records.metadata.RecordMetaData;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.datavec.api.writable.Writable;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.evaluation.meta.Prediction;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.dataset.api.DataSet;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.nd4j.linalg.learning.config.Sgd;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
	public Main() {
	}

	public static void main(String[] args) {
		try {
			// load the model
			String simpleMlp = "/home/adrian/Documentos/git/externaldependencies/oda/oda-subsystems/predicter/src/main/resources/model.h5";
			double[] data = {10,115,0,0,0,35.3,0.134,29};
			double[] data2 = {2,197,70,45,543,30.5,0.158,53};
			MultiLayerNetwork model = KerasModelImport.
					importKerasSequentialModelAndWeights(simpleMlp);
			// make a random sample
			int inputs = 8;
			INDArray features = Nd4j.zeros(2, inputs);
			for (int i=0; i<inputs; i++) {
				features.putScalar(0, i, data[i]);
				features.putScalar(1, i, data2[i]);
			}
			// get the prediction
			double prediction = model.output(features).getDouble(1);
			System.out.println(prediction);
		} catch (IOException var33) {
			var33.printStackTrace();
		} catch (UnsupportedKerasConfigurationException e) {
			e.printStackTrace();
		} catch (InvalidKerasConfigurationException e) {
			e.printStackTrace();
		}

	}
}*/
