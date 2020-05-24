//
// Created by crims on 2020-04-19.
//

#include "SpriteManager.h"
#include "Log.h"
#include "Renderer.h"

#define LOG_TAG "SpriteManager"

namespace Engine2D {

SpriteManager *SpriteManager::getInstance() {
    static std::unique_ptr <SpriteManager> sSpriteManager = std::make_unique<SpriteManager>(ConstructorTag{});
    return sSpriteManager.get();
}

int SpriteManager::add(const std::shared_ptr<Sprite>& sprite)
{
    std::lock_guard<std::mutex> _lock(mMutex);

    mSpriteMap[mNextId] = sprite;
    return mNextId++;
}

std::shared_ptr<Sprite> SpriteManager::get(int id)
{
    std::lock_guard<std::mutex> _lock(mMutex);

    auto iter = mSpriteMap.find(id);
    if (iter != mSpriteMap.end()) {
        return iter->second;
    }

    return nullptr;
}

void SpriteManager::remove(int id)
{
    Renderer::getInstance()->run([this, id] {
        std::lock_guard<std::mutex> _lock(mMutex);

        auto iter = mSpriteMap.find(id);
        if (iter != mSpriteMap.end()) {
            mSpriteMap.erase(iter);
        }
    });
}

bool SpriteManager::initPrograms()
{
    if (!Sprite::initProgram())
        return false;
    return true;
}

void SpriteManager::clear()
{
    std::lock_guard<std::mutex> _lock(mMutex);

    mSpriteMap.clear();
}

SpriteManager::SpriteList SpriteManager::getSpriteList()
{
    std::lock_guard<std::mutex> _lock(mMutex);

    SpriteManager::SpriteList list;
    list.reserve(mSpriteMap.size());

    std::transform(mSpriteMap.begin(), mSpriteMap.end(),back_inserter(list),
        [] (std::pair<int, std::shared_ptr<Sprite> > const & pair)
            { return pair.second; });

    std::sort(list.begin(), list.end(),
            [](std::shared_ptr<Sprite>& a, std::shared_ptr<Sprite>& b) -> bool
            {
                if (a->getLayer() < b->getLayer()) {
                    return true;
                }
                else if (a->getLayer() > b->getLayer()) {
                    return false;
                }
                else {
                    return a->getDepth() < b->getDepth();
                }
            } );

    return list;
}

}