package es.amplia.oda.core.commons.interfaces;

import java.lang.reflect.Type;

public interface DatastreamSetterTypeMapper {
	Type getTypeOf(String id); //Must return null in case 'id' is not found in the system
}
