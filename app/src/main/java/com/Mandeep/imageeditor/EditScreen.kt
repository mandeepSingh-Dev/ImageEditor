package com.Mandeep.imageeditor

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import com.Mandeep.imageeditor.databinding.ActivityEditScreenBinding

class EditScreen : AppCompatActivity()
{
    lateinit var binding: ActivityEditScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditScreenBinding.inflate(LayoutInflater.from(this))

        setContentView(binding.root)

        val savedUri = Uri.parse(intent?.getStringExtra("SAVED_URI"))
        binding.Imageee.setImageURI(savedUri)

        binding.crop.setOnClickListener {
            //call the standard crop action intent (the user device may not support it)
            //call the standard crop action intent (the user device may not support it)
            val cropIntent = Intent("com.android.camera.action.CROP")
            //indicate image type and Uri
            cropIntent.setDataAndType(savedUri, "image/*")
            //set crop properties
            cropIntent.putExtra("crop", "true")
            //indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1)
            cropIntent.putExtra("aspectY", 1)
            //indicate output X and Y
            cropIntent.putExtra("outputX", 256)
            cropIntent.putExtra("outputY", 256)
            //retrieve data on return
            cropIntent.putExtra("return-data", true)
            //start the activity - we handle returning in onActivityResult
            //start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, 45)

        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 45)
        {
            var bitmap: Bitmap = data?.extras?.getParcelable("data")!!
            Log.d("dfdbfjd",bitmap.toString())
          //  binding.Imageee

        }
    }
}