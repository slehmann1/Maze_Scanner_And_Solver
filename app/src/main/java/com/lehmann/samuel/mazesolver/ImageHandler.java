package com.lehmann.samuel.mazesolver;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by samuel on 2017-11-28.
 */

public class ImageHandler {

    //The highest value that is not cutoff (up to 255)
    final double CUTOFFMAGNITUDE = 110;
    final int REQUIRED_SIZE = 300;


    //The width of the rectangle used to smooth
    final int RECTSMOOTHWIDTH = 2;

    //The epsilon value given to the douglas peucker algorithm
    final double EPSILON = 0.01;

    ImageView left, right;


    public ImageHandler(int inputBitmapLocation, Activity activity) {

        left = activity.findViewById(R.id.left);
        right = activity.findViewById(R.id.right);

        Bitmap inputBitmap = decodeFile(inputBitmapLocation, activity);
        //Convert the bitmap to black and white to prevent necessity of euclidean distance comparison (slower)
        Bitmap greyedBitmap = grayScale(inputBitmap);
        Bitmap outputBitmap = cutoffed(greyedBitmap);
        Bitmap smoothedBitmap = rectangularSmooth(outputBitmap);
        List<Coordinate> contigCoords = determineMidOfContiguous(smoothedBitmap);


        Log.v("sam", "HEIGHT" + Integer.toString(inputBitmap.getHeight()));


    }

    /**
     * Uses the Douglas-Peucker algorithm to remove points from a curve, simplifying it
     * Assumes that the start point of the line is the first element in the list
     * This is a standard implementation
     *
     * @param input
     * @return
     */
    private List<Coordinate> simplify(List<Coordinate> input) {
        double furthestDistance = 0;
        int furthestIndex = 0;

        for (int i = 1; i < input.size(); i++) {
            if (input.get(i).distanceTo(input.get(0)) > furthestDistance) {
                furthestDistance = input.get(i).distanceTo(input.get(0));
                furthestIndex = i;
            }
        }

        if (furthestDistance > EPSILON) {
            List<Coordinate> reduced1 = simplify(input.subList(0, furthestIndex + 1));
            List<Coordinate> reduced2 = simplify(input.subList(0, input.size()));
            List<Coordinate> output = new ArrayList<>(reduced1);

            output.addAll(reduced2.subList(1, reduced2.size()));
            return output;
        } else {
            return input;
        }
    }

    /**
     * Scans a bitmap for black pixels that are connected. Groups of connected pixels are returned
     *
     * @param bitmap
     * @return
     */
    private List<List<WallPixel>> determineContiguousGroups(Bitmap bitmap) {
        List<WallPixel> pixels = new ArrayList<>();
        int possibleGroupIDs = 0;
        int maxGroupID = -1;

        for (int x = 0; x < bitmap.getWidth(); x++) {
            for (int y = 0; y < bitmap.getHeight(); y++) {
                if (bitmap.getPixel(x, y) == Color.BLACK) {
                    int groupID = determineInitGroupID(x, y, pixels, maxGroupID);

                    if (groupID > maxGroupID) {
                        maxGroupID = groupID;
                        possibleGroupIDs++;
                    }

                    pixels.add(new WallPixel(x, y, groupID));
                }
            }
        }

        List<WallPixel> checkedPixels = pixels;
        List<Integer> neighbouringGroupIDs = new ArrayList<>();

        //Do a second pass
        for (WallPixel pixel : pixels) {
            //Check neighbours
            for (int x = -1; x < 1; x++) {
                for (int y = -1; y < 1; y++) {

                    if (!(x == y && y == 0)) {

                        int id = getPixelGroupID(pixel.x + x, pixel.y + y, checkedPixels);
                        if (!neighbouringGroupIDs.contains(id)) {
                            // One of the neighbours has a group ID not in the list
                            neighbouringGroupIDs.add(id);
                        }

                    }

                }
            }
            //All neighbours have been checked
            if (neighbouringGroupIDs.size() > 1) {
                int lowestID = neighbouringGroupIDs.get(0);

                for (int id : neighbouringGroupIDs) {
                    if (lowestID > id) {
                        lowestID = id;
                    } else {
                        possibleGroupIDs--;
                    }
                }

                //Update the ids in both the checked and the unchecked lists to be the same as the new low id

                updateGroupIDs(pixels, neighbouringGroupIDs, lowestID);
                updateGroupIDs(checkedPixels, neighbouringGroupIDs, lowestID);
            }

            //pixel has now been checked
            checkedPixels.remove(pixel);
        }

        List<List<WallPixel>> output = new ArrayList<>(possibleGroupIDs);

        for (int i = 0; i < possibleGroupIDs; i++) {
            List<WallPixel> groupedList = new ArrayList<>();
            for (WallPixel pixel : pixels) {
                if (pixel.groupId == i) {
                    groupedList.add(pixel);
                }
            }
            output.add(groupedList)
        }

        return  output;
    }

