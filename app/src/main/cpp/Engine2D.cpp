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
#include "Line.h"
#include "ShapeManager.h"
#include "Circle.h"

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

JNIEXPORT void JNICALL
Java_com_lifejourney_engine2d_Engine2D_nEngineInit(JNIEnv *env, jobject /* this */,
        jobject activity) {
    // Get the Renderer instance to create it
    Renderer::getInstance()->init(env, activity);
}

JNIEXPORT void JNICALL
Java_com_lifejourney_engine2d_Engine2D_nEngineFinalize(JNIEnv *env, jobject /* this */) {
    ResourceManager::getInstance()->releaseAllTextures();
    ShapeManager::getInstance()->releaseAll();
    //Renderer::getInstance()->close();
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
JNIEXPORT void JNICALL
Java_com_lifejourney_engine2d_Engine2D_nEngineLockDraw(JNIEnv *env, jobject thiz) {
    Renderer::getInstance()->lockDraw();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_lifejourney_engine2d_Engine2D_nEngineUnlockDraw(JNIEnv *env, jobject thiz) {
    Renderer::getInstance()->unlockDraw();
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
JNIEXPORT void JNICALL
Java_com_lifejourney_engine2d_ResourceManager_nReleaseAllTextures(JNIEnv *env, jobject thiz) {
    ResourceManager::getInstance()->releaseAllTextures();
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_lifejourney_engine2d_Sprite_nCreateSprite(JNIEnv *env, jobject thiz, jstring asset,
                                                   jint gridCols, jint gridRows) {
    std::string textureNameS = to_string(asset, env);

    if (textureNameS.length() > 0) {
        std::shared_ptr<Texture> texture = ResourceManager::getInstance()->getTexture(textureNameS);
        return SpriteManager::getInstance()->add(std::make_shared<Sprite>(texture, gridCols, gridRows));
    }

    return -1;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_lifejourney_engine2d_Sprite_nDestroySprite(JNIEnv *env, jobject thiz, jint id) {
    SpriteManager::getInstance()->remove(id);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_lifejourney_engine2d_Sprite_nSetProperties(JNIEnv *env, jobject thiz, jint id, jint x, jint y,
                                                jint width, jint height, jint layer,
                                                jfloat depth, jfloat rotation, jboolean visible,
                                                jint gridCol, jint gridRow) {
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
    sprite->setGridIndex(gridCol, gridRow);
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_lifejourney_engine2d_Line_nCreateLine(JNIEnv *env, jobject thiz, jfloat begin_x, jfloat begin_y, jfloat end_x,
                 jfloat end_y, jfloat r, jfloat g, jfloat b, jfloat a, jint layer) {
    auto line = std::make_shared<Line>(
            glm::vec2(begin_x, begin_y), glm::vec2(end_x, end_y), glm::vec4(r, g, b, a));
    line->setLayer(layer);
    return ShapeManager::getInstance()->add(line);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_lifejourney_engine2d_Line_nDestoryLine(JNIEnv *env, jobject thiz, jint id) {
    ShapeManager::getInstance()->remove(id);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_lifejourney_engine2d_Line_nSetProperties(JNIEnv *env, jobject thiz, jint id,
                                                  jfloat begin_x, jfloat begin_y, jfloat end_x, jfloat end_y,
                                                  jfloat r, jfloat g, jfloat b, jfloat a, jint layer,
                                                  jfloat depth, jboolean visible) {
    std::shared_ptr<Line> line =
            std::dynamic_pointer_cast<Line>(ShapeManager::getInstance()->get(id));
    if (line == nullptr) {
        ALOGW("Invalid line %d", id);
        return;
    }

    line->setBegin(glm::vec2(begin_x, begin_y));
    line->setEnd(glm::vec2(end_x, end_y));
    line->setColor(glm::vec4(r, g, b, a));
    line->setLayer(layer);
    line->setDepth(depth);
    line->setVisible(visible);
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_lifejourney_engine2d_Circle_nCreateCircle(JNIEnv *env, jobject thiz, jfloat center_x,
                                                   jfloat center_y, jfloat radius, jfloat r,
                                                   jfloat g, jfloat b, jfloat a, jint layer) {
    auto circle = std::make_shared<Circle>(
            glm::vec2(center_x, center_y), radius, glm::vec4(r, g, b, a));
    circle->setLayer(layer);
    return ShapeManager::getInstance()->add(circle);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_lifejourney_engine2d_Circle_nDestoryCircle(JNIEnv *env, jobject thiz, jint id) {
    ShapeManager::getInstance()->remove(id);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_lifejourney_engine2d_Circle_nSetProperties(JNIEnv *env, jobject thiz, jint id,
                                                    jfloat center_x, jfloat center_y, jfloat radius,
                                                    jfloat r, jfloat g, jfloat b, jfloat a,
                                                    jint layer, jfloat depth, jboolean visible) {
    std::shared_ptr<Circle> circle =
            std::dynamic_pointer_cast<Circle>(ShapeManager::getInstance()->get(id));
    if (circle == nullptr) {
        ALOGW("Invalid circle %d", id);
        return;
    }

    circle->setCenter(glm::vec2(center_x, center_y));
    circle->setRadius(radius);
    circle->setColor(glm::vec4(r, g, b, a));
    circle->setLayer(layer);
    circle->setDepth(depth);
    circle->setVisible(visible);
}
