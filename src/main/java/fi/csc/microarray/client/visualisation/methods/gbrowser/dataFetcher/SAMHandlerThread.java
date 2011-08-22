package fi.csc.microarray.client.visualisation.methods.gbrowser.dataFetcher;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import fi.csc.microarray.client.visualisation.methods.gbrowser.DataSource;
import fi.csc.microarray.client.visualisation.methods.gbrowser.SAMDataSource;
import fi.csc.microarray.client.visualisation.methods.gbrowser.message.AreaRequest;
import fi.csc.microarray.client.visualisation.methods.gbrowser.message.AreaResult;

public class SAMHandlerThread extends AreaRequestHandler {
    
    SAMDataSource samData;
	private SAMFileFetcherThread fileFetcher;
	private BlockingQueue<SAMFileRequest> fileRequestQueue = new LinkedBlockingQueue<SAMFileRequest>();
	private ConcurrentLinkedQueue<SAMFileResult> fileResultQueue = new ConcurrentLinkedQueue<SAMFileResult>();

    public SAMHandlerThread(DataSource file, Queue<AreaRequest> areaRequestQueue,
            AreaResultListener areaResultListener) {
        
        super(areaRequestQueue, areaResultListener);
        samData = (SAMDataSource) file;
    }

	@Override
	public synchronized void run() {

		// Start file processing layer thread
		fileFetcher = new SAMFileFetcherThread(fileRequestQueue, fileResultQueue, samData);
		fileFetcher.start();
		
		// Start this thread
		super.run();
	}

	protected boolean checkOtherQueues() {
		SAMFileResult fileResult = null;
		if ((fileResult = fileResultQueue.poll()) != null) {
			processFileResult(fileResult);
		}
		return fileResult != null;
	}

    private void processFileResult(SAMFileResult fileResult) {

    	createAreaResult(new AreaResult(fileResult.getStatus(), fileResult.getContent()));
	}

	/**
     * Handles normal and concised area requests by using SAMFile.
     */
    @Override
    protected void processAreaRequest(AreaRequest areaRequest) {
		fileRequestQueue.add(new SAMFileRequest(areaRequest, areaRequest.status));
    }

}
