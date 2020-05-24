//
// Created by crims on 2020-04-19.
//

#ifndef RACINGFEVER_SHAPEMANAGER_H
#define RACINGFEVER_SHAPEMANAGER_H

#include <vector>
#include <memory>
#include <mutex>
#include <map>
#include <unordered_map>
#include "Shape.h"
#include "ResourceManager.h"

namespace Engine2D
{

class ShapeManager {
    // Prevent construct from outside
    struct ConstructorTag {
    };

public:
    explicit ShapeManager(ConstructorTag) { }

    static ShapeManager *getInstance();

    bool initPrograms();
    void releaseAll();

public:
    int add(const std::shared_ptr<Shape>& sprite);
    std::shared_ptr<Shape> get(int id);
    void remove(int id);

public:
    using ShapeList = std::vector<std::shared_ptr<Shape> >;
    using ShapeMap = std::unordered_map<int, std::shared_ptr<Shape> >;

    ShapeList getShapeList();

    const ShapeMap& getShapeMap() const { return mShapeMap; }

private:
    ShapeMap mShapeMap;

    int mNextId = 0;
    std::mutex mMutex;
};

}

#endif //RACINGFEVER_SHAPEMANAGER_H
