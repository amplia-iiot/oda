package es.amplia.oda.operation.createrule;

import es.amplia.oda.operation.api.OperationCreateRule;
import es.amplia.oda.operation.createrule.configuration.RuleCreatorConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class OperationCreateRuleImpl implements OperationCreateRule {

	private static final Logger LOGGER = LoggerFactory.getLogger(OperationCreateRuleImpl.class);

	private static final String NAMERULE = "namerule";
	private static final String DATASTREAM_ID = "datastreamId";
	private static final String WHEN = "when";
	private static final String THEN = "then";

	private String path = "";

	@Override
	public CompletableFuture<Result> createRule(String deviceId, Map<String, String> ruleInfo) {
		LOGGER.info("Creating rule '{}' from Opengate", ruleInfo.get(NAMERULE));
		boolean createdNow = false;

		File dir = new File (path + ruleInfo.get(DATASTREAM_ID));
		if(!dir.exists()) {
			dir.mkdir();
			createdNow = true;
		}

		File file = new File (path + ruleInfo.get(DATASTREAM_ID) + FileSystems.getDefault().getSeparator() + ruleInfo.get(NAMERULE) + ".js");
		if(!file.exists()) {
			try (FileOutputStream rule = new FileOutputStream(path + ruleInfo.get(DATASTREAM_ID) + "/" + ruleInfo.get(NAMERULE) + ".js")) {
				String oldchar = ";";
				String newchar = ";\n\t";
				rule.write(("load(\"" + path + "utils.js\");\n\n").getBytes());

				rule.write(("function when (state, value) {\n\t").getBytes());
				rule.write(ruleInfo.get(WHEN)
						.substring(0, ruleInfo.get(WHEN).lastIndexOf(oldchar))
						.replaceAll(oldchar , newchar)
						.concat(ruleInfo.get(WHEN).substring(ruleInfo.get(WHEN).lastIndexOf(oldchar)))
						.getBytes());
				rule.write(("\n}\n\n").getBytes());

				rule.write(("function then (state, value) {\n\t").getBytes());
				rule.write(ruleInfo.get(THEN)
						.substring(0, ruleInfo.get(THEN).lastIndexOf(oldchar))
						.replaceAll(oldchar , newchar)
						.concat(ruleInfo.get(THEN).substring(ruleInfo.get(THEN).lastIndexOf(oldchar)))
						.getBytes());
				rule.write(("\n}").getBytes());
			} catch (IOException e) {
				LOGGER.error("Something went wrong while rule {} was being created. " +
						"The temp file that was be created until now will be deleted", ruleInfo.get(NAMERULE));
				file = new File(path + ruleInfo.get(DATASTREAM_ID) + FileSystems.getDefault().getSeparator() + ruleInfo.get(NAMERULE) + ".js");
				file.delete();
				if(createdNow) {
					dir.delete();
				}
				return CompletableFuture.completedFuture(new Result(Status.ERROR_CREATING,
						"Error trying to create the rule."));
			}
			return CompletableFuture.completedFuture(new Result(Status.OK,
					"Rule '" + ruleInfo.get(NAMERULE) +
							"' has been created for datastream '" + ruleInfo.get(DATASTREAM_ID) + "'"));
		}
		return CompletableFuture.completedFuture(new Result(Status.ALREADY_EXISTS,
				"Error trying to create the rule. '" + ruleInfo.get(NAMERULE) +
						"' is in use for datastream '" + ruleInfo.get(DATASTREAM_ID) + "'"));
	}

	public void loadConfiguration(RuleCreatorConfiguration config) {
		this.path = config.getPath();
	}
}
