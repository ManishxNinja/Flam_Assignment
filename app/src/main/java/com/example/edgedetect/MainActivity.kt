package com.example.edgedetect

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.opengl.GLSurfaceView
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import android.util.Log
import android.view.Surface
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import android.graphics.Bitmap

class MainActivity : AppCompatActivity() {

    external fun processFrame(inputAddr: Long, outputAddr: Long)

    private lateinit var cameraDevice: CameraDevice
    private lateinit var captureSession: CameraCaptureSession
    private lateinit var cameraManager: CameraManager
    private lateinit var glSurfaceView: GLSurfaceView
    private val cameraHandler = Handler(Looper.getMainLooper())

    companion object {
        init {
            System.loadLibrary("native-lib")
            OpenCVLoader.initDebug()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ✅ Initialize OpenGL SurfaceView
        glSurfaceView = findViewById(R.id.glSurfaceView)
        glSurfaceView.setEGLContextClientVersion(2)
        glSurfaceView.setRenderer(GLRenderer())

        // ✅ Initialize Camera Manager
        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager

        // ✅ Check camera permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1)
            return
        }

        // ✅ Open camera once ready
        openCamera()
    }

    private fun openCamera() {
        try {
            val cameraId = cameraManager.cameraIdList[0] // use back camera
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) return

            cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    startCameraPreview()
                }

                override fun onDisconnected(camera: CameraDevice) {
                    camera.close()
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    camera.close()
                    Log.e("Camera", "Error: $error")
                }
            }, cameraHandler)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startCameraPreview() {
        try {
            val surfaceTexture = SurfaceTexture(10)
            surfaceTexture.setDefaultBufferSize(640, 480)
            val surface = Surface(surfaceTexture)

            val captureRequestBuilder =
                cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                    addTarget(surface)
                }

            cameraDevice.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    captureSession = session
                    session.setRepeatingRequest(captureRequestBuilder.build(), null, cameraHandler)
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    Log.e("Camera", "Configuration failed")
                }
            }, cameraHandler)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 🧠 Optional: convert preview bitmap → OpenCV Mat → process → render
    private fun processCurrentFrame(bitmap: Bitmap) {
        val matInput = Mat(bitmap.height, bitmap.width, CvType.CV_8UC4)
        val matOutput = Mat(bitmap.height, bitmap.width, CvType.CV_8UC4)
        Utils.bitmapToMat(bitmap, matInput)
        processFrame(matInput.nativeObjAddr, matOutput.nativeObjAddr)
        Utils.matToBitmap(matOutput, bitmap)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            captureSession.close()
            cameraDevice.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
