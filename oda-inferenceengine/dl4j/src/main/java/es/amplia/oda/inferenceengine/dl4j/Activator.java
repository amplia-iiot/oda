package es.amplia.oda.inferenceengine.dl4j;

import es.amplia.oda.core.commons.utils.ConfigurableBundle;
import es.amplia.oda.inferenceengine.api.InferenceEngine;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.cpu.nativecpu.CpuBackend;
import org.nd4j.linalg.factory.Nd4j;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

	private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

	private ConfigurableBundle configurableBundle;
	private Dl4jInferenceEngine inferenceEngine;
	private ServiceRegistration<InferenceEngine> ruleEngineServiceRegistration;

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		Thread.currentThread().setContextClassLoader(CpuBackend.class.getClassLoader());

		// load the model
		String simpleMlp = "/home/adrian/model.h5";
		double[] data = {10,115,0,0,0,35.3,0.134,29};
		double[] data2 = {2,197,70,45,543,30.5,0.158,53};
		MultiLayerNetwork model = KerasModelImport.
				importKerasSequentialModelAndWeights(simpleMlp);
		ComputationGraph model1 = KerasModelImport.importKerasModelAndWeights(simpleMlp);
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
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {

	}
}
