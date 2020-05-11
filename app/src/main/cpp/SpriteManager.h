//
// Created by crims on 2020-04-19.
//

#ifndef RACINGFEVER_SPRITEMANAGER_H
#define RACINGFEVER_SPRITEMANAGER_H

#include <vector>
#include <memory>
#include <mutex>
#include <map>
#include <unordered_map>
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

    bool initPrograms();
    void clear();

public:
    int add(const std::shared_ptr<Sprite>& sprite);
    std::shared_ptr<Sprite> get(int id);
    void remove(int id);

public:
    using SpriteList = std::vector<std::shared_ptr<Sprite> >;
    using SpriteMap = std::unordered_map<int, std::shared_ptr<Sprite> >;

    SpriteList getSpriteList();

    const SpriteMap& getSpriteMap() const { return mSpriteMap; }

private:
    SpriteMap mSpriteMap;

    int mNextId = 0;
    std::mutex mMutex;
};

}

#endif //RACINGFEVER_SPRITEMANAGER_H
