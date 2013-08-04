/**
 * 
 */
package tml.storage;

import java.io.File;
import java.io.IOException;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import tml.storage.Repository;

/**
 * @author Jorge Villalon
 *
 */
public class TmlCleanupTask extends TimerTask {

	private static Logger logger = Logger.getLogger(TmlCleanupTask.class);
	private Repository repository;
	/**
	 * 
	 */
	public TmlCleanupTask(Repository repo) {
		this.repository = repo;
	}

	/* (non-Javadoc)
	 * @see java.util.TimerTask#run()
	 */
	@Override
	public void run() {
		File lock = new File("tml.cleanup.lock");
		if (lock.exists()) {
			logger.debug("Cleanup Timer still running! Skipping execution.");
		}
		try {
			lock.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Couldn't create annotator lock file");
			return;
		}

		Thread th = repository.cleanup();
		try {
			th.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}

		lock.delete();
	}

}
