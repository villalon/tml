/**
 * 
 */
package tml.storage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;

import tml.storage.Repository;
import tml.storage.RepositoryEvent;
import tml.storage.RepositoryListener;

/**
 * @author Jorge Villalon
 *
 */
public class TmlIndexerTask extends TimerTask {

	private static Logger logger = Logger.getLogger(TmlIndexerTask.class);
	private Repository repository;
	private String uploadFolder;
	public String getUploadFolder() {
		return uploadFolder;
	}

	public void setUploadFolder(String uploadFolder) {
		this.uploadFolder = uploadFolder;
	}

	public int getMaxFilesToProcess() {
		return maxFilesToProcess;
	}

	public void setMaxFilesToProcess(int maxFilesToProcess) {
		this.maxFilesToProcess = maxFilesToProcess;
	}

	private int maxFilesToProcess = 1;
	/**
	 * 
	 */
	public TmlIndexerTask(Repository repo) {
		this.repository = repo;
	}

	/* (non-Javadoc)
	 * @see java.util.TimerTask#run()
	 */
	@Override
	public void run() {
		File lock = new File("tml.indexer.lock");
		if (lock.exists()) {
			logger.debug("Timer still running! Skipping execution.");
		}
		try {
			lock.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Couldn't create indexer lock file");
			return;
		}

		List<File> filesToProcess = getFilesToProcess();

		if (filesToProcess.size() > 0) {
			RepositoryListener listener = new RepositoryListener() {				
				@Override
				public void repositoryAction(RepositoryEvent evt) {
					//logger.debug(evt.getAction() + " " + evt.getCurrent() + " of " + evt.getMaximum());
				}
			};
			repository.addRepositoryListener(listener);
			for (File processed : filesToProcess) {
				File newFile = new File(
						this.uploadFolder + "/"
						+ processed.getName());
				boolean success = processed.renameTo(newFile);
				if(!success) {
					logger.debug("Couldn't move file " + processed + " to processing foler. Skipping.");
					continue;
				}
				File[] fileList = new File[1];
				fileList[0] = newFile;
				try {
					repository.addDocumentsInList(fileList);
				} catch (LockObtainFailedException e) {
					e.printStackTrace();
					logger.error(e.getMessage());
					newFile.renameTo(processed);
					continue;
				} catch (CorruptIndexException e) {
					e.printStackTrace();
					logger.error(e.getMessage());
					newFile.renameTo(processed);
					continue;
				} catch (IOException e) {
					e.printStackTrace();
					logger.error(e.getMessage());
					newFile.renameTo(processed);
					continue;
				}
				logger.debug("File " + newFile + " added to the repository!");
				processed = new File(
						repository.getProcessedPath() + "/"
						+ newFile.getName());
				success = newFile.renameTo(processed);
				int count = 0;
				while(!success) {
					count++;
					try {
						logger.debug("Waiting for IO");
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					success = newFile.renameTo(processed);
					if(count >= 10)
						break;
				}
				if (!success) {
					logger.error("Couldn't move " + newFile + " to processed folder! Trying to delete "
							+ newFile);
					if (!newFile.delete()) {
						logger.error("Couldn't DELETE indexed file! " + newFile);
					}
				}
			}
			logger.info("Indexer found "
					+ filesToProcess.size()
					+ " documents that were successfully processed.");
			repository.removeRepositoryListener(listener);

		} else {
			logger.debug("No files to index, back to sleep.");
		}

		lock.delete();
	}
	
	private List<File> getFilesToProcess() {
		ArrayList<File> filesToProcess = new ArrayList<File>();
		File uploadedFilesFolder = new File(this.uploadFolder);
		if(!uploadedFilesFolder.exists()) {
			logger.error("Upload folder doesn't exist for TML");
			return filesToProcess;
		}
		for (File upload : uploadedFilesFolder.listFiles()) {
			if (upload.isDirectory())
				continue;
			if (upload.getName().endsWith(".txt")) {
				filesToProcess.add(upload);
				if(filesToProcess.size() >= this.maxFilesToProcess) {
					return filesToProcess;
				}
			}
		}
		return filesToProcess;
	}
}
