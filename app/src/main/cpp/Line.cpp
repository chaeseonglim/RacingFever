//
// Created by crims on 2020-05-11.
//

#include "Line.h"

#define LOG_TAG "Line"

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

namespace LineProgram {

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

std::unique_ptr<Line::ProgramState> Line::sProgramState = nullptr;

bool Line::initProgram()
{
    if (sProgramState == nullptr)
        sProgramState.reset(new Line::ProgramState);
    if (sProgramState->program == 0)
        return false;

    return true;
}

Line::ProgramState::ProgramState()
{
    using namespace LineProgram;

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

Line::Line(const glm::vec2 &begin, const glm::vec2 &end, const glm::vec4 &color)
        : mBegin(begin), mEnd(end), mColor(color)
{
    prepare();
    Renderer::getInstance()->run([this]() { prepare(); });
}

Line::~Line()
{
    cleanup();
}

void Line::prepareInternal()
{
    if (!mPrepared && Line::sProgramState) {
        bool error = false;

        ProgramState &state = *Line::sProgramState;

        glUseProgram(state.program);
        error &= checkGlError("glUseProgram");

        glGenVertexArrays(1, &mVertexArray);
        error &= checkGlError("glGenVertexArrays");
        glGenBuffers(1, &mVertexBuffer);
        error &= checkGlError("glGenBuffers");

        if (!error)
            mPrepared = true;
        else
            ALOGW("Failed to prepare line");
    }
}

void Line::prepare()
{
    Renderer::getInstance()->run([this]() {
        prepareInternal();
    });
}

void Line::cleanup()
{
    if (mPrepared) {
        Renderer::getInstance()->run([vertexArray = this->mVertexArray]() {
            glDeleteVertexArrays(1, &vertexArray);
        });
    }
}

void Line::draw(const glm::mat4 &projection, const glm::mat4 &initialModel)
{
    if (!isVisible())
        return;

    prepareInternal();

    if (!mPrepared || Line::sProgramState == nullptr) {
        ALOGW("Line is not prepared to draw");
        return;
    }

    ProgramState &state = *Line::sProgramState;

    glUseProgram(state.program);
    checkGlError("glUseProgram");

    // Make vertices
    float minX = std::min(mBegin.x, mEnd.x);
    float maxX = std::max(mBegin.x, mEnd.x);
    float minY = std::min(mBegin.y, mEnd.y);
    float maxY = std::max(mBegin.y, mEnd.y);
    if (minX == maxX)
        maxX += 1.0f;
    if (minY == maxY)
        maxY += 1.0f;
    float xRatio = 1.0f/(maxX - minX);
    float yRatio = 1.0f/(maxY - minY);

    GLfloat vertices[] =
        {
            (mBegin.x - minX) * xRatio, (mBegin.y - minY) * yRatio,
            (mEnd.x - minX) * xRatio, (mEnd.y - minY) * yRatio,
        };

    glBindBuffer(GL_ARRAY_BUFFER, mVertexBuffer);
    checkGlError("glBindBuffer");
    glBufferData(GL_ARRAY_BUFFER, sizeof(vertices), vertices, GL_DYNAMIC_DRAW);
    checkGlError("glBufferData");

    glBindVertexArray(mVertexArray);
    checkGlError("glBindVertexArray");

    glVertexAttribPointer(0, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(GLfloat), (GLvoid *) 0);
    checkGlError("glVertexAttribPointer");

    glEnableVertexAttribArray(0);
    checkGlError("glEnableVertexAttribArray");

    glm::mat4 model = glm::translate(initialModel, glm::vec3(minX, minY, 0.0f));
    float diffX = maxX - minX, diffY = maxY - minY;
    model = glm::scale(model, glm::vec3(diffX, diffY, 1.0f));

    glUniformMatrix4fv(state.modelHandle, 1, GL_FALSE, glm::value_ptr(model));
    checkGlError("glUniformMatrix4fv(model)");

    glUniformMatrix4fv(state.projectionHandle, 1, GL_FALSE, glm::value_ptr(projection));
    checkGlError("glUniformMatrix4fv(projection)");

    glUniform4fv(state.colorHandle, 1, glm::value_ptr(mColor));
    checkGlError("glUniform4fv");

    glDrawArrays(GL_LINES, 0, 2);
    checkGlError("glDrawArrays");

    glBindVertexArray(0);
    checkGlError("glBindVertexArray");

    glDisableVertexAttribArray(0);
    checkGlError("glDisableVertexAttribArray");
}

} // namespace samples
