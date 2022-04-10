package com.Mandeep.imageeditor

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.Mandeep.imageeditor.databinding.ActivityMainBinding
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ExecutorService


class HomeScreen : AppCompatActivity()
{
    lateinit var binding:ActivityMainBinding

    private val PERMISSION_REQUEST_CAMERA = 101
    private var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>? = null
    private var cameraProvider:ProcessCameraProvider?=null
    lateinit var camera: Camera
    lateinit var imageCapture:ImageCapture
    lateinit var cameraExecutable: ExecutorService
    lateinit var savedUri:Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        supportActionBar?.hide()

       val byteArray =  intent.getByteArrayExtra("Bitmap")
        if(byteArray!=null) {
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            //  val bitmap = intent.getParcelableExtra<Bitmap>("Bitmap")
            if (bitmap != null) {
                binding.cardView.visibility = View.VISIBLE
                binding.imagePreview.setImageBitmap(bitmap)
            }
        }

        binding.takeSelfieButton.setOnClickListener {
            binding.captureButton.isEnabled = true
            binding.previewParentLayout.animation = AnimationUtils.loadAnimation(this,R.anim.camera_appear)
            binding.previewParentLayout.visibility = View.VISIBLE

            binding.buttonsconstraintLayout.animation = AnimationUtils.loadAnimation(this,R.anim.alpha_disappear)
            binding.buttonsconstraintLayout.visibility = View.GONE

            cameraProviderFuture = ProcessCameraProvider.getInstance(this)
            requestCamera()
        }

        binding.captureButton.setOnClickListener {
            binding.progressbar.visibility = View.VISIBLE
           // binding.captureButton.isEnabled = true
           try {
               takePhoto()
           }catch(e:Exception){}
        }

        binding.uploadImageButton.setOnClickListener {
            launcher.launch("image/*")
        }

        binding.backbutton1.setOnClickListener {
            if(binding.previewParentLayout.visibility == View.VISIBLE) {

                binding.captureButton.isEnabled = false
                binding.previewParentLayout.animation = AnimationUtils.loadAnimation(this,R.anim.cam_disappear)
                binding.previewParentLayout.visibility = View.GONE

                binding.buttonsconstraintLayout.animation = AnimationUtils.loadAnimation(this,R.anim.alpha_appear)
                binding.buttonsconstraintLayout.visibility = View.VISIBLE

                cameraProviderFuture?.cancel(true)
                cameraProvider?.unbindAll()
                cameraProvider = null
            }
            else{
                onBackPressed()
            }
        }
    } //End.Of.onCreate

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
                 Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show() }
        }
    }//end of onRequetPermiss...

    private fun startCamera()
    {
        Log.d("48gh4g","camrea is stared")
        cameraProviderFuture?.addListener({
            try {
                cameraProvider = cameraProviderFuture?.get()

                cameraProvider?.let { bindCameraPreview(it) }

            } catch (e: Exception) { }
        }, ContextCompat.getMainExecutor(this))
    }//End of startCamera()

    private fun bindCameraPreview(cameraProvider: ProcessCameraProvider)
    {
        Log.d("48gh4g","binding is started")
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

    private fun takePhoto() {
        binding.captureButton.isEnabled = false

        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val contentValues = ContentValues()
        contentValues.apply {
            put(MediaStore.Images.ImageColumns.MIME_TYPE,"image/png")
            put(MediaStore.Images.ImageColumns.DISPLAY_NAME,System.currentTimeMillis())
            put(MediaStore.Images.ImageColumns.TITLE,System.currentTimeMillis())
            put(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,Environment.DIRECTORY_PICTURES)
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(contentResolver,uri,contentValues).build()

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    binding.captureButton.isEnabled = true
                }
                override fun onImageSaved(output: ImageCapture.OutputFileResults)
                {
                    binding.captureButton.isEnabled = false
                    binding.progressbar.visibility = View.GONE

                    output.savedUri?.apply { savedUri = this }
                    val intent = Intent(this@HomeScreen,EditScreen::class.java)
                    intent.putExtra("SAVED_URI",savedUri.toString())
                    startActivity(intent)
                   }
            })

    }//E.O. takephoto()

    @SuppressLint("RestrictedApi")
    override fun onBackPressed() {
        if(binding.previewParentLayout.visibility == View.VISIBLE) {

            binding.captureButton.isEnabled = false
            binding.previewParentLayout.animation = AnimationUtils.loadAnimation(this,R.anim.cam_disappear)
            binding.previewParentLayout.visibility = View.GONE

            binding.buttonsconstraintLayout.animation = AnimationUtils.loadAnimation(this,R.anim.alpha_appear)
            binding.buttonsconstraintLayout.visibility = View.VISIBLE

            cameraProviderFuture?.cancel(true)
            cameraProvider?.unbindAll()
            cameraProvider = null
        }
        else{
            super.onBackPressed()
        }
    }

   private val launcher = registerForActivityResult(ActivityResultContracts.GetContent(),
       ActivityResultCallback {
           if(it!=null) {
               savedUri = it
               val intent = Intent(this@HomeScreen,EditScreen::class.java)
               intent.putExtra("SAVED_URI",savedUri.toString())
               startActivity(intent)
           }
           else{
               Log.d("difnd","dk")
           }
       })

    override fun onResume() {
        super.onResume()
        binding.captureButton.isEnabled = true
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}