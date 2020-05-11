//
// Created by crims on 2020-05-11.
//

#include <GLES3/gl3.h>

#include "Log.h"
#include "Utils.h"

#define LOG_TAG "Utils"

namespace Engine2D {

    bool checkGlError(const char *op) {
        bool isError = false;
        for (GLint error = glGetError(); error; error = glGetError()) {
            ALOGI("after %s() glError (0x%x)\n", op, error);
            isError = true;
        }
        return isError;
    }

}

