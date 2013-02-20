package fi.csc.microarray.client.visualisation.methods.gbrowser.track;

import java.awt.Color;
import java.util.LinkedList;

import fi.csc.microarray.client.visualisation.methods.gbrowser.dataSource.DataSource;
import fi.csc.microarray.client.visualisation.methods.gbrowser.dataSource.TabixDataSource;
import fi.csc.microarray.client.visualisation.methods.gbrowser.fileFormat.Strand;
import fi.csc.microarray.client.visualisation.methods.gbrowser.gui.GBrowserConstants;
import fi.csc.microarray.client.visualisation.methods.gbrowser.gui.GBrowserView;
import fi.csc.microarray.client.visualisation.methods.gbrowser.gui.LayoutTool;
import fi.csc.microarray.client.visualisation.methods.gbrowser.gui.LayoutTool.LayoutMode;

/**
 * Track group containing information about genes: transcript, intensity, gene, snp
 * repeat masker.
 * 
 * @author Vilius Zukauskas, Petri Klemelä
 *
 */
public class GeneTrackGroup extends TrackGroup {
	
	protected TranscriptTrack transcript;
	protected Track geneOverview;
	protected Track gene;
	protected ReferenceSNPTrack snpTrack = null;
	protected RepeatMaskerTrack repeatMasker;
	protected Track geneOverviewReversed;
	protected Track geneReversed;
	protected TranscriptTrack transcriptReversed;
	protected ReferenceSNPTrack snpTrackReversed;
	private boolean isUserData;

	public GeneTrackGroup(GBrowserView dataView, DataSource annotationDataSource, TabixDataSource repeatDataSource, boolean isUserData) {
		super(dataView);
		
		this.isUserData = isUserData;
		
		if (annotationDataSource != null) {
			transcript = new TranscriptTrack(dataView, annotationDataSource, GBrowserConstants.SWITCH_VIEWS_AT);
			transcript.setStrand(Strand.FORWARD);

//			geneOverview = new CoverageEstimateTrack(dataView, annotationDataSource, GBrowserConstants.COLOR_BLUE_BRIGHTER, 
//					GBrowserConstants.CHANGE_TRACKS_ZOOM_THRESHOLD2, true, false);
//			geneOverview.setStrand(Strand.FORWARD);
			geneOverview = new EmptyTrack(dataView, transcript.getMinHeight(), GBrowserConstants.CHANGE_TRACKS_ZOOM_THRESHOLD2);
			

			gene = new GeneTrack(dataView, annotationDataSource, GBrowserConstants.COLOR_BLUE_BRIGHTER, 
					GBrowserConstants.SWITCH_VIEWS_AT, GBrowserConstants.CHANGE_TRACKS_ZOOM_THRESHOLD2);
			gene.setStrand(Strand.FORWARD);
		}
		
		if (repeatDataSource != null) {
			repeatMasker = new RepeatMaskerTrack(dataView, repeatDataSource, 0, GBrowserConstants.SWITCH_VIEWS_AT);
		}
		
		if (annotationDataSource != null) {
//			geneOverviewReversed = new CoverageEstimateTrack(dataView, annotationDataSource, GBrowserConstants.COLOR_BLUE_BRIGHTER, 
//					GBrowserConstants.CHANGE_TRACKS_ZOOM_THRESHOLD2, true, false);
//			geneOverviewReversed.setStrand(Strand.REVERSE);
			geneOverviewReversed = new EmptyTrack(dataView, transcript.getMinHeight(), GBrowserConstants.CHANGE_TRACKS_ZOOM_THRESHOLD2);

			geneReversed = new GeneTrack(dataView, annotationDataSource, GBrowserConstants.COLOR_BLUE_BRIGHTER, 
					GBrowserConstants.SWITCH_VIEWS_AT, GBrowserConstants.CHANGE_TRACKS_ZOOM_THRESHOLD2);
			geneReversed.setStrand(Strand.REVERSE);

			transcriptReversed = new TranscriptTrack(dataView, annotationDataSource, GBrowserConstants.SWITCH_VIEWS_AT);
			transcriptReversed.setStrand(Strand.REVERSE);
		}
		
		adds(isUserData);
	}

	public void adds(boolean isUserData) {
		
		this.tracks = new LinkedList<Track>();
		
		if (!isUserData) {
			// title
			tracks.add(new TitleTrack(view, "Annotations", Color.black));
		}
		
        if (transcript != null) { // no annotation data source 
        	// Transcript, detailed, forward
        	tracks.add(transcript);

        	// Gene, overview, forward 
        	tracks.add(geneOverview);

        	// Gene, detailed, forward
        	tracks.add(gene);
        }
		
		if (snpTrack != null) {
			// SNP track Forward
			tracks.add(snpTrack);
		}

		if (isUserData) {
			tracks.add(new SeparatorTrack(view, Color.gray, 1, 0, Long.MAX_VALUE));
		} else {
			// Ruler track
			tracks.add(new RulerTrack(view));			
		}

		if (snpTrackReversed != null) {
			// SNP track Reversed
			tracks.add(snpTrackReversed);
		}

		if (repeatMasker != null) {
			// Repeat masker track
			tracks.add(repeatMasker);
		}
		
		if (transcript != null) { //no annotation data source
			// Gene, overview, reverse
			tracks.add(geneOverviewReversed);

			// Gene, detailed, reverse
			tracks.add(geneReversed);

			// Transcript, detailed, reverse
			tracks.add(transcriptReversed);
		}
		
		// Add gene group to data view
//	    addGroup(view, tracks);
	}
	
	@Override
	public String getName() {
		return "GeneTrackGroup";
	}
	
	private void setChangeSNP(boolean change) {
		if (change) {
			snpTrack.changeSNPView();
			snpTrackReversed.changeSNPView();
		} else {
			snpTrack.returnSNPView();
			snpTrackReversed.returnSNPView();
		}
		view.fireAreaRequests();
        view.redraw();
	}
	
	@Override
	public void showOrHide(String name, boolean state) {
		super.showOrHide(name, state);
		if (snpTrack != null && name.equals("changeSNP")) {
			setChangeSNP(state);
		}
	}
	
//	@Override
//	public LayoutMode getLayoutMode() {
//		return LayoutMode.FILL;
//	}
	
	@Override
	public int getMinHeight() {
		if (isUserData) {
			return super.getMinHeight();
		} else {
			return 250;
		}
	}	
}
