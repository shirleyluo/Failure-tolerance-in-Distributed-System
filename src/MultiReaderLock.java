
public class MultiReaderLock {
	
	// TODO: Add any necessary members here.
	private int readers;
	private int writers;

	/**
	 * Initializes a multi-reader (single-writer) lock.
	 */
	public MultiReaderLock() {
		// TODO: Initialize members.
		readers = 0;
		writers = 0;
	}

	/**
	 * Will wait until there are no active writers in the system, and then will
	 * increase the number of active readers.
	 */
	public synchronized void lockRead() {
		// TODO: Fill in. Do not modify method signature.
		while(writers > 0){
			try{
				this.wait();
			}catch(InterruptedException e){
				System.out.println("Please wait unitl there are no active writers." + e.getMessage());
			}
		}
		readers++;
	}

	/**
	 * Will decrease the number of active readers, and notify any waiting
	 * threads if necessary.
	 */
	public synchronized void unlockRead() {
		// TODO: Fill in. Do not modify method signature.
		readers--;
		notifyAll();
	}

	/**
	 * Will wait until there are no active readers or writers in the system, and
	 * then will increase the number of active writers.
	 */
	public synchronized void lockWrite() {
		// TODO: Fill in. Do not modify method signature.
		while(readers > 0 || writers > 0){
			try{
				this.wait();
			}catch(InterruptedException e){
				System.out.println("Please wait until there are no active readers or writers." + e.getMessage());
			}
		}
		writers++;
	}

	/**
	 * Will decrease the number of active writers, and notify any waiting
	 * threads if necessary.
	 */
	public synchronized void unlockWrite() {
		// TODO: Fill in. Do not modify method signature.
		writers--;
		notifyAll();
	}

}
