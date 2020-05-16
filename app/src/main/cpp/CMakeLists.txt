# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# ============== Games SDK

# This uses Swappy as a static library from the Game SDK package. Make sure the Android
# SDK and NDK versions that you are using are supported by the Game SDK.
include("gamesdk/samples/gamesdk.cmake")
add_gamesdk_target(PACKAGE_DIR "gamesdk" BUILD_TYPE "Release")

# =============== SOIL

add_library( # Sets the name of the library.
        SOIL

        # Sets the library as a static library.
        STATIC

        # Provides a relative path to your source file(s).
        soil/src/image_DXT.c
        soil/src/image_helper.c
        soil/src/SOIL.c
        soil/src/stb_image_aug.c
        )

include_directories(
        soil/src
)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        SOIL
        EGL
        GLESv3 )

# ============== Bouncy Ball

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        Engine2D

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        Engine2D.cpp
        Renderer.cpp
        Thread.cpp
        Texture.cpp
        Sprite.cpp
        SpriteManager.cpp
        ShapeManager.cpp
        Line.cpp
        Circle.cpp
        ResourceManager.cpp
        Rect.cpp
        Size.cpp
        Utils.cpp
    )

include_directories(
        gamesdk/include
        glm
    )

target_compile_options(
        Engine2D
        PUBLIC
        -Wall
)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        Engine2D

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}

        android
        EGL
        GLESv2
        GLESv3
        gamesdk
        SOIL )