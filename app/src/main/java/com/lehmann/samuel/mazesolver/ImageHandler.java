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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.Math;

import java.io.Console;
import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * Created by samuel on 2017-11-28.
 */

public class ImageHandler {

    //The highest value that is not cutoff (up to 255)
    final double cutOffMagnitude = 110;
    final int REQUIRED_SIZE = 300;

    //the width of the rectangle used to smooth
    final int rectSmoothWidth = 2;

    ImageView left, right;


    public ImageHandler(int inputBitmapLocation, Activity activity) {

        left = activity.findViewById(R.id.left);
        right = activity.findViewById(R.id.right);

        Bitmap inputBitmap = decodeFile(inputBitmapLocation, activity);
        //Convert the bitmap to black and white to prevent necessity of euclidean distance comparison (slower)
        Bitmap greyedBitmap = grayScale(inputBitmap);
        Bitmap outputBitmap = cutoffed(greyedBitmap);
        Bitmap smoothedBitmap = rectangularSmooth(outputBitmap);


        Log.v("sam", "HEIGHT" + Integer.toString(inputBitmap.getHeight()));


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

            if (Color.red(pixels[i]) < cutOffMagnitude) {
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
     * @param inputBitmap
     * @return
     */
    private Bitmap rectangularSmooth(Bitmap inputBitmap) {

        if (rectSmoothWidth <= 1)
            return inputBitmap;



        for (int rectStartX = 0; rectStartX <= inputBitmap.getWidth() - rectSmoothWidth; rectStartX += rectSmoothWidth) {
            for (int rectStartY = 0; rectStartY <= inputBitmap.getHeight() - rectSmoothWidth; rectStartY += rectSmoothWidth) {

                //Determine what color the square should be set to
                int color;
                if (shouldSmoothWhite(inputBitmap, rectStartX, rectStartY)) {
                    color = Color.WHITE;
                } else {
                    color = Color.BLACK;
                }

                //Create the pixels array
                int[] pixels = new int[rectSmoothWidth * rectSmoothWidth];
                Arrays.fill(pixels, color);

                //change the colors
                inputBitmap.setPixels(pixels, 0, rectSmoothWidth, rectStartX, rectStartY, rectSmoothWidth, rectSmoothWidth);

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

        int pixelArea = rectSmoothWidth * rectSmoothWidth;
        int requiredWhites = pixelArea / 2 - 1;

        for (int x = 0; x < rectSmoothWidth; x++) {
            for (int y = 0; y < rectSmoothWidth; y++) {
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
