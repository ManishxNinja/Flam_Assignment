#include <jni.h>
#include <opencv2/opencv.hpp>
using namespace cv;

extern "C"
JNIEXPORT void JNICALL
Java_com_example_edgedetect_MainActivity_processFrame(JNIEnv*, jobject, jlong addrInput, jlong addrOutput) {
    Mat& input = *(Mat*)addrInput;
    Mat& output = *(Mat*)addrOutput;
    cvtColor(input, output, COLOR_RGBA2GRAY);
    Canny(output, output, 50, 150);
}
