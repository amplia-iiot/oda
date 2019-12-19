package es.amplia.oda.ruleengine.api;

import es.amplia.oda.core.commons.utils.State;
import es.amplia.oda.core.commons.utils.DatastreamValue;
import lombok.Value;

import javax.script.ScriptException;

@Value
public class Rule {
	String name;
	String datastreamId;
	ScriptTranslator script;

	public Rule(String name, String datastreamId, ScriptTranslator script) throws ScriptException {
		this.name = name;
		this.datastreamId = datastreamId;
		this.script = script;

		script.initScript(name);
	}

	public boolean when(State state, DatastreamValue value) {
		return (boolean) script.runMethod(name, "when", state, value);
	}

	public State then(State state, DatastreamValue value) {
		return (State) script.runMethod(name, "then", state, value);
	}
}
