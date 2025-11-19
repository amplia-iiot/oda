package es.amplia.oda.ruleengine.api;

import es.amplia.oda.core.commons.utils.State;
import es.amplia.oda.core.commons.utils.DatastreamValue;
import es.amplia.oda.core.commons.utils.OsgiContext;
import lombok.Value;

import javax.script.ScriptException;
import java.util.List;

@Value
public class Rule {
	String name;
	List<String> datastreamIds;
	ScriptTranslator script;

	public Rule(String name, List<String> datastreamIds, ScriptTranslator script) throws ScriptException {
		this.name = name;
		this.script = script;
		this.datastreamIds = datastreamIds;
		script.initScript(name);
	}

	public boolean when(State state, DatastreamValue value, OsgiContext ctx) {
		return (boolean) script.runMethod(name, "when", state, value, ctx);
	}

	public State then(State state, DatastreamValue value, OsgiContext ctx) {
		return (State) script.runMethod(name, "then", state, value, ctx);
	}
}
