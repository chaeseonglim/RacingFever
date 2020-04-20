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
        this.context = context;
    }

    public boolean loadTexture(String asset) {
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
                result = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return result;
    }

    private Context context;

    private native boolean nLoadTexture(String name, byte[] image);
    private native void nReleaseTexture(String name);
    private native boolean nIsTextureLoaded(String name);
}
