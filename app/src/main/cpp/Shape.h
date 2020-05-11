//
// Created by crims on 2020-05-11.
//

#ifndef RACINGFEVER_SHAPE_H
#define RACINGFEVER_SHAPE_H

#include <GLES3/gl3.h>
#include <glm/glm.hpp>
#include <memory>

namespace Engine2D {

class Shape {
public:
    Shape() = default;
    virtual ~Shape() {}

    virtual void draw(const glm::mat4 &projection, const glm::mat4 &initialModel) = 0;

    void show() { mVisible = true; }
    void hide() { mVisible = false;}
    void setVisible(bool visible) { mVisible = visible; }
    bool isVisible() const { return mVisible; }

    const int getLayer() const { return mLayer; }
    void setLayer(int layer) { mLayer = layer; }

    const GLfloat getDepth() const { return mDepth; }
    void setDepth(GLfloat depth) { mDepth = depth; }

private:
    int mLayer = 0;
    GLfloat mDepth = 0.0f;
    bool mVisible = false;
};

}

#endif //RACINGFEVER_SHAPE_H
