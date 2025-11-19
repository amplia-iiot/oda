package es.amplia.oda.operation.api.engine;

import java.util.Map;
import javax.script.ScriptException;
import es.amplia.oda.core.commons.utils.OsgiContext;

public class OperationScript {

    String name;
	OperationScriptTranslator script;

	public OperationScript (String name, OperationScriptTranslator script) throws ScriptException {
		this.name = name;
		this.script = script;
		script.initScript(name);
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> execute(String deviceId, String operationId, Map<String, Object> params, OsgiContext ctx) {
		Object ret = script.runMethod(name, "execute", deviceId, operationId, params, ctx);
		if ( (ret == null) || ( (ret instanceof String) && ((String)ret).isEmpty() ) ) return null; // Cuando se devuelve null en el script no lo hace con la clase Map, por eso primero lo comprobamos
		return (Map<String, Object>) ret;
	}
}
