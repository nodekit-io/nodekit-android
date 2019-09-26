cmake_minimum_required(VERSION 3.4.1)
include_directories(../cpp)


set(CURRENT_DIR ${CMAKE_SOURCE_DIR})
add_library(jsc SHARED IMPORTED)
set_target_properties( # Specifies the target library.
        jsc PROPERTIES
        IMPORTED_LOCATION "${CURRENT_DIR}/../jniLibs/${ANDROID_ABI}/libjsc.so")

add_library( # Sets the name of the library.
        native-lib

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        native-lib.cpp
        JSContext.cpp
        JSValue.cpp
        JSFunction.cpp
        JSObject.cpp
        JSInstance.cpp
        JSString.cpp
        )


#find_library( # Sets the name of the path variable.
#              log-lib
#
#              # Specifies the name of the NDK library that
#              # you want CMake to locate.
#              log )
#

target_link_libraries(native-lib jsc ${log-lib})
