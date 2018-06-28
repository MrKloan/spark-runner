package spark.runner.exceptions;

public class SparkRunnerException extends RuntimeException {
	
	public SparkRunnerException(final String msg) {
		super(msg);
	}
	
	public SparkRunnerException(final Throwable t) {
		super(t);
	}
	
	public SparkRunnerException(final String msg, final Throwable t) {
		super(msg, t);
	}
}
