package com.checho.myfragment

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_2.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class Fragment2 : Fragment() {

    private val PERMISSION_REQUEST_CAMERA = 90
    private val PERMISSION_REQUEST_GALERY = 98
    val permissions = arrayOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private var photoFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addPhoto.setOnClickListener {
            requestMyPermission(PERMISSION_REQUEST_CAMERA)
        }

        addGallery.setOnClickListener {
            requestMyPermission(PERMISSION_REQUEST_GALERY)
        }
    }


    private fun requestMyPermission(requestCode: Int) {

        requestPermissions(permissions, requestCode)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_GALERY -> {
                if (grantResults.size == permissions.size && grantResults.count { it == PackageManager.PERMISSION_GRANTED } == permissions.size) {
                    startActivityForResult(Intent().apply {
                        type = "image/*"
                        action = Intent.ACTION_GET_CONTENT
                    }, PERMISSION_REQUEST_GALERY)
                } else {
                    Toast.makeText(context, "Permisos denegados", Toast.LENGTH_LONG).show()
                }
            }

            PERMISSION_REQUEST_CAMERA -> {
                if (grantResults.size == permissions.size && grantResults.count { it == PackageManager.PERMISSION_GRANTED } == permissions.size) {
                    OpenCamara()
                } else {
                    Toast.makeText(context, "Permisos denegados", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun OpenCamara() {
        val photoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (photoIntent.resolveActivity(activity!!.packageManager) != null) {
            try {
                photoFile = createPhotoFile()

            } catch (ioException: IOException) {
                throw ioException

            }
            if (photoFile != null) {
                val photoUri: Uri =
                    FileProvider.getUriForFile(context!!, "com.checho", photoFile!!)
                val resultIntent = activity!!.packageManager.queryIntentActivities(
                    photoIntent, PackageManager.MATCH_DEFAULT_ONLY
                )
                for (i in resultIntent) {
                    val packagesName = i.activityInfo.packageName
                    activity!!.grantUriPermission(
                        packagesName,
                        photoUri,
                        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }

                photoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(photoIntent, PERMISSION_REQUEST_CAMERA)

            }
        }
    }

    private fun createPhotoFile(): File? {
        val imageFileName: String = "myphoto${SimpleDateFormat("yyyMMdd_HHmmss").format(Date())}"
        val storageDir: File? = activity!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return if (storageDir?.exists() == false) {
            val result = storageDir.mkdir()
            null
        } else {
            File.createTempFile(imageFileName, ".jpg", storageDir)
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PERMISSION_REQUEST_GALERY -> {
                    data?.let {
                        Picasso.get().load(it.data).into(gallery)
                    }
                }
                PERMISSION_REQUEST_CAMERA -> {
                    val thread = Thread(Runnable {
                        val bitmap = BitmapFactory.decodeFile(photoFile!!.absolutePath)
                        val bitmapRotated = rotateImage(bitmap)
                        val result = resizeBitmap(bitmapRotated, 400, 400)
                        showBitmap(result)
                    })
                    thread.start()
                }

            }
        }
    }

    private fun showBitmap(bitmap: Bitmap) {
        activity!!.runOnUiThread {
            camera.setImageBitmap(bitmap)
        }

    }

    private fun rotateImage(bitmap: Bitmap): Bitmap {

        val bitmapWidth = bitmap.width
        val bitmapHeight = bitmap.height
        val matrix = Matrix()
        matrix.postRotate(90f)
        val scaledBitmap = Bitmap.createScaledBitmap(
            bitmap,
            bitmapWidth,
            bitmapHeight,
            true
        )
        return Bitmap.createBitmap(
            scaledBitmap,
            0,
            0,
            scaledBitmap.width,
            scaledBitmap.height,
            matrix,
            true
        )
    }

    fun resizeBitmap(bitmap: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        val scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);

        val ratioX = newWidth / (bitmap.getWidth()).toFloat()
        val ratioY = newHeight / (bitmap.getHeight()).toFloat()
        val middleX = newWidth / 2.0f;
        val middleY = newHeight / 2.0f;

        val scaleMatrix = Matrix()
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        val canvas = Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(
            bitmap,
            middleX - bitmap.getWidth() / 2,
            middleY - bitmap.getHeight() / 2,
            Paint(Paint.FILTER_BITMAP_FLAG)
        );

        return scaledBitmap;
    }


}