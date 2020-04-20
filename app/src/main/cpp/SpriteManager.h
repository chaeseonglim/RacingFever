//
// Created by crims on 2020-04-19.
//

#ifndef RACINGFEVER_SPRITEMANAGER_H
#define RACINGFEVER_SPRITEMANAGER_H

#include <list>
#include <memory>
#include <mutex>
#include "Sprite.h"
#include "ResourceManager.h"

namespace Engine2D
{

class SpriteManager {
    // Prevent construct from outside
    struct ConstructorTag {
    };

public:
    explicit SpriteManager(ConstructorTag) { }

    static SpriteManager *getInstance();

    void init();
    void clear();

public:
    int add(const std::shared_ptr<Sprite>& sprite);
    std::shared_ptr<Sprite> get(int id);
    void remove(int id);

public:
    using SpriteList = std::list<std::shared_ptr<Sprite> >;
    using SpriteMap = std::unordered_map<int, std::pair<std::shared_ptr<Sprite>, SpriteList::iterator > >;

    const SpriteMap& getSpriteMap() const { return mSpriteMap; }
    const SpriteList& getSpriteList() const { return mSpriteList; }

    void lock()     { mMutex.lock(); }
    void unlock()   { mMutex.unlock(); }

private:
    SpriteMap mSpriteMap;
    SpriteList mSpriteList;
    int mNextId = 0;

    std::mutex mMutex;
};

}

#endif //RACINGFEVER_SPRITEMANAGER_H
