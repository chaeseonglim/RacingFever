package com.lifejourney.racingfever;

public class Camera {

    private Camera() {}
    private static class Singleton {
        private static final Camera instance = new Camera();
    }

    public static Camera GetInstance() {
        return Singleton.instance;
    }

    public Rect getViewport() {
        return viewport;
    }

    public void setViewport(Rect viewport) {
        this.viewport = viewport;
    }

    public void setViewport(int x, int y, int width, int height) {
        this.viewport = new Rect(x, y, width, height);
    }

    private Rect viewport;

    private native void nEngineSetResolution(int width, int height);
}
