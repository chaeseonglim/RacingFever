//
// Created by crims on 2020-05-11.
//

#ifndef RACINGFEVER_CIRCLE_H
#define RACINGFEVER_CIRCLE_H

#include <glm/glm.hpp>
#include "Shape.h"

namespace Engine2D
{

class Circle : public Shape
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
    Circle() = default;
    Circle(const glm::vec2 &center, float radius, const glm::vec4 &color);
    virtual ~Circle();

    void draw(const glm::mat4 &projection, const glm::mat4 &initialModel) override ;

    const glm::vec2 &getCenter() const {
        return mCenter;
    }

    void setCenter(const glm::vec2 &mCenter) {
        Circle::mCenter = mCenter;
    }

    float getRadius() const {
        return mRadius;
    }

    void setRadius(float mRadius) {
        Circle::mRadius = mRadius;
    }

    const glm::vec4 &getColor() const {
        return mColor;
    }

    void setColor(const glm::vec4 &mColor) {
        Circle::mColor = mColor;
    }

private:
    void prepare();
    void prepareInternal();
    void cleanup();

private:
    glm::vec2 mCenter;
    float mRadius;
    glm::vec4 mColor;
    GLuint mVertexArray;
    GLuint mVertexBuffer;
    bool mPrepared = false;
};

}

#endif //RACINGFEVER_CIRCLE_H
