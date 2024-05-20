package xyz.rtsvk.alfax.util.ratelimit;

/**
 * Class that implements a semaphore-like structure
 * @author Jastrobaron
 */
public class RateLimiter {

	/**
	 * Semaphore value
	 */
	private int count;


	/**
	 * Class constructor
	 * @param count initial semaphore value
	 */
	public RateLimiter(int count) {
		this.count = count;
	}


	/**
	 * Decrements the semaphore value
	 * @throws RateLimitExceededException if the semaphore value goes non-positive
	 */
	public synchronized void lock() throws RateLimitExceededException {
		if (this.count <= 0) {
			throw new RateLimitExceededException("Rate limit reached");
		} else {
			this.count--;
		}
	}

	/**
	 * Increments the semaphore value
	 */
	public synchronized void unlock() {
		this.count++;
	}
}
