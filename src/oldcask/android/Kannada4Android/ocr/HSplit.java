package oldcask.android.Kannada4Android.ocr;

import java.io.IOException;

import jjil.algorithm.RgbCrop;
import jjil.core.Error;
import jjil.core.RgbImage;

public class HSplit {
	private static final int MYTHRESHOLDPARAM = 120;

	int Strength[];

	int h, w;

	static int pcount = 1;

	/**
	 * 
	 * @param input
	 *            The input BufferedImage on which the operations will be done
	 * @param t
	 *            Boolean representation of the thresholded version of the input
	 *            image
	 */
	public HSplit(RgbImage input, boolean t[][]) {
		h = input.getHeight();
		w = input.getWidth();
		Strength = new int[h];
		for (int i = 0; i < h; i++) {
			Strength[i] = HistAnl.getStrengthH(t, i, 0, w);
		}
	}

	/**
	 * Segments the Number Plate on the constituting character
	 * 
	 * @param H
	 *            The Halves object which contains the halves of the Number
	 *            Plate
	 * @param PicQueue
	 *            The Queue into which the segemts are inserted
	 */
	public void segment(Halves H, BIQueue PicQueue) {
		int Strength[] = new int[w], Lines[] = new int[w];
		int from[] = new int[20];
		int to[] = new int[20];
		int width[] = new int[20];

		for (int i = 0; i < H.ActHalf; i++) {
			int h = H.Half[i].getHeight();
			int w = H.Half[i].getWidth();
			int count = 0, itr = 0;
			Lines[++count] = 0;

			boolean t[][] = Threshold.threshold(H.Half[i], 0.75f, 0.15f,MYTHRESHOLDPARAM);
			try {
				RgbImageAndroid.toFile(null,  Threshold.makeImage(t),100, "data/thresholdedinit.jpg");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			for (int j = 0; j < w; j++) {
				Strength[j] = HistAnl.getStrengthV(t, 0, j, h);
				if (Strength[j] < 0.08 * t.length)
					Lines[count++] = j;

			}
			Lines[count] = w;
			
			System.out.println("***********Compare Testing\n\n ****************");
			System.out.println("Width = " +w);
			
			for (int j = 0; j < count; j++) {
				if (Lines[j + 1] - Lines[j] != 1) {
					from[itr] = Lines[j];
					to[itr] = Lines[j + 1];
					width[itr] = to[itr] - from[itr];
					System.out.println("Printing Hsplit " + from[itr] + " " + to[itr] + " "
							+ width[itr] + " " + itr);
			
					try{
					RgbImage subImage = H.Half[i];
					RgbCrop subCrop = new RgbCrop(from[itr], 0, width[itr], h);
					subCrop.push(H.Half[i]);
					if(!subCrop.isEmpty())
						subImage = (RgbImage) subCrop.getFront();
					
					if (width[itr] < 4
							|| Threshold.threshold(subImage) > 0.75f)
						continue;
					boolean sub[][] = getSubArray(t, from[itr], to[itr], 0, h);
					System.out.println("wtf is happening here 3.. ");
					PicQueue.insert(subImage, sub);
					System.out.println("wtf is happening here 4.. ");
					itr++;
					} catch (Error e) {
						System.out.println(" Error in height in Rgbcrop in hSplit");
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * Two Dimensional version of System.arraycopy() Copies a particular part of
	 * the two dimensional boolean array into another
	 * 
	 * @param t
	 *            The source array, of the form t[y][x]
	 * @param x1
	 *            The starting x position.
	 * @param x2
	 *            The ending y position
	 * @param y1
	 *            The starting y position
	 * @param y2
	 *            The ending y position
	 * @return The copied array
	 */
	public boolean[][] getSubArray(boolean t[][], int x1, int x2, int y1, int y2) {
		boolean temp[][] = new boolean[y2 - y1][x2 - x1];
		for (int i = y1, a = 0; i < y2; i++, a++) {
			for (int j = x1, b = 0; j < x2; j++, b++) {
				temp[a][b] = t[i][j];
			}
		}
		return temp;
	}

	/**
	 * Finds a split point on the candaidate image. The Number plate is printed
	 * in two lines, it returns a value near half of the height of the image
	 * 
	 * @return the SplitPoint.
	 */
	public int shouldSplit() {
		if (Strength[h / 2] < 5)
			return h / 2;

		boolean Split = false;
		int j = h / 2;
		while (!Split && j > h / 3) {
			j--;
			if (Strength[j] < 5)
				Split = true;
		}
		if (Split == true)
			return j;

		Split = false;
		j = h / 2;
		while (!Split && j <= 2 * h / 3) {
			j++;
			if (Strength[j] < 5)
				Split = true;
		}
		if (Split == true)
			return j;

		return (h - 1);
	}

}
