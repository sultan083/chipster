package fi.csc.microarray.client.visualisation.methods.gbrowser;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;

import fi.csc.microarray.client.visualisation.methods.gbrowser.dataFetcher.TreeThread;
import fi.csc.microarray.client.visualisation.methods.gbrowser.fileFormat.BEDParser;
import fi.csc.microarray.client.visualisation.methods.gbrowser.fileFormat.CytobandParser;
import fi.csc.microarray.client.visualisation.methods.gbrowser.fileFormat.ElandParser;
import fi.csc.microarray.client.visualisation.methods.gbrowser.fileFormat.GeneParser;
import fi.csc.microarray.client.visualisation.methods.gbrowser.fileFormat.SequenceParser;
import fi.csc.microarray.client.visualisation.methods.gbrowser.fileFormat.Strand;
import fi.csc.microarray.client.visualisation.methods.gbrowser.fileFormat.TranscriptParser;
import fi.csc.microarray.client.visualisation.methods.gbrowser.fileFormat.miRNAParser;
import fi.csc.microarray.client.visualisation.methods.gbrowser.track.CytobandTrack;
import fi.csc.microarray.client.visualisation.methods.gbrowser.track.GeneTrack;
import fi.csc.microarray.client.visualisation.methods.gbrowser.track.IntensityTrack;
import fi.csc.microarray.client.visualisation.methods.gbrowser.track.PeakTrack;
import fi.csc.microarray.client.visualisation.methods.gbrowser.track.ProfileTrack;
import fi.csc.microarray.client.visualisation.methods.gbrowser.track.RulerTrack;
import fi.csc.microarray.client.visualisation.methods.gbrowser.track.SeparatorTrack;
import fi.csc.microarray.client.visualisation.methods.gbrowser.track.SeqBlockTrack;
import fi.csc.microarray.client.visualisation.methods.gbrowser.track.SeqTrack;
import fi.csc.microarray.client.visualisation.methods.gbrowser.track.Track;
import fi.csc.microarray.client.visualisation.methods.gbrowser.track.TranscriptTrack;
import fi.csc.microarray.client.visualisation.methods.gbrowser.track.TranscriptTrack.PartColor;

public class TrackFactory {
	
	public static void addGeneTracks(GenomePlot genomePlot, DataSource annotationFile) {

		// initialise data source file
		GeneParser geneParser = new GeneParser();
		
		// add tracks for both strands
		for (Strand strand : Strand.values()) {

			// overview
			IntensityTrack annotationOverviewReversed = new IntensityTrack(genomePlot.getDataView(), annotationFile, TreeThread.class, geneParser, PartColor.CDS.c, 10000000);
			annotationOverviewReversed.setStrand(strand);
			addTrack(genomePlot.getDataView(), annotationOverviewReversed);

			// detailed
			GeneTrack annotationReversed = new GeneTrack(genomePlot.getDataView(), annotationFile, TreeThread.class, geneParser, PartColor.CDS.c, 0, 10000000);
			annotationReversed.setStrand(strand);
			addTrack(genomePlot.getDataView(), annotationReversed);
		}
	}

	
	public static void addReadTracks(GenomePlot plot, DataSource userData, DataSource seqFile) throws FileNotFoundException, MalformedURLException {
		ElandParser userDataParser = new ElandParser();
		View dataView = plot.getDataView();
		int switchViewsAt = 50000;
		
		// FORWARD
		// Overview
		IntensityTrack readOverview = new IntensityTrack(dataView, userData, TreeThread.class, userDataParser, Color.gray, switchViewsAt);
		addTrack(dataView, readOverview);

		// Detailed
		SeqBlockTrack reads = new SeqBlockTrack(dataView, userData, TreeThread.class, userDataParser, Color.RED, 0, switchViewsAt);
		addTrack(dataView, reads);

		dataView.addTrack(new SeparatorTrack(dataView));

		if (seqFile != null) {
			// Reference sequence
			SeqTrack seq = new SeqTrack(dataView, seqFile, TreeThread.class, new SequenceParser(), 800);
			addTrack(dataView, seq);
		}

		// R E V E R S E D ///////////////////////////////////////////////////
		// Overview
		IntensityTrack readOverviewReversed = new IntensityTrack(dataView, userData, TreeThread.class, userDataParser, Color.gray, switchViewsAt);

		readOverviewReversed.setStrand(Strand.REVERSED);
		addTrack(dataView, readOverviewReversed);

		// Detailed
		dataView.addTrack(new SeparatorTrack(dataView));

		SeqBlockTrack readsReversed = new SeqBlockTrack(dataView, userData, TreeThread.class, userDataParser, Color.RED, 0, switchViewsAt);
		readsReversed.setStrand(Strand.REVERSED);
		addTrack(dataView, readOverviewReversed);
	}