    /**
     * Updates all of the elements within the list that have a group id in the integer list groupIDs
     * to have the new groupID lowestID
     *
     * @param pixels
     * @param groupIDs
     * @param lowestID
     * @return
     */
    private List<WallPixel> updateGroupIDs(List<WallPixel> pixels, List<Integer> groupIDs, int lowestID) {
        for (int i = 0; i < pixels.size(); i++) {
            if (groupIDs.contains(pixels.get(i).groupId)) {
                pixels.get(i).groupId = lowestID;
            }
        }
        return pixels;
    }

    /**
     * Returns the pixel group id for the specific x and y value in the wall pixel list. Returns -1
     * if the wallpixel does not exist
     *
     * @param x
     * @param y
     * @param pixels
     * @return
     */
    private int getPixelGroupID(int x, int y, List<WallPixel> pixels) {
        for (WallPixel wallPixel : pixels) {
            if (wallPixel.x == x && wallPixel.y == y) {
                return wallPixel.groupId;
            }
        }
        return -1;
    }

    /**
     * Determines an initial group id for a pixel, based on whether any neighbours already have a
     * group id. If no neighbours have an id, then a new one is created. There still may be some
     * overlaps of id with this technique, and a second pass is needed.
     *
     * @param x          x value of new pixel
     * @param y          y value of new pixel
     * @param pixels     list of already found pixels
     * @param maxGroupID the maximum id that has already been assigned
     * @return
     */
    private int determineInitGroupID(int x, int y, List<WallPixel> pixels, int maxGroupID) {
        for (WallPixel wallPixel : pixels) {
            if (Math.abs(wallPixel.x - x) <= 1 && Math.abs(wallPixel.y - y) <= 1) {
                //This is a neighbour
                return wallPixel.groupId;
            }
        }

        //There were no neighbours, return a new group id
        return maxGroupID + 1;
    }


    /**
     * Determines the midpoint in height between two contiguous lines
     * Currently not used, but kept
     *
     * @param inputBitmap
     * @return
     */
    private List<Coordinate> determineMidOfContiguous(Bitmap inputBitmap) {
        List<Coordinate> coords = new ArrayList<Coordinate>();

        Boolean currentlyContiguous = false;
        int lowerY = 0;

        for (int x = 0; x < inputBitmap.getWidth(); x++) {
            for (int y = 0; y < inputBitmap.getHeight(); y++) {
                if (inputBitmap.getPixel(x, y) == Color.WHITE) {
                    if (!currentlyContiguous) {
                        lowerY = y;
                        currentlyContiguous = true;
                    }
                } else {
                    if (currentlyContiguous) {
                        coords.add(new Coordinate(x, lowerY + (y - lowerY) / 2));
                    }
                    currentlyContiguous = false;
                }
            }
        }

        // Add to bitmap
        for (Coordinate c : coords) {
            inputBitmap.setPixel(c.x, c.y, Color.BLUE);
        }

        return coords;
    }

