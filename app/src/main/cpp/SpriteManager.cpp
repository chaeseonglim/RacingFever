//
// Created by crims on 2020-04-19.
//

#include "SpriteManager.h"
#include "Log.h"

#define LOG_TAG "SpriteManager"

namespace Engine2D {

SpriteManager *SpriteManager::getInstance() {
    static std::unique_ptr <SpriteManager> sSpriteManager = std::make_unique<SpriteManager>(ConstructorTag{});
    return sSpriteManager.get();
}

int SpriteManager::add(const std::shared_ptr<Sprite>& sprite)
{
    std::lock_guard<std::mutex> _lock(mMutex);

    auto iter = mSpriteList.insert(mSpriteList.end(), sprite);
    mSpriteMap[mNextId] = std::make_pair(sprite, iter);

    return mNextId++;
}

std::shared_ptr<Sprite> SpriteManager::get(int id)
{
    std::lock_guard<std::mutex> _lock(mMutex);

    auto iter = mSpriteMap.find(id);
    if (iter != mSpriteMap.end()) {
        return iter->second.first;
    }

    return nullptr;
}

void SpriteManager::remove(int id)
{
    std::lock_guard<std::mutex> _lock(mMutex);

    auto iter = mSpriteMap.find(id);
    if (iter != mSpriteMap.end()) {
        mSpriteList.erase((iter->second).second);
        mSpriteMap.erase(iter);
    }
}

void SpriteManager::init()
{
    Sprite::init();
}

void SpriteManager::clear()
{
    std::lock_guard<std::mutex> _lock(mMutex);

    mSpriteList.clear();
    mSpriteMap.clear();
}

}