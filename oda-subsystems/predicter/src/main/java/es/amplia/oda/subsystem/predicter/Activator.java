package es.amplia.oda.subsystem.predicter;

import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.modelimport.keras.exceptions.InvalidKerasConfigurationException;
import org.deeplearning4j.nn.modelimport.keras.exceptions.UnsupportedKerasConfigurationException;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.io.IOException;

public class Activator implements BundleActivator {
	@Override
	public void start(BundleContext bundleContext) throws Exception {
		try {
			// load the model
			String simpleMlp = "/home/debian/model.h5";
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


			/*RecordReader recordReader = new CSVRecordReader(0, ',');
			recordReader.initialize(new FileSplit(new File("/home/adrian/Documentos/git/externaldependencies/oda/oda-subsystems/predicter/src/main/resources/demo.csv")));
			int labelIndex = 2;
			int numClasses = 4;
			int batchSize = 200;
			RecordReaderDataSetIterator iterator = new RecordReaderDataSetIterator(recordReader, batchSize, labelIndex, numClasses);
			iterator.setCollectMetaData(true);
			DataSet allData = iterator.next();
			allData.shuffle();
			SplitTestAndTrain testAndTrain = allData.splitTestAndTrain(0.8D);
			DataSet trainingData = testAndTrain.getTrain();
			DataSet testData = testAndTrain.getTest();
			List<RecordMetaData> trainMetaData = trainingData.getExampleMetaData(RecordMetaData.class);
			List<RecordMetaData> testMetaData = testData.getExampleMetaData(RecordMetaData.class);
			String format = "%-20s\t%s";
			Iterator var13 = trainMetaData.iterator();

			RecordMetaData recordMetaData;
			while(var13.hasNext()) {
				recordMetaData = (RecordMetaData)var13.next();
				System.out.println(String.format(format, recordMetaData.getLocation(), recordMetaData.getURI()));
			}

			var13 = testMetaData.iterator();

			while(var13.hasNext()) {
				recordMetaData = (RecordMetaData)var13.next();
				System.out.println(recordMetaData.getLocation());
			}

			DataNormalization normalizer = new NormalizerStandardize();
			normalizer.fit(trainingData);
			normalizer.transform(trainingData);
			normalizer.transform(testData);
			boolean numInputs = true;
			int outputNum = 4;
			long seed = 6L;
			MultiLayerConfiguration conf = (new NeuralNetConfiguration.Builder()).seed(seed).activation(Activation.TANH).weightInit(WeightInit.XAVIER).updater(new Sgd(0.1D)).l2(1.0E-4D).list().layer(((org.deeplearning4j.nn.conf.layers.DenseLayer.Builder)((org.deeplearning4j.nn.conf.layers.DenseLayer.Builder)(new org.deeplearning4j.nn.conf.layers.DenseLayer.Builder()).nIn(2)).nOut(3)).build()).layer(((org.deeplearning4j.nn.conf.layers.OutputLayer.Builder)((org.deeplearning4j.nn.conf.layers.OutputLayer.Builder)((org.deeplearning4j.nn.conf.layers.OutputLayer.Builder)(new org.deeplearning4j.nn.conf.layers.OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)).activation(Activation.SOFTMAX)).nIn(3)).nOut(outputNum)).build()).build();
			MultiLayerNetwork model = new MultiLayerNetwork(conf);
			model.init();
			model.setListeners(new TrainingListener[]{new ScoreIterationListener(100)});

			for(int i = 0; i < 50; ++i) {
				model.fit(trainingData);
			}

			Evaluation eval = new Evaluation(numClasses);
			INDArray output = model.output(testData.getFeatures());
			eval.eval(testData.getLabels(), output, testMetaData);
			System.out.println(eval.stats());
			List<Prediction> predictionErrors = eval.getPredictionErrors();
			Iterator var23 = predictionErrors.iterator();

			while(var23.hasNext()) {
				Prediction p = (Prediction)var23.next();
				System.out.println("Predicted class: " + p.getPredictedClass() + ", Actual class: " + p.getActualClass() + "\t" + ((RecordMetaData)p.getRecordMetaData(RecordMetaData.class)).getLocation());
			}

			List<RecordMetaData> predictionErrorMetaData = new ArrayList();
			Iterator var38 = predictionErrors.iterator();

			while(var38.hasNext()) {
				Prediction p = (Prediction)var38.next();
				predictionErrorMetaData.add(p.getRecordMetaData(RecordMetaData.class));
			}

			DataSet predictionErrorExamples = iterator.loadFromMetaData(predictionErrorMetaData);
			normalizer.transform(predictionErrorExamples);
			List<Record> predictionErrorRawData = recordReader.loadFromMetaData(predictionErrorMetaData);

			for(int i = 0; i < predictionErrors.size(); ++i) {
				Prediction p = (Prediction)predictionErrors.get(i);
				RecordMetaData meta = (RecordMetaData)p.getRecordMetaData(RecordMetaData.class);
				INDArray features = predictionErrorExamples.getFeatures().getRow((long)i, true);
				INDArray labels = predictionErrorExamples.getLabels().getRow((long)i, true);
				List<Writable> rawData = ((Record)predictionErrorRawData.get(i)).getRecord();
				INDArray networkPrediction = model.output(features);
				System.out.println(meta.getLocation() + ": \tRaw Data: " + rawData + "\tNormalized: " + features + "\tLabels: " + labels + "\tPredictions: " + networkPrediction);
			}

			List<Prediction> list1 = eval.getPredictions(1, 2);
			List<Prediction> list2 = eval.getPredictionByPredictedClass(2);
			List var43 = eval.getPredictionsByActualClass(2);*/
		} catch (IOException var33) {
			var33.printStackTrace();
		} catch (UnsupportedKerasConfigurationException e) {
			e.printStackTrace();
		} catch (InvalidKerasConfigurationException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {

	}
}