    // Decodes image and scales it to reduce memory consumption
    private Bitmap decodeFile(int inputBitmapLocation, Activity activity) {

        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        InputStream is = activity.getResources().openRawResource(inputBitmapLocation);
        BitmapFactory.decodeStream(is, null, o);

        // The new size we want to scale to


        // Find the correct scale value. It should be the power of 2.
        int scale = 1;
        while (o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                o.outHeight / scale / 2 >= REQUIRED_SIZE) {
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;

        //Make it mutable and return
        Bitmap output = BitmapFactory.decodeStream(is, null, o2);
        output = output.copy(output.getConfig(), true);
        return output;
    }

    /*Cuts off the bitmap at a specified value
     *
     * @param inputBitmap
     * @return
     */
    private Bitmap cutoffed(Bitmap inputBitmap) {
        int[] pixels = new int[inputBitmap.getWidth() * inputBitmap.getHeight()];
        inputBitmap.getPixels(pixels, 0, inputBitmap.getWidth(), 0, 0, inputBitmap.getWidth() - 1, inputBitmap.getHeight() - 1);

        for (int i = 0; i < pixels.length; i++) {

            if (Color.red(pixels[i]) < CUTOFFMAGNITUDE) {
                pixels[i] = Color.BLACK;
            } else {
                pixels[i] = Color.WHITE;
            }

        }
        inputBitmap.setPixels(pixels, 0, inputBitmap.getWidth(), 0, 0, inputBitmap.getWidth() - 1, inputBitmap.getHeight() - 1);
        return inputBitmap;
    }

    /**
     * Smooths an image using a rectangular brush, of size rectSmoothWidth
     *
     * @param inputBitmap
     * @return
     */
    private Bitmap rectangularSmooth(Bitmap inputBitmap) {

        if (RECTSMOOTHWIDTH <= 1)
            return inputBitmap;


        for (int rectStartX = 0; rectStartX <= inputBitmap.getWidth() - RECTSMOOTHWIDTH; rectStartX += RECTSMOOTHWIDTH) {
            for (int rectStartY = 0; rectStartY <= inputBitmap.getHeight() - RECTSMOOTHWIDTH; rectStartY += RECTSMOOTHWIDTH) {

                //Determine what color the square should be set to
                int color;
                if (shouldSmoothWhite(inputBitmap, rectStartX, rectStartY)) {
                    color = Color.WHITE;
                } else {
                    color = Color.BLACK;
                }

                //Create the pixels array
                int[] pixels = new int[RECTSMOOTHWIDTH * RECTSMOOTHWIDTH];
                Arrays.fill(pixels, color);

                //change the colors
                inputBitmap.setPixels(pixels, 0, RECTSMOOTHWIDTH, rectStartX, rectStartY, RECTSMOOTHWIDTH, RECTSMOOTHWIDTH);

            }
        }

        return inputBitmap;

    }

    /**
     * Returns true if the rectangle created from the start x and y and the rectSmoothWidth
     * should be smoothed to white based on half of the pixels in the square being white.
     * This is for a black and white image
     *
     * @param rectStartX
     * @param rectStartY
     * @return
     */
    private boolean shouldSmoothWhite(Bitmap inputBitmap, int rectStartX, int rectStartY) {


        int whiteCount = 0;
        int blackCount = 0;

        int pixelArea = RECTSMOOTHWIDTH * RECTSMOOTHWIDTH;
        int requiredWhites = pixelArea / 2 - 1;

        for (int x = 0; x < RECTSMOOTHWIDTH; x++) {
            for (int y = 0; y < RECTSMOOTHWIDTH; y++) {
                if (inputBitmap.getPixel(rectStartX + x, rectStartY + y) == Color.WHITE) {
                    whiteCount++;
                } else {
                    blackCount++;
                }
                /*
                TODO: Find a better way to do this. As is, this will lead to inconsistencies, depending on pixel placement
                if (whiteCount > requiredWhites) {
                    //Should MJ
                    return true;
                }*/
                if (blackCount > requiredWhites) {
                    return false;
                }
            }
        }


        return true;
    }

    /**
     * Converts a colored bitmap to a grayscale bitmap
     *
     * @param inputBitmap
     * @return
     */
    private Bitmap grayScale(Bitmap inputBitmap) {
        int width, height;

        height = inputBitmap.getHeight();
        width = inputBitmap.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();

        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(inputBitmap, 0, 0, paint);

        return bmpGrayscale;
    }
}
