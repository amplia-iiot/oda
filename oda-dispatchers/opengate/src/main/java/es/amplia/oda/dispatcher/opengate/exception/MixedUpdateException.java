package es.amplia.oda.dispatcher.opengate.exception;

/*
 *  Exception thrown in an Update Operation if request passes configuration and rules files at the same operation
 */
public class MixedUpdateException extends RuntimeException {

	public MixedUpdateException(String detailedMessage) {
		super(detailedMessage);
	}
}
