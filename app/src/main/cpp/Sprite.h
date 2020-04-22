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

#pragma once

#include <array>
#include <GLES3/gl3.h>
#include <glm/glm.hpp>
#include "Texture.h"

namespace Engine2D
{

class Sprite
{
public:
    Sprite() = default;
    Sprite(const std::shared_ptr<Texture>& texture);
    Sprite(const glm::vec2 &pos, const glm::vec2 &size, GLfloat rotation,
           const std::shared_ptr<Texture>& texture = nullptr, const glm::vec3 &color = glm::vec3());
    virtual ~Sprite();

    void prepare();
    void cleanup();
    void draw(const glm::mat4 &projection);

    void show() { mVisible = true; }
    void hide() { mVisible = false;}
    void setVisible(bool visible) { mVisible = visible; }
    bool isVisible() const { return mVisible; }

    const glm::vec2 &getPos() const;
    void setPos(const glm::vec2 &pos);

    const glm::vec2 &getSize() const;
    void setSize(const glm::vec2 &size);

    GLfloat getRotation() const;
    void setRotation(GLfloat rotation);

    const glm::vec3 &getColor() const;
    void setColor(const glm::vec3 &color);

private:
    struct ProgramState {
        ProgramState();

        GLuint program;
        GLint colorHandle;
        GLint modelHandle;
        GLint projectionHandle;
    };
    static std::unique_ptr<ProgramState> sProgramState;

public:
    static void init();

private:
    glm::vec2 mPos;
    glm::vec2 mSize;
    GLfloat mRotation = 0.0f;
    std::shared_ptr<Texture> mTexture;
    glm::vec3 mColor;
    GLuint mQuadVAO;
    bool mPrepared = false;
    bool mVisible = false;
};

} // namespace samples
