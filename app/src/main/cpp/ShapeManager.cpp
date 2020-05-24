//
// Created by crims on 2020-04-19.
//

#include "ShapeManager.h"
#include "Log.h"
#include "Renderer.h"
#include "Line.h"
#include "Circle.h"

#define LOG_TAG "ShapeManager"

namespace Engine2D {

ShapeManager *ShapeManager::getInstance() {
    static std::unique_ptr <ShapeManager> sShapeManager = std::make_unique<ShapeManager>(ConstructorTag{});
    return sShapeManager.get();
}

int ShapeManager::add(const std::shared_ptr<Shape>& sprite)
{
    std::lock_guard<std::mutex> _lock(mMutex);

    mShapeMap[mNextId] = sprite;
    return mNextId++;
}

std::shared_ptr<Shape> ShapeManager::get(int id)
{
    std::lock_guard<std::mutex> _lock(mMutex);

    auto iter = mShapeMap.find(id);
    if (iter != mShapeMap.end()) {
        return iter->second;
    }

    return nullptr;
}

void ShapeManager::remove(int id)
{
    Renderer::getInstance()->run([this, id] {
        std::lock_guard<std::mutex> _lock(mMutex);

        auto iter = mShapeMap.find(id);
        if (iter != mShapeMap.end()) {
            mShapeMap.erase(iter);
        }
    });
}

bool ShapeManager::initPrograms()
{
    if (!Line::initProgram())
        return false;
    if (!Circle::initProgram())
        return false;

    return true;
}

void ShapeManager::releaseAll()
{
    std::lock_guard<std::mutex> _lock(mMutex);

    mShapeMap.clear();
}

ShapeManager::ShapeList ShapeManager::getShapeList()
{
    std::lock_guard<std::mutex> _lock(mMutex);

    ShapeManager::ShapeList list;
    list.reserve(mShapeMap.size());

    std::transform(mShapeMap.begin(), mShapeMap.end(),back_inserter(list),
        [] (std::pair<int, std::shared_ptr<Shape> > const & pair)
            { return pair.second; });

    std::sort(list.begin(), list.end(),
            [](std::shared_ptr<Shape>& a, std::shared_ptr<Shape>& b) -> bool
            {
                if (a->getLayer() < b->getLayer()) {
                    return true;
                }
                else if (a->getLayer() > b->getLayer()) {
                    return false;
                }
                else {
                    if (a->getDepth() < b->getDepth())
                        return true;
                    else
                        return false;
                }
            } );

    return list;
}

}