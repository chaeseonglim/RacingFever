package com.lifejourney.racingfever;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

public class ResourceManager {

    private ResourceManager() {}
    private static class Singleton {
        private static final ResourceManager instance = new ResourceManager();
    }

    public static ResourceManager GetInstance() {
        return Singleton.instance;
    }

    public void addContext(Context context) {
        mContext = context;
    }

    public void loadTexture(String assetName) {
        try {
            InputStream is = mContext.getAssets().open(assetName);
            byte[] fileBytes = new byte[is.available()];
            is.read(fileBytes);
            is.close();

            nLoadTexture(assetName, fileBytes);
        } catch (
                IOException e) {
            e.printStackTrace();
        }
    }

    private Context mContext;

    private native void nLoadTexture(String name, byte[] image);
    private native void nReleaseTexture(String name);
}
