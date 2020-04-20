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
    std::shared_ptr<Texture> texture = std::make_shared<Texture>(file, alpha);
    if (texture && texture->isLoaded()) {
        mTextures[name] = texture;
    }
    else {
        texture = nullptr;
    }

    return texture;
}

std::shared_ptr<Texture> ResourceManager::loadTexture(const unsigned char *memory, size_t memSize, GLboolean alpha, std::string name)
{
    std::shared_ptr<Texture> texture = std::make_shared<Texture>(memory, memSize, alpha);
    if (texture && texture->isLoaded()) {
        mTextures[name] = texture;
    }
    else {
        texture = nullptr;
    }

    return texture;
}

void ResourceManager::releaseTexture(std::string name)
{
    mTextures.erase(name);
}

std::shared_ptr<Texture> ResourceManager::getTexture(std::string name)
{
    auto iter = mTextures.find(name);
    if (iter == mTextures.end())
        return nullptr;

    return mTextures[name];
}

void ResourceManager::clear()
{
    mTextures.clear();
}

}