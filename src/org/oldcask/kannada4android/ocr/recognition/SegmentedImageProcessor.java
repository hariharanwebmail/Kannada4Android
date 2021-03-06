package org.oldcask.kannada4android.ocr.recognition;

import java.util.ArrayList;
import java.util.List;

import org.oldcask.kannada4android.ocr.imagelibrary.Hilditch;
import org.oldcask.kannada4android.ocr.imagelibrary.Parameters;
import org.oldcask.kannada4android.ocr.imagelibrary.Threshold;
import org.oldcask.kannada4android.ocr.preprocessing.Localisation;



import jjil.core.RgbImage;

public class SegmentedImageProcessor {
	List<RgbImage> segmentsList;
	List<RgbImage> thinnedSegmentsList;
	int validSegments;
	Hilditch segmentThinner;
	DownSample downSampler[];

	public SegmentedImageProcessor() {
		segmentsList = new ArrayList<RgbImage>(Parameters.MAX_CHARACTERS_RECOGNISABLE);
		thinnedSegmentsList = new ArrayList<RgbImage>(Parameters.MAX_CHARACTERS_RECOGNISABLE);
		validSegments = 0;
		segmentThinner = new Hilditch();
		downSampler = new DownSample[Parameters.MAX_CHARACTERS_RECOGNISABLE];
	}

	/**
	 * All segments are processed(Thinned,Downsampled) one by one and inserted into the Queues
	 * 
	 * @param inputSegment
	 *            A Segment of the Image
	 * 
	 * @param inputBoolean
	 *            The boolean representation of the thresholded input image
	 */
	public void process(RgbImage inputSegment, boolean inputBoolean[][]) {
		segmentsList.add(inputSegment);

		boolean tempBoolean[][] = new boolean[inputBoolean.length + 4][inputBoolean[0].length + 4];
		padFalse(inputBoolean, tempBoolean, inputBoolean.length, inputBoolean[0].length);

		RgbImage thinnedImage = segmentThinner.thinningHilditch(tempBoolean, tempBoolean[0].length,
				tempBoolean.length, Parameters.LAYERS_TO_THIN);
		RgbImage localisedImage;
		
		if (thinnedImage != null) {
			tempBoolean = new boolean[thinnedImage.getHeight()][thinnedImage.getWidth()];
			tempBoolean = Threshold.thresholdIterative(thinnedImage);
			
			Localisation localisation = new Localisation(thinnedImage, tempBoolean);
			localisedImage = localisation.localiseImageByWidth();
			localisedImage = localisation.localiseImageByHeight();
			tempBoolean = new boolean[localisedImage.getHeight()][localisedImage.getWidth()];
			tempBoolean = Threshold.thresholdIterative(localisedImage);
			Localisation.Print(tempBoolean);
			
			thinnedSegmentsList.add(localisedImage);
		} else {
			segmentsList.remove(validSegments);
			return;
		}
		downSampler[validSegments] = new DownSample();
		downSampler[validSegments].downSample(localisedImage, tempBoolean);
		validSegments++;
	}
	/**
	 * 
	 * padFalse method pads the source boolean array with 2 rows of 'false' values
	 * 
	 * @param sourceBoolean
	 *            The source array
	 * @param resultBoolean
	 *            The bigger resulting array
	 * @param height
	 *            Height of the array (t1.length)
	 * @param width
	 *            Width of the array (t1[0].length)
	 * 
	 * @return The padded array
	 */
	public boolean[][] padFalse(boolean sourceBoolean[][], boolean resultBoolean[][], int height, int width) {

		for (int i = 0; i < width + 4; i++) {
			resultBoolean[0][i] = false;
			resultBoolean[height + 1][i] = false;
		}

		for (int j = 0; j < height + 4; j++) {
			resultBoolean[j][0] = false;
			resultBoolean[j][width + 1] = false;
		}

		for (int k = 0, ki = 2; k < height; k++, ki++)
			for (int l = 0, li = 2; l < width; l++, li++)
				resultBoolean[ki][li] = sourceBoolean[k][l];
		return resultBoolean;
	}
	public RgbImage getPic(int index) {
		return segmentsList.get(index);
	}

	public RgbImage getThinPic(int index) {
		return thinnedSegmentsList.get(index);
	}

	public boolean[][] getDownsample(int index) {
		return downSampler[index].getDownSampled();
	}

	public int getNumberOfValidSegments() {
		return validSegments;
	}
}
