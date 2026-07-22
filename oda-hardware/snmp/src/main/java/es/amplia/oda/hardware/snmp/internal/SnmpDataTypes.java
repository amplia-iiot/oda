package es.amplia.oda.hardware.snmp.internal;

import lombok.extern.slf4j.Slf4j;
import org.snmp4j.smi.*;

@Slf4j
public class SnmpDataTypes {

    public static Variable formatValue(String type, String value) throws IllegalArgumentException {
        return switch (type.toUpperCase()) {
            case "OID" -> new OID(value);
            case "INTEGER" -> new Integer32(Integer.parseInt(value));
            case "STRING" -> new OctetString(value);
            case "GAUGE" -> new Gauge32(Long.parseLong(value));
            case "COUNTER32" -> new Counter32(Long.parseLong(value));
            case "COUNTER64" -> new Counter64(Long.parseLong(value));
            case "TIMETICK" -> new TimeTicks(Long.parseLong(value));
            case "OPAQUE" -> new Opaque(value.getBytes());
            case "IP" -> new IpAddress(value);
            default -> throw new IllegalArgumentException("Unsupported variable type " + type);
        };
    }

    public static Object parseVariable(Variable value) {
        try {
            switch (value.getSyntax()) {
                case SMIConstants.SYNTAX_OBJECT_IDENTIFIER:
                case SMIConstants.SYNTAX_IPADDRESS:
                case SMIConstants.SYNTAX_BITS:
                    return value.toString();
                case SMIConstants.SYNTAX_INTEGER:
                    return value.toInt();
                case SMIConstants.SYNTAX_GAUGE32:
                case SMIConstants.SYNTAX_COUNTER32:
                case SMIConstants.SYNTAX_COUNTER64:
                    return value.toLong();
                case SMIConstants.SYNTAX_TIMETICKS:
                    TimeTicks timeTicks = (TimeTicks) value;
                    return timeTicks.toMilliseconds();
                case SMIConstants.SYNTAX_OPAQUE:
                    Opaque opaque = (Opaque) value;
                    return opaque.toHexString();
                // by default return value as a string
                default:
                    log.error("Unsupported variable type {}, returning as string", value.getSyntaxString());
                    return value.toString();
            }
        } catch (Exception e) {
            log.error("Exception parsing variable : ", e);
            return value.toString();
        }
    }
}
