package es.amplia.oda.inferenceengine.api;

public interface InferenceEngine {

	void createDatastreamDirectory(String toString);
	void deleteDatastreamDirectory(String toString);

	void importModel(String s);
	void removeImportedModel(String s);
}
