package org.ebml.matroska;

import org.ebml.Element;
import org.ebml.MasterElement;
import org.ebml.UnsignedIntegerElement;
import org.ebml.io.DataWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatroskaFileCues
{
  private static final Logger LOG = LoggerFactory.getLogger(MatroskaFileCues.class);
  private MasterElement cues = MatroskaDocTypes.Cues.getInstance();
  private long endOfEbmlHeaderBytePosition;

  public MatroskaFileCues(long endOfEbmlHeaderBytePosition)
  {
    this.endOfEbmlHeaderBytePosition = endOfEbmlHeaderBytePosition;
  }

  public void addCue(long positionInFile, long timecodeOfCluster, int trackNumber)
  {
    LOG.debug("Adding matroska cue to cues element at position [{}], using timecode [{}], for track number [{}]",
              positionInFile,
              timecodeOfCluster,
              trackNumber);

    UnsignedIntegerElement cueTime = MatroskaDocTypes.CueTime.getInstance();
    cueTime.setValue(timecodeOfCluster);
    MasterElement cuePoint = MatroskaDocTypes.CuePoint.getInstance();
    cuePoint.addChildElement(cueTime);
    MasterElement cueTrackPositions = createCueTrackPositions(positionInFile, trackNumber);
    cuePoint.addChildElement(cueTrackPositions);
    cues.addChildElement(cuePoint);

    LOG.debug("Finished adding matroska cue to cues element");
  }

  private MasterElement createCueTrackPositions(long positionInFile, int trackNumber)
  {
    MasterElement cueTrackPositions = MatroskaDocTypes.CueTrackPositions.getInstance();

    UnsignedIntegerElement cueTrack = MatroskaDocTypes.CueTrack.getInstance();
    cueTrack.setValue(trackNumber);
    cueTrackPositions.addChildElement(cueTrack);

    UnsignedIntegerElement cueClusterPosition = MatroskaDocTypes.CueClusterPosition.getInstance();
    cueClusterPosition.setValue(getPositionRelativeToSegmentEbmlElement(positionInFile));
    cueTrackPositions.addChildElement(cueClusterPosition);

    return cueTrackPositions;
  }

  public Element write(DataWriter ioDW, MatroskaFileMetaSeek metaSeek)
  {
    long currentBytePositionInFile = ioDW.getFilePointer();
    LOG.debug("Writing matroska cues at file byte position [{}]", currentBytePositionInFile);
    long numberOfBytesInCueData = cues.writeElement(ioDW);
    LOG.debug("Done writing matroska cues, number of bytes was [{}]", numberOfBytesInCueData);

    metaSeek.addIndexedElement(cues, currentBytePositionInFile);
    return cues;
  }

  private long getPositionRelativeToSegmentEbmlElement(long currentBytePositionInFile)
  {
    return currentBytePositionInFile - endOfEbmlHeaderBytePosition;
  }
}
