//
// Created by crims on 2020-05-11.
//

#ifndef RACINGFEVER_LINE_H
#define RACINGFEVER_LINE_H

#include <glm/glm.hpp>
#include "Shape.h"

namespace Engine2D
{

class Line : public Shape
{
private:
    struct ProgramState {
        ProgramState();

        GLuint program;
        GLint modelHandle;
        GLint projectionHandle;
        GLint colorHandle;
    };
    static std::unique_ptr<ProgramState> sProgramState;

public:
    static bool initProgram();

public:
    Line() = default;
    Line(const glm::vec2 &begin, const glm::vec2 &end, const glm::vec4 &color);
    virtual ~Line();

    void draw(const glm::mat4 &projection, const glm::mat4 &initialModel) override ;

    const glm::vec2 &getBegin() const {
        return mBegin;
    }

    void setBegin(const glm::vec2 &mBegin) {
        Line::mBegin = mBegin;
    }

    const glm::vec2 &getEnd() const {
        return mEnd;
    }

    void setEnd(const glm::vec2 &mEnd) {
        Line::mEnd = mEnd;
    }

    const glm::vec4 &getColor() const {
        return mColor;
    }

    void setColor(const glm::vec4 &mColor) {
        Line::mColor = mColor;
    }

private:
    void prepare();
    void prepareInternal();
    void cleanup();

private:
    glm::vec2 mBegin;
    glm::vec2 mEnd;
    glm::vec4 mColor;
    GLuint mVertexArray;
    GLuint mVertexBuffer;
    bool mPrepared = false;
};

}

#endif //RACINGFEVER_LINE_H
