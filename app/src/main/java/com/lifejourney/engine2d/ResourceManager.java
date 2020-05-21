package com.lifejourney.engine2d;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.lifejourney.racingfever.InfoBitmap;

import java.io.IOException;
import java.io.InputStream;

public class ResourceManager {

    private static final String LOG_TAG = "ResourceManager";

    public ResourceManager(Context context) {
        this.context = context;
    }

    public boolean loadTexture(String asset) {
        // NOTE: It won't give your texture object instead it's added inside the 2D engine.

        boolean result = false;

        try {
            if (!nIsTextureLoaded(asset)) {
                InputStream is = context.getAssets().open(asset);
                byte[] fileBytes = new byte[is.available()];
                is.read(fileBytes);
                is.close();

                result = nLoadTexture(asset, fileBytes);
            }
            else {
                result = nAttachTexture(asset);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return result;
    }

    public void releaseTexture(String asset) {
        nReleaseTexture(asset);
    }

    public InfoBitmap loadGrayscaleBitmap(String asset) {
        InfoBitmap infoBitmap = null;
        try {
            InputStream istr = context.getAssets().open(asset);
            BitmapFactory.Options option = new BitmapFactory.Options();
            option.outConfig = android.graphics.Bitmap.Config.ALPHA_8;
            infoBitmap = new InfoBitmap(BitmapFactory.decodeStream(istr, null, option));
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed to load bitmap: " + asset);
            e.printStackTrace();
        }

        return infoBitmap;
    }

    private Context context;

    private native boolean nLoadTexture(String name, byte[] image);
    private native boolean nAttachTexture(String name);
    private native void nReleaseTexture(String name);
    private native boolean nIsTextureLoaded(String name);
}
