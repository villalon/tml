/**
 * 
 */
package tml.test;


import java.io.File;
import java.io.IOException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;
import org.junit.BeforeClass;
import org.junit.Test;

import tml.Configuration;

import static org.junit.Assert.*;

/**
 * @author Jorge Villalon
 *
 */
public class DbConnectionTest extends AbstractTmlIndexingTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		AbstractTmlIndexingTest.setUpBeforeClass();
	}

	@Test
	public void checkConnection() {
		assertNotNull(repository.getDbConnection());
	}
	
	@Test
	public void addMetaData() throws LockObtainFailedException, CorruptIndexException, IOException {
		File[] files = new File[1];
		files[0] = new File(Configuration.getTmlFolder() + "/corpora/uppsala/0100.a1.txt");
		repository.addDocumentsInList(files);
	}
	
	@Test
	public void getNullMetaData() {
		String metadata = repository.getAnnotations("0100.a1", "penntree");
		assertNull(metadata);
		metadata = repository.getAnnotations("p1d0100.a1", "penntree");
		assertNull(metadata);
		metadata = repository.getAnnotations("s1d0100.a1", "penntree");
		assertNull(metadata);
	}
}
