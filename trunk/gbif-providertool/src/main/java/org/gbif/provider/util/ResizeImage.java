/*
 * Copyright 2009 GBIF.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gbif.provider.util;

import com.sun.media.jai.codec.FileSeekableStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.awt.RenderingHints;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;

/**
 * TODO: Documentation.
 * 
 */
public class ResizeImage {
  protected static final Log log = LogFactory.getLog(ResizeImage.class);

  public static void resizeImage(File source, File resized, int newWidth,
      int newHeight) throws IOException {
    // read in the original image from a file
    PlanarImage originalImage = JAI.create("stream", new FileSeekableStream(
        source));

    final int originalWidth = originalImage.getWidth();
    final int originalHeight = originalImage.getHeight();
    double hRatio = ((double) newHeight / (double) originalHeight);
    double wRatio = ((double) newWidth / (double) originalWidth);
    double scale = Math.min(hRatio, wRatio);

    final ParameterBlock parameterBlock = new ParameterBlock();
    parameterBlock.addSource(originalImage);
    parameterBlock.add(scale);
    parameterBlock.add(scale);
    parameterBlock.add(Interpolation.getInstance(Interpolation.INTERP_BICUBIC_2));

    final RenderingHints renderingHints = new RenderingHints(
        RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    final PlanarImage newImage = JAI.create("SubsampleAverage", parameterBlock,
        renderingHints);

    // output stream, in a specific encoding
    OutputStream outputStream = new FileOutputStream(resized);
    JAI.create("encode", newImage, outputStream, "PNG", null);

    log.debug("Resized logo image with factor " + scale);
    outputStream.close();
  }

}
