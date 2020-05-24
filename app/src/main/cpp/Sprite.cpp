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

#include "Sprite.h"

#define LOG_TAG "Sprite"

#include <cmath>
#include <cstdlib>
#include <string>
#include <vector>

#include <GLES3/gl3.h>
#include <GLES3/gl3ext.h>

#include <glm/glm.hpp>
#include <glm/gtc/type_ptr.hpp>
#include <glm/gtc/matrix_transform.hpp>

#include "Log.h"
#include "Renderer.h"
#include "Utils.h"

namespace SpriteProgram {

auto const sVertexShader =
    "#version 300 es\n"

    "layout (location = 0) in vec4 vertex; // <vec2 position, vec2 texCoords>\n"
    "out vec2 TexCoords;\n"

    "uniform mat4 model;\n"
    "uniform mat4 projection;\n"

    "void main()\n"
    "{\n"
    "    TexCoords = vertex.zw;\n"
    "    gl_Position = projection * model * vec4(vertex.xy, 0.0, 1.0);\n"
    "}\n";
auto const sFragmentShader =
    "#version 300 es\n"
    "precision mediump float;"

    "in vec2 TexCoords;\n"
    "out vec4 color;\n"

    "uniform sampler2D image;\n"

    "void main()\n"
    "{\n"
    "    color = texture(image, TexCoords);\n"
    "}\n";

bool checkGlError(const char *op) {
    bool isError = false;
    for (GLint error = glGetError(); error; error = glGetError()) {
        ALOGI("after %s() glError (0x%x)\n", op, error);
        isError = true;
    }
    return isError;
}

GLuint loadShader(GLenum shaderType, const char *pSource) {
    GLuint shader = glCreateShader(shaderType);
    if (shader == 0) {
        return shader;
    }

    glShaderSource(shader, 1, &pSource, NULL);
    glCompileShader(shader);
    GLint compiled = GL_FALSE;
    glGetShaderiv(shader, GL_COMPILE_STATUS, &compiled);
    if (!compiled) {
        GLint infoLength = 0;
        glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &infoLength);
        if (infoLength > 0) {
            std::vector<char> info((size_t)infoLength, '\0');
            glGetShaderInfoLog(shader, infoLength, NULL, info.data());
            ALOGE("Could not compile shader %d:\n%s\n", shaderType, info.data());
            glDeleteShader(shader);
            shader = 0;
        }
    }

    return shader;
}

GLuint createProgram(const char *pVertexSource, const char *pFragmentSource) {
    GLuint vertexShader = loadShader(GL_VERTEX_SHADER, pVertexSource);
    if (!vertexShader) {
        ALOGE("Failed to load vertex shader");
        return 0;
    }

    GLuint pixelShader = loadShader(GL_FRAGMENT_SHADER, pFragmentSource);
    if (!pixelShader) {
        ALOGE("Failed to load vertex shader");
        return 0;
    }

    GLuint program = glCreateProgram();
    if (program == 0) {
        ALOGE("Failed to create program");
        return program;
    }

    glAttachShader(program, vertexShader);
    checkGlError("glAttachShader");
    glAttachShader(program, pixelShader);
    checkGlError("glAttachShader");
    glLinkProgram(program);
    GLint linkStatus = GL_FALSE;
    glGetProgramiv(program, GL_LINK_STATUS, &linkStatus);
    if (!linkStatus) {
        GLint infoLength = 0;
        glGetProgramiv(program, GL_INFO_LOG_LENGTH, &infoLength);
        if (infoLength > 0) {
            std::vector<char> info((size_t)infoLength, '\0');
            glGetProgramInfoLog(program, infoLength, NULL, info.data());
            ALOGE("Could not link program:\n%s\n", info.data());
        }
        glDeleteProgram(program);
        program = 0;
    }
    return program;
}

} // anonymous namespace

