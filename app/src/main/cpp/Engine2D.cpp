//
// Created by crims on 2020-04-18.
//
/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#define LOG_TAG "Engine2D"

#include <cmath>
#include <string>

#include <jni.h>

#include <android/native_window_jni.h>

#include "Log.h"

#include "swappy/swappyGL.h"
#include "swappy/swappyGL_extra.h"

#include "Renderer.h"
#include "SpriteManager.h"

using std::chrono::nanoseconds;
using namespace Engine2D;

namespace {

    std::string to_string(jstring jstr, JNIEnv *env) {
        const char *utf = env->GetStringUTFChars(jstr, nullptr);
        std::string str(utf);
        env->ReleaseStringUTFChars(jstr, utf);
        return str;
    }

} // anonymous namespace

extern "C" {

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

JNIEXPORT void JNICALL
Java_com_lifejourney_engine2d_Engine2D_nEngineInit(JNIEnv *env, jobject /* this */,
        jobject activity) {
    // Get the Renderer instance to create it
    Renderer::getInstance();

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
}

JNIEXPORT void JNICALL
Java_com_lifejourney_engine2d_Engine2D_nEngineSetSurface(JNIEnv *env, jobject /* this */,
                                                               jobject surface, jint width, jint height) {
    ANativeWindow *window = ANativeWindow_fromSurface(env, surface);
    Renderer::getInstance()->setWindow(window,
        static_cast<int32_t>(width),
        static_cast<int32_t>(height));
}

JNIEXPORT void JNICALL
Java_com_lifejourney_engine2d_Engine2D_nEngineClearSurface(JNIEnv * /* env */, jobject /* this */) {
    Renderer::getInstance()->setWindow(nullptr, 0, 0);
}

JNIEXPORT void JNICALL
Java_com_lifejourney_engine2d_Engine2D_nEngineStart(JNIEnv * /* env */, jobject /* this */) {
    Renderer::getInstance()->start();
}

JNIEXPORT void JNICALL
Java_com_lifejourney_engine2d_Engine2D_nEngineStop(JNIEnv * /* env */, jobject /* this */) {
    Renderer::getInstance()->stop();
}

JNIEXPORT void JNICALL
Java_com_lifejourney_engine2d_Engine2D_nEngineSetAutoSwapInterval(JNIEnv *env, jobject /* this */,
                                                                        jboolean enabled) {
    SwappyGL_setAutoSwapInterval(enabled);
}

JNIEXPORT float JNICALL
Java_com_lifejourney_engine2d_Engine2D_nEngineGetAverageFps(JNIEnv * /* env */, jobject /* this */) {
    return Renderer::getInstance()->getAverageFps();
}

JNIEXPORT uint64_t JNICALL
Java_com_lifejourney_engine2d_Engine2D_nEngineGetSwappyStats(JNIEnv * /* env */,
                                                                   jobject /* this */,
                                                                   jint stat,
                                                                   jint bin) {
    static bool enabled = false;
    if (!enabled) {
        SwappyGL_enableStats(true);
        enabled = true;
    }

    // stats are read one by one, query once per stat
    static SwappyStats stats;
    static int stat_idx = -1;

    if (stat_idx != stat) {
        SwappyGL_getStats(&stats);
        stat_idx = stat;
    }

    uint64_t value = 0;

    if (stats.totalFrames) {
        switch (stat) {
        case 0:
            value = stats.idleFrames[bin];
            break;
        case 1:
            value = stats.lateFrames[bin];
            break;
        case 2:
            value = stats.offsetFromPreviousFrame[bin];
            break;
        case 3:
            value = stats.latencyFrames[bin];
            break;
        default:
            return stats.totalFrames;
        }
        value = std::round(value * 100.0f / stats.totalFrames);
    }

return value;
}

} // extern "C"

extern "C"
JNIEXPORT void JNICALL
Java_com_lifejourney_engine2d_Engine2D_nEngineSetViewport(JNIEnv *env, jobject thiz, jint x, jint y,
                                                              jint width, jint height) {
    Renderer::getInstance()->setViewport(x, y, width, height);
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_lifejourney_engine2d_ResourceManager_nLoadTexture(JNIEnv *env, jobject thiz,
                                                              jstring name, jbyteArray image) {

    std::string nameS = to_string(name, env);
    if (nameS.empty()) {
        ALOGW("Name is empty");
        return false;
    }

    auto imageSize = env->GetArrayLength(image);
    if (imageSize == 0) {
        ALOGW("Image is empty");
        return false;
    }

    jbyte *cImage = env->GetByteArrayElements(image, nullptr);
    if (ResourceManager::getInstance()->loadTexture((const unsigned char*)cImage, imageSize, true, nameS) == nullptr) {
        ALOGW("Failed to load texture from memory")
        env->ReleaseByteArrayElements(image, cImage, 0);
        return false;
    }
    env->ReleaseByteArrayElements(image, cImage, 0);
    return true;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_lifejourney_engine2d_ResourceManager_nAttachTexture(JNIEnv *env, jobject thiz,
                                                                jstring name) {
    std::string nameS = to_string(name, env);
    if (nameS.empty()) {
        ALOGW("Name is empty");
        return false;
    }

    if (ResourceManager::getInstance()->attachTexture(nameS) == nullptr) {
        ALOGW("Failed to attach texture %s", nameS.c_str());
        return false;
    }

    return true;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_lifejourney_engine2d_ResourceManager_nReleaseTexture(JNIEnv *env, jobject thiz,
                                                                 jstring name) {
    std::string nameS = to_string(name, env);
    if (nameS.empty()) {
        ALOGW("Name is empty");
        return;
    }

    ResourceManager::getInstance()->releaseTexture(nameS);
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_lifejourney_engine2d_ResourceManager_nIsTextureLoaded(JNIEnv *env, jobject thiz,
                                                                  jstring name) {
    std::string nameS = to_string(name, env);
    if (nameS.empty()) {
        ALOGW("Name is empty");
        return false;
    }

    return (ResourceManager::getInstance()->getTexture(nameS) != nullptr);
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_lifejourney_engine2d_Sprite_nCreateSprite(JNIEnv *env, jobject thiz, jstring asset) {
    std::string textureNameS = to_string(asset, env);

    if (textureNameS.length() > 0) {
        std::shared_ptr<Texture> texture = ResourceManager::getInstance()->getTexture(textureNameS);

        return SpriteManager::getInstance()->add(std::make_shared<Sprite>(texture));
    }

    return -1;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_lifejourney_engine2d_Sprite_nDestorySprite(JNIEnv *env, jobject thiz, jint id) {
    SpriteManager::getInstance()->remove(id);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_lifejourney_engine2d_Sprite_nSetProperties(JNIEnv *env, jobject thiz, jint id, jint x, jint y,
                                                jint width, jint height, jint layer,
                                                jfloat depth, jfloat rotation, jboolean visible) {
    auto sprite = SpriteManager::getInstance()->get(id);
    if (sprite == nullptr) {
        ALOGW("Invalid sprite %d", id);
        return;
    }

    sprite->setPos(glm::vec2((float)x, (float)y));
    sprite->setSize(glm::vec2(width, height));
    sprite->setLayer(layer);
    sprite->setDepth(depth);
    sprite->setRotation(glm::radians(rotation));
    sprite->setVisible(visible);
}
