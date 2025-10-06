cmake_minimum_required(VERSION 3.22.1)
project("edge-detection-rnd")

find_package(OpenCV REQUIRED)
include_directories(${OpenCV_INCLUDE_DIRS})

add_library(native-lib SHARED app/src/main/cpp/native-lib.cpp)
find_library(log-lib log)

target_link_libraries(native-lib ${log-lib} ${OpenCV_LIBS})