namespace Engine2D {

std::unique_ptr<Sprite::ProgramState> Sprite::sProgramState = nullptr;

bool Sprite::initProgram()
{
    if (sProgramState == nullptr)
        sProgramState.reset(new Sprite::ProgramState);
    return sProgramState->program != 0;

}

Sprite::ProgramState::ProgramState() {
    using namespace SpriteProgram;

    program = createProgram(sVertexShader, sFragmentShader);
    if (program == 0) {
        ALOGE("Failed to create program");
        checkGlError("createProgram");
        return;
    }

    modelHandle = glGetUniformLocation(program, "model");
    checkGlError("glGetUniformLocation(model)");
    projectionHandle = glGetUniformLocation(program, "projection");
    checkGlError("glGetUniformLocation(projection)");
}

Sprite::Sprite(const std::shared_ptr<Texture>& texture, int gridCols, int gridRows)
    : mTexture(texture), mGridCols(gridCols), mGridRows(gridRows)
{
    prepare();
}

Sprite::~Sprite()
{
    cleanup();
}

void Sprite::prepareInternal()
{
    if (!Sprite::sProgramState) {
        return;
    }

    bool error = false;

    ProgramState &state = *Sprite::sProgramState;

    glUseProgram(state.program);
    error &= checkGlError("glUseProgram");

    glGenVertexArrays(1, &mQuadVAO);
    error &= checkGlError("glGenVertexArrays");
    glGenBuffers(1, &mVBO);
    error &= checkGlError("glGenBuffers");

    float left = 0.0f, right = 0.0f, bottom = 0.0f, top = 0.0f;
    if (mTexture) {
        int textureWidth = mTexture->getWidth(), textureHeight = mTexture->getHeight();
        int textureGridWidth = textureWidth / mGridCols;
        int textureGridHeight = textureHeight / mGridRows;
        int textureGridLeft = textureGridWidth * mCurGridCol;
        int textureGridRight = textureGridLeft + textureGridWidth;
        int textureGridBottom = textureGridHeight * mCurGridRow;
        int textureGridTop = textureGridBottom + textureGridHeight;
        left = (textureGridLeft + 0.5f) / textureWidth;
        right = (textureGridRight - 0.5f) / textureWidth;
        bottom = (textureGridBottom + 0.5f) / textureHeight;
        top = (textureGridTop - 0.5f) / textureHeight;
    }
    GLfloat vertices[] =
            {
                    // Pos      // Tex
                    -0.5f,  0.5f, left, top,
                     0.5f, -0.5f, right, bottom,
                    -0.5f, -0.5f, left, bottom,

                    -0.5f,  0.5f, left, top,
                     0.5f,  0.5f, right, top,
                     0.5f, -0.5f, right, bottom
            };

    glBindBuffer(GL_ARRAY_BUFFER, mVBO);
    error &= checkGlError("glBindBuffer");
    glBufferData(GL_ARRAY_BUFFER, sizeof(vertices), vertices, GL_STATIC_DRAW);
    error &= checkGlError("glBufferData");

    glBindVertexArray(mQuadVAO);
    error &= checkGlError("glBindVertexArray");
    glVertexAttribPointer(0, 4, GL_FLOAT, GL_FALSE, 4 * sizeof(GLfloat), (GLvoid *) 0);
    error &= checkGlError("glVertexAttribPointer");
    glBindBuffer(GL_ARRAY_BUFFER, 0);
    error &= checkGlError("glBindBuffer");
    glBindVertexArray(0);
    error &= checkGlError("glBindVertexArray");

    if (!error)
        mPrepared = true;
    else
        ALOGW("Failed to prepare sprite");
}

void Sprite::prepare()
{
    Renderer::getInstance()->run([this]() {
        prepareInternal();
    });
}

void Sprite::cleanup()
{
    if (mPrepared) {
        Renderer::getInstance()->run([quadVAO = this->mQuadVAO, vbo = this->mVBO]() {
            glDeleteBuffers(1, &vbo);
            glDeleteVertexArrays(1, &quadVAO);
        });
    }
}

void Sprite::draw(const glm::mat4 &projection, const glm::mat4 &initialModel)
{
    if (!mVisible)
        return;

    if (!mPrepared || mNeedPrepareAgain) {
        if (mNeedPrepareAgain) {
            if (mPrepared) {
                glDeleteBuffers(1, &mVBO);
                glDeleteVertexArrays(1, &mQuadVAO);
            }
            mNeedPrepareAgain = false;
        }
        prepareInternal();
        if (!mPrepared) {
            ALOGW("Sprite is not prepared to draw");
            return;
        }
    }

    ProgramState &state = *Sprite::sProgramState;

    glUseProgram(state.program);
    checkGlError("glUseProgram");

    glm::mat4 model = glm::translate(initialModel, glm::vec3(mPos, 0.0f));
    model = glm::rotate(model, mRotation, glm::vec3(0.0f, 0.0f, 1.0f));
    model = glm::scale(model, glm::vec3(mSize.x, mSize.y, 1.0f));
    glUniformMatrix4fv(state.modelHandle, 1, GL_FALSE, glm::value_ptr(model));
    checkGlError("glUniformMatrix4fv(model)");

    glUniformMatrix4fv(state.projectionHandle, 1, GL_FALSE, glm::value_ptr(projection));
    checkGlError("glUniformMatrix4fv(projection)");

    glBindVertexArray(mQuadVAO);
    checkGlError("glBindVertexArray");
    glEnableVertexAttribArray(0);
    checkGlError("glEnableVertexAttribArray");

    if (mTexture) {
        glActiveTexture(GL_TEXTURE0);
        checkGlError("glActiveTexture");
        mTexture->bind();
    }

    glDrawArrays(GL_TRIANGLES, 0, 6);
    checkGlError("glDrawArrays");

    glBindVertexArray(0);
    checkGlError("glBindVertexArray");
    glDisableVertexAttribArray(0);
    checkGlError("glDisableVertexAttribArray");
}

const void Sprite::setGridIndex(int gridCol, int gridRow) {
    if (gridCol >= mGridCols || gridRow >= mGridRows) {
        ALOGE("Wrong grid index of sprite: %d %d (%d %d)", gridCol, gridRow,
                mGridCols, mGridRows);
        return;
    }

    if (gridCol == mCurGridCol && gridRow == mCurGridRow) {
        return;
    }

    mCurGridCol = gridCol;
    mCurGridRow = gridRow;
    mNeedPrepareAgain = true;
}

} // namespace samples
