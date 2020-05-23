//
// Created by crims on 2020-04-19.
//

#ifndef RACINGFEVER_TEXTURE_H
#define RACINGFEVER_TEXTURE_H

#include <GLES3/gl3.h>

namespace Engine2D {

class Texture
{
public:
    Texture(const GLchar *file, bool alpha);
    Texture(const unsigned char *memory, size_t memSize, bool alpha);
    ~Texture();

    void prepare(unsigned char* data);
    void cleanup();
    void bind() const;

    GLuint id() const { return mID; }
    bool isLoaded() const { return mLoaded; }

    GLuint getWidth() const {
        return mWidth;
    }

    GLuint getHeight() const {
        return mHeight;
    }

private:
    void loadFromFile(const GLchar *file, GLboolean alpha);
    void loadFromMemory(const unsigned char *memory, size_t memSize, GLboolean alpha);

private:
    // Texture ID
    GLuint mID;
    // Texture image dimensions
    GLuint mWidth, mHeight; // Width and height of loaded image in pixels
    bool mAlpha; // Alphaness of image
    // Texture Format
    GLuint mInternalFormat = GL_RGB; // Format of texture object
    GLuint mImageFormat = GL_RGB; // Format of loaded image
    // Texture configuration
    GLuint mWrapS = GL_CLAMP_TO_EDGE; // Wrapping mode on S axis
    GLuint mWrapT = GL_CLAMP_TO_EDGE; // Wrapping mode on T axis
    GLuint mFilterMin = GL_LINEAR; // Filtering mode if texture pixels < screen pixels
    GLuint mFilterMax = GL_LINEAR; // Filtering mode if texture pixels > screen pixels
    bool mLoaded = false;
    bool mPrepared = false;
};

}

#endif //RACINGFEVER_TEXTURE_H
