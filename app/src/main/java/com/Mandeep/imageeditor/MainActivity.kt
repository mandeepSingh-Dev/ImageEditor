package com.Mandeep.imageeditor

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.Mandeep.imageeditor.databinding.ActivityMainBinding
import com.google.common.util.concurrent.ListenableFuture
import com.theartofdev.edmodo.cropper.CropImage
import java.io.OutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity()
{
    lateinit var binding:ActivityMainBinding

    val PERMISSION_REQUEST_CAMERA = 101
    private var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>? = null
    var cameraProvider:ProcessCameraProvider?=null
    lateinit var camera: Camera
    lateinit var imageCapture:ImageCapture
    lateinit var cameraExecutable: ExecutorService
    lateinit var savedUri:Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        cameraExecutable = Executors.newSingleThreadExecutor()

        binding.takeSelfieButton.setOnClickListener {

            binding.buttonsconstraintLayout.visibility = View.GONE
            binding.previewParentLayout.visibility = View.VISIBLE

            cameraProviderFuture = ProcessCameraProvider.getInstance(this)
            requestCamera()
        }
        binding.captureButton.setOnClickListener {
            takePhoto()
        }

    }
    private fun requestCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Log.d("fkdnfdk","granted")
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), PERMISSION_REQUEST_CAMERA)
        }
    }//end of requestCamera
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                 Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }//end of onRequetPermiss...

    private fun startCamera()
    {
        Log.d("48gh4g","camrea is stared")
        cameraProviderFuture?.addListener({
            try {
                cameraProvider = cameraProviderFuture?.get()

                cameraProvider?.let {
                    bindCameraPreview(it)
                }

            } catch (e: Exception) {
            } catch (e: InterruptedException) {
            }
        }, ContextCompat.getMainExecutor(this))

    }
    fun bindCameraPreview(cameraProvider: ProcessCameraProvider)
    {
        Log.d("48gh4g","binding is stared")

        //  binding?.activityMainPreviewView?.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        binding.activityMainPreviewView.implementationMode = PreviewView.ImplementationMode.PERFORMANCE

        val preview = Preview.Builder().build()

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
            .build()

        preview.setSurfaceProvider(binding.activityMainPreviewView.surfaceProvider)

         imageCapture = ImageCapture.Builder().build()
        try {
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(this, cameraSelector, imageCapture, preview)
        }catch (e:java.lang.Exception){}
    }


   /* fun buildImageCaptureUseCase(): ImageCapture {
        return ImageCapture.Builder()
            .setTargetAspectRatio(aspectRatio)
            .setTargetRotation(rotation)
            .setTargetResolution(resolution)
            .setFlashMode(flashMode)
            .setCaptureMode(captureMode)
            .build()
    }*/
    override fun onResume() {
    super.onResume()
  /*  if(binding.previewParentLayout.visibility == View.VISIBLE) {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        requestCamera()
    }*/
}

    fun getOutputStream(): OutputStream {
        val uri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        val contentValues = ContentValues()
        contentValues.let {
            it.put(MediaStore.Images.ImageColumns.MIME_TYPE,"image/png")
            it.put(MediaStore.Images.ImageColumns.DISPLAY_NAME,System.currentTimeMillis())
            it.put(MediaStore.Images.ImageColumns.TITLE,System.currentTimeMillis())
            it.put(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,Environment.DIRECTORY_PICTURES)
        }
        val insertedUri = contentResolver.insert(uri,contentValues)

        val outputStream = contentResolver?.openOutputStream(insertedUri!!)

        Log.d("3fhyyyyyy3ign", "SAVED IMAGE"+insertedUri)

        return outputStream!!
    }

    private fun takePhoto() {

        val uri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        val contentValues = ContentValues()
        contentValues.apply {
            put(MediaStore.Images.ImageColumns.MIME_TYPE,"image/png")
            put(MediaStore.Images.ImageColumns.DISPLAY_NAME,System.currentTimeMillis())
            put(MediaStore.Images.ImageColumns.TITLE,System.currentTimeMillis())
            put(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,Environment.DIRECTORY_PICTURES)
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(contentResolver,uri,contentValues).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback
            {
                override fun onError(exc: ImageCaptureException) {}
                override fun onImageSaved(output: ImageCapture.OutputFileResults)
                {
                output.savedUri?.apply { savedUri = this }
                    val intent = Intent(this@MainActivity,EditScreen::class.java)
                    intent.putExtra("SAVED_URI",savedUri.toString())
                    startActivity(intent)

                   // CropImage.activity(savedUri).start(this@MainActivity)
                   }
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutable.shutdown()
    }

    @SuppressLint("RestrictedApi")
    override fun onBackPressed() {
        if(binding.previewParentLayout.visibility == View.VISIBLE) {
            binding.previewParentLayout.visibility = View.GONE
            binding.buttonsconstraintLayout.visibility = View.VISIBLE

            cameraProviderFuture?.cancel(true)
            cameraProvider?.unbindAll()
            cameraProvider = null
        }
        else{
            super.onBackPressed()
        }
    }

}