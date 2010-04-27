package fi.csc.microarray.client.visualisation.methods.gbrowser.dataFetcher;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import fi.csc.microarray.client.visualisation.methods.gbrowser.DataSource;
import fi.csc.microarray.client.visualisation.methods.gbrowser.fileFormat.FileParser;
import fi.csc.microarray.client.visualisation.methods.gbrowser.message.AreaRequest;
import fi.csc.microarray.client.visualisation.methods.gbrowser.message.FileRequest;
import fi.csc.microarray.client.visualisation.methods.gbrowser.message.FileResult;
import fi.csc.microarray.client.visualisation.methods.gbrowser.message.RowRegion;

public class TreeThread extends AreaRequestHandler {

	private TreeNode rootNode;

	private BlockingQueue<FileRequest> fileRequestQueue = new LinkedBlockingQueue<FileRequest>();
	private ConcurrentLinkedQueue<FileResult> fileResultQueue = new ConcurrentLinkedQueue<FileResult>();

	private FileFetcherThread fileFetcher;

	private static final boolean DEBUG = false;

	private FileParser inputParser;

	private DataSource file;

	public TreeThread(DataSource file, Queue<AreaRequest> areaRequestQueue, AreaResultListener areaResultListener, FileParser inputParser) {

		super(areaRequestQueue, areaResultListener);
		this.inputParser = inputParser;
		this.file = file;
	}

	public synchronized void run() {

		fileFetcher = new FileFetcherThread(fileRequestQueue, fileResultQueue, this, inputParser);
		createTree(fileFetcher.getRowCount());
		fileFetcher.start();

		super.run();
	}

	protected boolean checkOtherQueues() {
		FileResult fileResult;
		if ((fileResult = fileResultQueue.poll()) != null) {
			fileResult.status.fileResultCount = fileResultQueue.size();

			processFileResult(fileResult);
		}
		return fileResult != null;
	}

	private void createTree(long rowCount) {
		rootNode = new TreeNode(new RowRegion(0l, rowCount), this, null);
	}

	private void processFileResult(FileResult fileResult) {
		fileResult.request.node.processFileResult(fileResult);
	}

	protected void processAreaRequest(AreaRequest areaRequest) {
		if (DEBUG) {
			System.out.println("Tree: Got area request " + areaRequest);
		}
		rootNode.processAreaRequest(areaRequest);
	}

	public void createFileRequest(AreaRequest areaRequest, RowRegion rowRegion, TreeNode node) {
		areaRequest.status.maybeClearQueue(fileRequestQueue);

		fileRequestQueue.add(new FileRequest(areaRequest, rowRegion, node, areaRequest.status));
	}

	public FileParser getInputParser() {
		return inputParser;
	}

	public DataSource getFile() {
		return file;
	}
}
