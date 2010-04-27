package fi.csc.microarray.client.visualisation.methods.gbrowser.message;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import fi.csc.microarray.client.visualisation.methods.gbrowser.DataSource;

public class FsfStatus {

	/**
	 * All threads should send this forward and end themselves
	 */
	public boolean poison;

	public long areaRequestCount;
	public long fileRequestCount;
	public long fileResultCount;
	public boolean clearQueues;
	public boolean concise;
	public boolean debug;

	private Set<Object> clearedAlready = new HashSet<Object>();
	public DataSource file;

	public void maybeClearQueue(Object fileResultQueue) {
		if (clearQueues && !clearedAlready.contains(fileResultQueue)) {
			clearedAlready.add(fileResultQueue);
			((Queue<?>) fileResultQueue).clear();
		}
	}
}
