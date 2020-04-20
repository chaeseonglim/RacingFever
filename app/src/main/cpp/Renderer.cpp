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
#include "ResourceManager.h"

using namespace std::chrono_literals;

namespace Engine2D {

Renderer *Renderer::getInstance() {
    static std::unique_ptr<Renderer> sRenderer = std::make_unique<Renderer>(ConstructorTag{});
    return sRenderer.get();
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

        threadState->width = width;
        threadState->height = height;
    });
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

void Renderer::job(Work work) {
    mWorkerThread.run([=](ThreadState *threadState) { work(); });
}

float Renderer::getAverageFps() {
    return averageFps;
}

void Renderer::requestDraw() {
    mWorkerThread.run(
            [=](ThreadState *threadState) { if (threadState->isStarted) draw(threadState); });
}

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
        return configHasAttribute(config, EGL_DEPTH_SIZE, 0);
    };

    const auto configIter = std::find_if(supportedConfigs.cbegin(), supportedConfigs.cend(),
                                         configMatches);

    config = (configIter != supportedConfigs.cend()) ? *configIter : supportedConfigs[0];

    const EGLint contextAttributes[] = {
            EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL_NONE
    };

    context = eglCreateContext(display, config, nullptr, contextAttributes);

    glDisable(GL_CULL_FACE);
    glDisable(GL_DEPTH_TEST);

    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    makeCurrent(EGL_NO_SURFACE);

    // init program objects
    SpriteManager::getInstance()->init();
}

Renderer::ThreadState::~ThreadState() {
    clearSurface();
    ResourceManager::getInstance()->clear();
    SpriteManager::getInstance()->clear();
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
        averageFps = fpsSum / fpsCount;
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

    glViewport(0, 0, threadState->width, threadState->height);

    // Alpha blending
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    // Just fill the screen with a color.
    glClearColor(0.3f, 0.3f, 0.3f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT);

    glm::mat4 projection = glm::ortho(0.0f, static_cast<GLfloat>(threadState->width),
                                      static_cast<GLfloat>(threadState->height), 0.0f);
    SpriteManager::getInstance()->lock();
    auto& sprites = SpriteManager::getInstance()->getSpriteList();
    for (auto& sprite: sprites)
        sprite->draw(projection);
    SpriteManager::getInstance()->unlock();

    SwappyGL_swap(threadState->display, threadState->surface);

    // If we're still started, request another frame
    requestDraw();
}

}