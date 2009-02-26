package org.gbif.provider.util;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.media.jai.Interpolation;
import javax.media.jai.InterpolationBicubic2;
import javax.media.jai.InterpolationNearest;
import javax.media.jai.JAI;
import javax.media.jai.OpImage;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.media.jai.codec.MemoryCacheSeekableStream;
import com.sun.media.jai.codec.SeekableStream;

public class ResizeImage {
	protected static final Log log = LogFactory.getLog(ResizeImage.class);

	public static void resizeImage(File source, File resized, int newWidth, int newHeight) throws IOException{
        // read in the original image from a file
        InputStream inputStream = new FileInputStream(source);
        PlanarImage originalImage = JAI.create("stream", new MemoryCacheSeekableStream(inputStream));

        final int originalWidth=originalImage.getWidth();
        final int originalHeight=originalImage.getHeight();
        double hRatio = ((double)newHeight/(double)originalHeight);
        double wRatio = ((double)newWidth/(double)originalWidth);
        double scale = Math.min(hRatio, wRatio);
        
        final ParameterBlock parameterBlock=new ParameterBlock();
        parameterBlock.addSource(originalImage);
        parameterBlock.add(scale);
        parameterBlock.add(scale);
        parameterBlock.add(Interpolation.getInstance(Interpolation.INTERP_BICUBIC_2));	
        
        final RenderingHints renderingHints=new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        final PlanarImage newImage=JAI.create("SubsampleAverage", parameterBlock, renderingHints);
        
        // output stream, in a specific encoding
        OutputStream outputStream = new FileOutputStream(resized);
        JAI.create("encode", newImage, outputStream, "PNG", null);
        
        log.debug("Resized logo image with factor "+scale);
        inputStream.close();
        outputStream.close();     

	}
	
}
