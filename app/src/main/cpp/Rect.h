//
// Created by crims on 2020-04-23.
//

#ifndef RACINGFEVER_RECT_H
#define RACINGFEVER_RECT_H


namespace Engine2D {

class Rect {

public:
    Rect() = default;
    Rect(int x, int y, int width, int height);

public:
    inline int getX() const {
        return x;
    }

    inline void setX(int x) {
        Rect::x = x;
    }

    inline int getY() const {
        return y;
    }

    inline void setY(int y) {
        Rect::y = y;
    }

    inline int getWidth() const {
        return width;
    }

    inline void setWidth(int width) {
        Rect::width = width;
    }

    inline int getHeight() const {
        return height;
    }

    inline void setHeight(int height) {
        Rect::height = height;
    }

private:
    int x = 0;
    int y = 0;
    int width = 0;
    int height = 0;
};

}

#endif //RACINGFEVER_RECT_H
