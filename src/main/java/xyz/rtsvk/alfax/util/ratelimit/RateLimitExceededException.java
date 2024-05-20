package xyz.rtsvk.alfax.util.ratelimit;

/**
 * Exception to be thrown when the rate limit is exceeded
 * @author Jastrobaron
 */
public class RateLimitExceededException extends Exception {

	/**
	 * @param rateLimitReached exception message
	 */
	public RateLimitExceededException(String message) {
		super(message);
	}
}
