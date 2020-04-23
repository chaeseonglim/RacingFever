//
// Created by crims on 2020-04-23.
//

#ifndef RACINGFEVER_SIZE_H
#define RACINGFEVER_SIZE_H


namespace Engine2D {

class Size {

public:
    Size() = default;
    Size(int width, int height);

public:
    int getWidth() const {
        return width;
    }

    void setWidth(int width) {
        Size::width = width;
    }

    int getHeight() const {
        return height;
    }

    void setHeight(int height) {
        Size::height = height;
    }

private:
    int width = 0;
    int height = 0;
};

}

#endif //RACINGFEVER_SIZE_H
