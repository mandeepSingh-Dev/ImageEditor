package com.Mandeep.imageeditor

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import com.Mandeep.imageeditor.databinding.ActivityEditScreenBinding
import java.lang.Exception


class EditScreen : AppCompatActivity()
{
    lateinit var binding: ActivityEditScreenBinding
    var savedUri:Uri?=null
    lateinit var bitmap:Bitmap
    val matrix:Matrix by lazy { Matrix() }
    var undoList:ArrayList<Bitmap>?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditScreenBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        undoList = ArrayList()

         savedUri = Uri.parse(intent?.getStringExtra("SAVED_URI"))
        binding.Imageee.setImageURI(savedUri)

        //crop image
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
           // startActivityForResult(cropIntent, 45)
            launcher.launch(cropIntent)

        }
          savedUri?.let {  bitmap = BitmapFactory.decodeStream(contentResolver?.openInputStream(it))}
          undoList?.add(bitmap)
        //rotate image
        binding.rotate.setOnClickListener {
            matrix.postRotate(90f)
            val bMapRotate = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            val bmd = BitmapDrawable(resources,bMapRotate)
            binding.Imageee.setImageBitmap(bMapRotate)
            binding.Imageee.setImageDrawable(bmd)
            undoList?.add(bMapRotate)
           // bitmap = bMapRotate
            //binding.Imageee.setImageDrawable(bmd)
        }

        //undo actions
        binding.undo.setOnClickListener {
            undo()
        }

        //save image
        binding.save.setOnClickListener {
            Log.d("dfjdfbjd43",undoList?.size.toString())

            val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val contentValues = ContentValues()
            contentValues.apply {
                put(MediaStore.Images.ImageColumns.MIME_TYPE,"image/png")
                put(MediaStore.Images.ImageColumns.DISPLAY_NAME,System.currentTimeMillis())
                put(MediaStore.Images.ImageColumns.TITLE,System.currentTimeMillis())
                put(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, Environment.DIRECTORY_PICTURES)
            }

            val insertedUri = contentResolver.insert(uri,contentValues)
            val outstream = contentResolver?.openOutputStream(insertedUri!!)

            //getting latest (last) bitmap from undoList
                val btmp = undoList?.get(undoList?.size?.minus(1)!!)
            Log.d("fkdfkdf",undoList?.size.toString())
                btmp?.compress(Bitmap.CompressFormat.JPEG,100,outstream)

        }
    }

    fun undo(){
        Log.d("dfdfjdbf33",undoList?.size.toString())
        if(undoList?.size!!>0)
        {
            try {
                val size = undoList?.size?.minus(1)
                val bitmapp = size?.minus(1)?.let { undoList?.get(it) }
                binding.Imageee.setImageBitmap(bitmapp)

                 bitmapp?.let { bitmap =it }

                undoList?.size?.minus(1)?.let { undoList?.removeAt(it) }
            }catch (e:Exception){}
        }
        //else if block for adding initial bitmap to undo List
        else if(undoList?.size == 0)
        {
            Log.d("dkfbdjf","ifnei")
            savedUri?.let { bitmap = BitmapFactory.decodeStream(contentResolver?.openInputStream(it))}

            bitmap.let{ undoList?.add(it)}
            //bitmap?.let { bitmap =it }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        undoList?.clear()
    }

    val activityResultContract = object:ActivityResultContract<Intent,Intent>(){
        override fun createIntent(context: Context, intent: Intent?): Intent {
            return intent!!
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Intent {
             return intent!!
        }
    }
    val launcher = registerForActivityResult(activityResultContract,object:ActivityResultCallback<Intent>{
        override fun onActivityResult(result: Intent?) {
            bitmap = result?.extras?.getParcelable("data")!!
            Log.d("dfdbfjd",bitmap.toString())
            binding.Imageee.setImageBitmap(bitmap)

            undoList?.add(bitmap)
        }
    })
}