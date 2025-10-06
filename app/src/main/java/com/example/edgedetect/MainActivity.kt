package com.example.edgedetect

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Size
import android.view.Surface
import android.view.TextureView
import androidx.core.app.ActivityCompat
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat

class MainActivity : AppCompatActivity() {

    external fun processFrame(inputAddr: Long, outputAddr: Long)

    private lateinit var cameraDevice: CameraDevice
    private lateinit var cameraCaptureSession: CameraCaptureSession
    private lateinit var textureView: TextureView
    private lateinit var cameraManager: CameraManager
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

        textureView = findViewById(R.id.textureView)
        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1)
            return
        }

        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                openCamera()
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = true
        }
    }

    private fun openCamera() {
        try {
            val cameraId = cameraManager.cameraIdList[0]
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
                return
            }
            cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    startPreview()
                }

                override fun onDisconnected(camera: CameraDevice) {
                    camera.close()
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    camera.close()
                }
            }, cameraHandler)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startPreview() {
        val surfaceTexture = textureView.surfaceTexture!!
        val surface = Surface(surfaceTexture)

        val captureRequestBuilder =
            cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                addTarget(surface)
            }

        cameraDevice.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                cameraCaptureSession = session
                session.setRepeatingRequest(captureRequestBuilder.build(), null, cameraHandler)
            }

            override fun onConfigureFailed(session: CameraCaptureSession) {}
        }, cameraHandler)
    }

    // Optional: process frame using OpenCV (grayscale/edge)
    private fun processCurrentFrame() {
        val bitmap = textureView.bitmap ?: return
        val matInput = Mat(bitmap.height, bitmap.width, CvType.CV_8UC4)
        val matOutput = Mat(bitmap.height, bitmap.width, CvType.CV_8UC4)
        Utils.bitmapToMat(bitmap, matInput)

        processFrame(matInput.nativeObjAddr, matOutput.nativeObjAddr)

        Utils.matToBitmap(matOutput, bitmap)
        runOnUiThread {
            textureView.bitmap = bitmap
        }
    }
}
