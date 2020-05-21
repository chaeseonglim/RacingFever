package com.lifejourney.racingfever;

import android.util.Log;

public class InfoBitmap {

    private static String LOG_TAG = "InfoBitmap";

    public InfoBitmap(android.graphics.Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public byte[][] get2DByteArray() {
        byte[][] array = new byte[getHeight()][getWidth()];
        int[] row = new int[getWidth()];
        for (int y = 0; y < bitmap.getHeight(); ++y) {
            bitmap.getPixels(row, 0, getWidth(), 0, y, getWidth(), 1);
            for (int x = 0; x < bitmap.getWidth(); ++x) {
                array[y][x] = (byte)(row[x] & 0x000000FF);
            }
        }

        return array;
    }

    public int getWidth() {
        return bitmap.getWidth();
    }

    public int getHeight() {
        return bitmap.getHeight();
    }

    private android.graphics.Bitmap bitmap;
}
