//
// Created by crims on 2020-04-18.
//

#include "Renderer.h"

#define LOG_TAG "Renderer"

#include <memory>
#include <GLES2/gl2.h>
#include <android/native_window.h>
#include <glm/gtc/matrix_transform.hpp>

#include "Log.h"

#include "swappy/swappyGL.h"
#include "swappy/swappyGL_extra.h"

#include "SpriteManager.h"
#include "ShapeManager.h"
#include "ResourceManager.h"

using namespace std::chrono_literals;

namespace Engine2D {

Renderer::ThreadState::ThreadState() {
    display = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    eglInitialize(display, 0, 0);

    const EGLint configAttributes[] = {
            EGL_RENDERABLE_TYPE, EGL_OPENGL_ES3_BIT,
            EGL_BLUE_SIZE, 8,
            EGL_GREEN_SIZE, 8,
            EGL_RED_SIZE, 8,
            EGL_NONE
    };

    EGLint numConfigs = 0;
    eglChooseConfig(display, configAttributes, nullptr, 0, &numConfigs);
    std::vector<EGLConfig> supportedConfigs(static_cast<size_t>(numConfigs));
    eglChooseConfig(display, configAttributes, supportedConfigs.data(), numConfigs, &numConfigs);

    // Choose a config, either a match if possible or the first config otherwise

    const auto configMatches = [&](EGLConfig config) {
        if (!configHasAttribute(config, EGL_RED_SIZE, 8)) return false;
        if (!configHasAttribute(config, EGL_GREEN_SIZE, 8)) return false;
        if (!configHasAttribute(config, EGL_BLUE_SIZE, 8)) return false;
        return true;
    };

    const auto configIter = std::find_if(supportedConfigs.cbegin(), supportedConfigs.cend(),
                                         configMatches);

    config = (configIter != supportedConfigs.cend()) ? *configIter : supportedConfigs[0];

    const EGLint contextAttributes[] = {
            EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL_NONE
    };

    context = eglCreateContext(display, config, nullptr, contextAttributes);
}

Renderer::ThreadState::~ThreadState() {
    clearSurface();
    ResourceManager::getInstance()->releaseAllTextures();
    SpriteManager::getInstance()->clear();
    ShapeManager::getInstance()->releaseAll();
    if (context != EGL_NO_CONTEXT) eglDestroyContext(display, context);
    if (display != EGL_NO_DISPLAY) eglTerminate(display);
}

void Renderer::ThreadState::clearSurface() {
    if (surface == EGL_NO_SURFACE) return;

    makeCurrent(EGL_NO_SURFACE);
    eglDestroySurface(display, surface);
    surface = EGL_NO_SURFACE;
}

bool Renderer::ThreadState::configHasAttribute(EGLConfig config, EGLint attribute, EGLint value) {
    EGLint outValue = 0;
    EGLBoolean result = eglGetConfigAttrib(display, config, attribute, &outValue);
    return result && (outValue == value);
}

EGLBoolean Renderer::ThreadState::makeCurrent(EGLSurface surface) {
    return eglMakeCurrent(display, surface, surface, context);
}


Renderer *Renderer::getInstance() {
    static std::unique_ptr<Renderer> sRenderer = std::make_unique<Renderer>(ConstructorTag{});
    return sRenderer.get();
}

Renderer::Renderer(ConstructorTag) {
}

void startFrameCallback(void *, int, long) {
}

void swapIntervalChangedCallback(void *) {
    uint64_t swap_ns = SwappyGL_getSwapIntervalNS();
    ALOGI("Swappy changed swap interval to %.2fms", swap_ns / 1e6f);
}

/** Test using an external thread provider */
static int threadStart(SwappyThreadId* thread_id, void *(*thread_func)(void*), void* user_data) {
    return ThreadManager::Instance().Start(thread_id, thread_func, user_data);
}
static void threadJoin(SwappyThreadId thread_id) {
    ThreadManager::Instance().Join(thread_id);
}
static bool threadJoinable(SwappyThreadId thread_id) {
    return ThreadManager::Instance().Joinable(thread_id);
}
static SwappyThreadFunctions sThreadFunctions = {
        threadStart, threadJoin, threadJoinable
};
/**/

void Renderer::init(JNIEnv* env, jobject activity) {
    if (!mInitialized) {
        // Should never happen
        if (Swappy_version() != SWAPPY_PACKED_VERSION) {
            ALOGE("Inconsistent Swappy versions");
        }

        Swappy_setThreadFunctions(&sThreadFunctions);

        SwappyGL_init(env, activity);

        SwappyTracer tracers;
        tracers.preWait = nullptr;
        tracers.postWait = nullptr;
        tracers.preSwapBuffers = nullptr;
        tracers.postSwapBuffers = nullptr;
        tracers.startFrame = startFrameCallback;
        tracers.userData = nullptr;
        tracers.swapIntervalChanged = swapIntervalChangedCallback;

        SwappyGL_injectTracer(&tracers);

        mInitialized = true;
    }
}

void Renderer::close() {
    SwappyGL_destroy();
}

void Renderer::setWindow(ANativeWindow *window, int32_t width, int32_t height) {
    mWorkerThread.run([=](ThreadState *threadState) {
        threadState->clearSurface();

        ALOGI("Creating window surface %dx%d", width, height);

        if (!window) return;

        threadState->surface =
                eglCreateWindowSurface(threadState->display, threadState->config, window, NULL);
        ANativeWindow_release(window);
        if (!threadState->makeCurrent(threadState->surface)) {
            ALOGE("Unable to eglMakeCurrent");
            threadState->surface = EGL_NO_SURFACE;
            return;
        }

        threadState->windowSize = Size(width, height);
    });

    mWorkerThread.run([](ThreadState *threadState) {
        // initProgram program objects
        if (!SpriteManager::getInstance()->initPrograms() ||
            !ShapeManager::getInstance()->initPrograms()) {
            std::terminate();
        }
    });
}

void Renderer::setViewport(int32_t x, int32_t y, int32_t width, int32_t height) {
    Rect viewport(x, y, width, height);

    /*
    ALOGE("Setting new viewport(%d,%d,%d,%d)",
          viewport.getX(),
          viewport.getY(),
          viewport.getWidth(),
          viewport.getHeight());
    */

    mViewport = viewport;
}

void Renderer::start() {
    mWorkerThread.run([this](ThreadState *threadState) {
        threadState->isStarted = true;
        requestDraw();
    });
}

void Renderer::stop() {
    mWorkerThread.run([=](ThreadState *threadState) { threadState->isStarted = false; });
}

void Renderer::run(Work work) {
    mWorkerThread.run([=](ThreadState *threadState) { work(); });
}

float Renderer::getAverageFps() {
    return mAverageFps;
}

void Renderer::requestDraw() {
    mWorkerThread.run([=](ThreadState *threadState) {
        if (threadState->isStarted) draw(threadState); });
}

// should be called once per draw as this function maintains the time delta between calls
void Renderer::calculateFps() {
    static constexpr int FPS_SAMPLES = 10;
    static std::chrono::steady_clock::time_point prev = std::chrono::steady_clock::now();
    static float fpsSum = 0;
    static int fpsCount = 0;

    std::chrono::steady_clock::time_point now = std::chrono::steady_clock::now();
    fpsSum += 1.0f / ((now - prev).count() / 1e9f);
    fpsCount++;
    if (fpsCount == FPS_SAMPLES) {
        mAverageFps = fpsSum / fpsCount;
        fpsSum = 0;
        fpsCount = 0;
    }
    prev = now;
}

void Renderer::draw(ThreadState *threadState) {
    // Don't render if we have no surface
    if (threadState->surface == EGL_NO_SURFACE) {
        // Sleep a bit so we don't churn too fast
        std::this_thread::sleep_for(50ms);
        requestDraw();
        return;
    }

    SwappyGL_recordFrameStart(threadState->display, threadState->surface);

    calculateFps();

    glViewport(0, 0, threadState->windowSize.getWidth(), threadState->windowSize.getHeight());

    // Cull face
    glDisable(GL_CULL_FACE);

    // Depth testing
    glDisable(GL_DEPTH_TEST);

    // Alpha blending
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    // Just fill the screen with a color.
    glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
    glClear(GL_COLOR_BUFFER_BIT);

    {
        std::unique_lock<std::mutex> lock(mDrawLock);

        glm::mat4 projection = glm::ortho(0.0f,
                                          static_cast<GLfloat>(mViewport.getWidth()),
                                          static_cast<GLfloat>(mViewport.getHeight()),
                                          0.0f);
        glm::mat4 model(1.0f);
        glm::vec2 viewportTransition(mViewport.getX(), mViewport.getY());
        model = glm::translate(model, glm::vec3(-viewportTransition, 0.0f));

        SpriteManager::SpriteList spriteList = SpriteManager::getInstance()->getSpriteList();
        //ALOGE("Number of sprites: %d", (int)spriteList.size());
        for (auto &sprite: spriteList) {
            sprite->draw(projection, model);
        }

        ShapeManager::ShapeList shapeList = ShapeManager::getInstance()->getShapeList();
        //ALOGE("Number of shapes: %d", (int)shapeList.size());
        for (auto &shape: shapeList) {
            shape->draw(projection, model);
        }
    }

    SwappyGL_swap(threadState->display, threadState->surface);

    // If we're still started, request another frame
    requestDraw();
}

}