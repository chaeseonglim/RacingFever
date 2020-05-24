//
// Created by crims on 2020-04-19.
//

#include "ResourceManager.h"
#include "Log.h"

#define LOG_TAG "ResourceManager"

namespace Engine2D
{

ResourceManager *ResourceManager::getInstance() {
    static std::unique_ptr <ResourceManager> sResourceManager = std::make_unique<ResourceManager>(ConstructorTag{});
    return sResourceManager.get();
}

std::shared_ptr<Texture> ResourceManager::loadTexture(const GLchar *file, GLboolean alpha, std::string name)
{
    std::unique_lock<std::mutex> lock(mTextureLock);

    // If there is already registered texture, just use it
    auto iter = mTextures.find(name);
    if (iter != mTextures.end()) {
        iter->second.second++;
        return iter->second.first;
    }

    std::shared_ptr<Texture> texture = std::make_shared<Texture>(file, alpha);
    if (texture && texture->isLoaded()) {
        mTextures[name] = std::make_pair(texture, 1);
    }
    else {
        texture = nullptr;
    }

    return texture;
}

std::shared_ptr<Texture> ResourceManager::loadTexture(const unsigned char *memory, size_t memSize, GLboolean alpha, std::string name)
{
    std::unique_lock<std::mutex> lock(mTextureLock);

    // If there is already registered texture, just use it
    auto iter = mTextures.find(name);
    if (iter != mTextures.end()) {
        iter->second.second++;
        return iter->second.first;
    }

    std::shared_ptr<Texture> texture = std::make_shared<Texture>(memory, memSize, alpha);
    if (texture && texture->isLoaded()) {
        mTextures[name] = std::make_pair(texture, 1);
    }
    else {
        texture = nullptr;
    }

    return texture;
}

std::shared_ptr<Texture> ResourceManager::attachTexture(std::string name)
{
    std::unique_lock<std::mutex> lock(mTextureLock);

    auto iter = mTextures.find(name);
    if (iter == mTextures.end())
        return nullptr;

    iter->second.second++;
    return iter->second.first;
}

void ResourceManager::releaseTexture(std::string name)
{
    std::unique_lock<std::mutex> lock(mTextureLock);

    auto iter = mTextures.find(name);
    if (iter != mTextures.end()) {
        iter->second.second--;
        if (iter->second.second == 0)
            mTextures.erase(iter);
    }
}

std::shared_ptr<Texture> ResourceManager::getTexture(std::string name)
{
    std::unique_lock<std::mutex> lock(mTextureLock);

    auto iter = mTextures.find(name);
    if (iter == mTextures.end())
        return nullptr;

    return iter->second.first;
}

void ResourceManager::releaseAllTextures()
{
    std::unique_lock<std::mutex> lock(mTextureLock);

    mTextures.clear();
}

}