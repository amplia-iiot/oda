package es.amplia.oda.dispatcher.opengate;

import es.amplia.oda.dispatcher.opengate.domain.Input;

interface JsonParser {
    Input parseInput(byte[] input) throws IllegalArgumentException;
}
