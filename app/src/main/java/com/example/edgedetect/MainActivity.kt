package com.example.edgedetect

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat

class MainActivity : AppCompatActivity() {

    external fun processFrame(inputAddr: Long, outputAddr: Long)

    companion object {
        init {
            System.loadLibrary("native-lib")
            OpenCVLoader.initDebug()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
