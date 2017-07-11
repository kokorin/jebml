package org.ebml.matroska;

import java.util.ArrayList;

import org.ebml.MasterElement;
import org.ebml.io.DataWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatroskaFileTags
{
  private static final int BLOCK_SIZE = 4096;
  private static final Logger LOG = LoggerFactory.getLogger(MatroskaFileTags.class);

  private final ArrayList<MatroskaFileTagEntry> tags = new ArrayList<>();

  private long myPosition;

  public void addTag(final MatroskaFileTagEntry tag)
  {
    tags.add(tag);
  }

  public long writeTags(final DataWriter ioDW)
  {
    myPosition = ioDW.getFilePointer();
    final MasterElement tagsElem = MatroskaDocTypes.Tags.getInstance();

    for (final MatroskaFileTagEntry tag : tags)
    {
      tagsElem.addChildElement(tag.toElement());
    }
    long len = tagsElem.writeElement(ioDW);
    if (ioDW.isSeekable())
    {
      new VoidElement(BLOCK_SIZE - tagsElem.getTotalSize()).writeElement(ioDW);
      return BLOCK_SIZE;
    }
    return len;
  }

  public void update(final DataWriter ioDW)
  {
    LOG.info("Updating tags list!");
    final long start = ioDW.getFilePointer();
    ioDW.seek(myPosition);
    writeTags(ioDW);
    ioDW.seek(start);
  }
}
