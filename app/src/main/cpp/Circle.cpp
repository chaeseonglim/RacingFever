//
// Created by crims on 2020-05-11.
//

#include "Circle.h"

#define LOG_TAG "Circle"

#include <math.h>
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

namespace CircleProgram {

auto const sVertexShader =
        "#version 300 es\n"

        "layout (location = 0) in vec3 vertex;\n"

        "uniform mat4 model;\n"
        "uniform mat4 projection;\n"

        "void main()\n"
        "{\n"
        "    gl_Position = projection * model * vec4(vertex.xy, 0.0, 1.0);\n"
        "}\n";
auto const sFragmentShader =
        "#version 300 es\n"
        "precision mediump float;"

        "uniform vec4 vColor;"
        "out vec4 color;\n"

        "void main()\n"
        "{\n"
        "    color = vColor;\n"
        "}\n";

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

bool checkGlError(const char *op) {
    bool isError = false;
    for (GLint error = glGetError(); error; error = glGetError()) {
        ALOGI("after %s() glError (0x%x)\n", op, error);
        isError = true;
    }
    return isError;
}

GLuint createProgram(const char *pVertexSource, const char *pFragmentSource) {
    GLuint vertexShader = loadShader(GL_VERTEX_SHADER, pVertexSource);
    if (!vertexShader) {
        return 0;
    }

    GLuint pixelShader = loadShader(GL_FRAGMENT_SHADER, pFragmentSource);
    if (!pixelShader) {
        return 0;
    }

    GLuint program = glCreateProgram();
    if (program == 0) {
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

std::unique_ptr<Circle::ProgramState> Circle::sProgramState = nullptr;

bool Circle::initProgram()
{
    if (sProgramState == nullptr)
        sProgramState.reset(new Circle::ProgramState);
    return sProgramState->program != 0;

}

Circle::ProgramState::ProgramState() {
    using namespace CircleProgram;

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
    colorHandle = glGetUniformLocation(program, "vColor");
    checkGlError("glGetUniformLocation(vColor)");
}

Circle::Circle(const glm::vec2 &center, float radius, const glm::vec4 &color)
        : mCenter(center), mRadius(radius), mColor(color) {
    Renderer::getInstance()->run([this]() { prepare(); });
}

Circle::~Circle()
{
    cleanup();
}

void Circle::prepareInternal()
{
    if (!mPrepared && Circle::sProgramState) {
        bool error = false;

        ProgramState &state = *Circle::sProgramState;

        glUseProgram(state.program);
        error &= checkGlError("glUseProgram");

        glGenVertexArrays(1, &mVertexArray);
        error &= checkGlError("glGenVertexArrays");
        glGenBuffers(1, &mVertexBuffer);
        error &= checkGlError("glGenBuffers");

        // Make vertices
        std::vector<GLfloat> vertices;
        int vertexCount = 30;

        for (int i = 0; i < vertexCount; ++i) {
            float percent = (i / (float) (vertexCount));
            float rad = static_cast<float>(percent * 2 * M_PI);

            float outerX = 0.0f + 1.0f * std::cos(rad);
            float outerY = 0.0f + 1.0f * std::sin(rad);

            vertices.push_back(outerX);
            vertices.push_back(outerY);
        }

        glBindBuffer(GL_ARRAY_BUFFER, mVertexBuffer);
        error &= checkGlError("glBindBuffer");
        glBufferData(GL_ARRAY_BUFFER,
                     static_cast<GLsizeiptr>(vertices.size() * sizeof(GLfloat)), &vertices[0],
                     GL_DYNAMIC_DRAW);
        error &= checkGlError("glBufferData");

        glBindVertexArray(mVertexArray);
        error &= checkGlError("glBindVertexArray");
        glVertexAttribPointer(0, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(GLfloat), (GLvoid *) 0);
        error &= checkGlError("glVertexAttribPointer");

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        error &= checkGlError("glBindBuffer");
        glBindVertexArray(0);
        error &= checkGlError("glBindVertexArray");

        if (!error)
            mPrepared = true;
        else
            ALOGW("Failed to prepare circle");
    }
}

void Circle::prepare()
{
    Renderer::getInstance()->run([this]() {
        prepareInternal();
    });
}

void Circle::cleanup()
{
    if (mPrepared) {
        glDeleteVertexArrays(1, &mVertexArray);
    }
}

void Circle::draw(const glm::mat4 &projection, const glm::mat4 &initialModel)
{
    if (!isVisible())
        return;

    prepareInternal();

    if (!mPrepared || Circle::sProgramState == nullptr) {
        ALOGW("Circle is not prepared to draw");
        return;
    }

    ProgramState &state = *Circle::sProgramState;

    glUseProgram(state.program);
    checkGlError("glUseProgram");

    glm::mat4 model = glm::translate(initialModel, glm::vec3(mCenter, 0.0f));
    model = glm::scale(model, glm::vec3(mRadius, mRadius, 1.0f));

    glUniformMatrix4fv(state.modelHandle, 1, GL_FALSE, glm::value_ptr(model));
    checkGlError("glUniformMatrix4fv(model)");

    glUniformMatrix4fv(state.projectionHandle, 1, GL_FALSE, glm::value_ptr(projection));
    checkGlError("glUniformMatrix4fv(projection)");

    glUniform4fv(state.colorHandle, 1, glm::value_ptr(mColor));

    glBindVertexArray(mVertexArray);
    checkGlError("glBindVertexArray");

    glEnableVertexAttribArray(0);
    checkGlError("glEnableVertexAttribArray");

    glDrawArrays(GL_LINE_LOOP, 0, 30);
    checkGlError("glDrawArrays");

    glBindVertexArray(0);
    checkGlError("glBindVertexArray");

    glDisableVertexAttribArray(0);
    checkGlError("glDisableVertexAttribArray");
}

} // namespace samples
