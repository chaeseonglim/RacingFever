//
// Created by crims on 2020-04-18.
//

#ifndef RACINGFEVER_RENDERER_H
#define RACINGFEVER_RENDERER_H


#include <EGL/egl.h>

#include "Thread.h"
#include "WorkerThread.h"

namespace Engine2D {

class Renderer {

    // Prevent construct from outside
    struct ConstructorTag {
    };

public:
    explicit Renderer(ConstructorTag) {}

    static Renderer *getInstance();

    // Sets the active window to render into
    // Takes ownership of window and will release its reference
    void setWindow(ANativeWindow *window, int32_t width, int32_t height);

    void start();

    void stop();

    using Work = std::function<void()>;
    void job(Work work);

    float getAverageFps();

    void requestDraw();

private:
    class ThreadState {
    public:
        ThreadState();
        ~ThreadState();

        void clearSurface();

        bool configHasAttribute(EGLConfig config, EGLint attribute, EGLint value);

        EGLBoolean makeCurrent(EGLSurface surface);

        EGLDisplay display = EGL_NO_DISPLAY;
        EGLConfig config = static_cast<EGLConfig>(0);
        EGLSurface surface = EGL_NO_SURFACE;
        EGLContext context = EGL_NO_CONTEXT;

        bool isStarted = false;

        std::chrono::nanoseconds refreshPeriod = std::chrono::nanoseconds{0};
        int64_t swapIntervalNS = 0;
        int32_t width = 0;
        int32_t height = 0;
    };

    void draw(ThreadState *threadState);
    void calculateFps();

    WorkerThread<ThreadState> mWorkerThread = {"Renderer", Affinity::None};

    float averageFps = -1.0f;
};

}

#endif //RACINGFEVER_RENDERER_H