	public static void addWigTrack(GenomePlot plot, DataSource peakFile) {
		miRNAParser miRNAParser = new miRNAParser();
		ProfileTrack annotation = new ProfileTrack(plot.getDataView(), peakFile, TreeThread.class, miRNAParser, Color.BLUE, 0, Long.MAX_VALUE);
		addTrack(plot.getDataView(), annotation);
	}
	
	public static void addPeakTracks(GenomePlot plot, DataSource peakFile) {
		BEDParser bedParser = new BEDParser();
		View dataView = plot.getDataView();

		PeakTrack annotation = new PeakTrack(dataView, peakFile, TreeThread.class, bedParser, Color.YELLOW, 0, Long.MAX_VALUE);
		addTrack(dataView, annotation);
	}

	public static void addTranscriptTracks(GenomePlot plot, DataSource annotationFile) {
		TranscriptParser geneParser = new TranscriptParser();
		View dataView = plot.getDataView();

		for (Strand strand : Strand.values()) {

			// Overview
			IntensityTrack annotationOverview = new IntensityTrack(dataView, annotationFile, TreeThread.class, geneParser, PartColor.CDS.c.darker(), 100000);
			annotationOverview.setStrand(strand);
			addTrack(dataView, annotationOverview);

			// Detailed
			TranscriptTrack annotation = new TranscriptTrack(dataView, annotationFile, TreeThread.class, geneParser, Color.DARK_GRAY, 100000);
			annotation.setStrand(strand);
			addTrack(dataView, annotation);

			if (strand == Strand.FORWARD) {
				dataView.addTrack(new SeparatorTrack(dataView));
			}
		}
	}

	public static void addMirnaTracks(GenomePlot plot, DataSource miRNAFile) {
		miRNAParser miRNAParser = new miRNAParser();
		View dataView = plot.getDataView();

		for (Strand strand : Strand.values()) {

			GeneTrack track = new GeneTrack(dataView, miRNAFile, TreeThread.class, miRNAParser, PartColor.CDS.c.darker(), 0, Long.MAX_VALUE);
			track.setStrand(strand);
			dataView.addTrack(track);
			track.initializeListener();

			if (strand == Strand.FORWARD) {
				dataView.addTrack(new SeparatorTrack(dataView));
			}
		}
	}

	public static void addCytobandTracks(GenomePlot plot, DataSource cytobandFile) {
		CytobandTrack overviewCytobands = new CytobandTrack(plot.getOverviewView(), cytobandFile, TreeThread.class, new CytobandParser(), false);
		addTrack(plot.getOverviewView(), overviewCytobands);

		CytobandTrack cytobands = new CytobandTrack(plot.getDataView(), cytobandFile, TreeThread.class, new CytobandParser(), true);
		addTrack(plot.getDataView(), cytobands);
	}

	public static void addRulerTrack(GenomePlot plot) {
		plot.getDataView().addTrack(new RulerTrack(plot.getDataView()));
	}
	
	private static void addTrack(View view, Track track) {
		view.addTrack(track);
		track.initializeListener();
	}
	

}
