package com.example.storyapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.example.storyapp.databinding.ActivityAddBinding
import com.example.storyapp.viewmodel.AddViewModel
import com.example.storyapp.viewmodel.LoginViewModel
import com.example.storyapp.viewmodel.ViewModelFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class AddActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddBinding
    private var getFile: File? = null
    private lateinit var token: String
    private val addViewModel: AddViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setActionBar()
        val pref = MyPreference.getInstance(dataStore)
        val loginViewModel =
            ViewModelProvider(this, ViewModelFactory(pref))[LoginViewModel::class.java]


        loginViewModel.getToken().observe(this) {
            token = it
        }

        loginViewModel.getName().observe(this) {
            binding.tvName.text = StringBuilder(getString(R.string.post_as)).append(" ").append(it)
        }

        addViewModel.message.observe(this) {
            showToast(it)
        }

        addViewModel.isLoading.observe(this) {
            showLoading(it)
        }


        binding.imgPhoto.setOnClickListener {
            select()
        }

        binding.btUpload.setOnClickListener {
            uploadImage()

        }


    }

    private fun uriToFile(selectedImg: Uri, context: Context): File {
        val contentResolver: ContentResolver = context.contentResolver
        val myFile = createCustomTempFile(context)

        val inputStream = contentResolver.openInputStream(selectedImg) as InputStream
        val outputStream: OutputStream = FileOutputStream(myFile)
        val buf = ByteArray(1024)
        var len: Int
        while (inputStream.read(buf).also { len = it } > 0) outputStream.write(buf, 0, len)
        outputStream.close()
        inputStream.close()

        return myFile
    }


    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri
            val myFile = uriToFile(selectedImg, this@AddActivity)
            getFile = myFile
            binding.imgPhoto.setImageURI(selectedImg)
            binding.tvDes.requestFocus()
        }
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, getString(R.string.choose_picture))
        launcherIntentGallery.launch(chooser)
    }

    private var anyPhoto = false
    private lateinit var currentPhotoPath: String
    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val myFile = File(currentPhotoPath)
            getFile = myFile
            val result = BitmapFactory.decodeFile(myFile.path)
            anyPhoto = true
            binding.imgPhoto.setImageBitmap(result)
            binding.tvDes.requestFocus()
        }
    }
    private val timeStamp: String = SimpleDateFormat(
        FILENAME_FORMAT,
        Locale.US
    ).format(System.currentTimeMillis())

    private fun createCustomTempFile(context: Context): File {
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(timeStamp, ".jpg", storageDir)
    }

    private fun startTakePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.resolveActivity(packageManager)
        createCustomTempFile(application).also {
            val photoURI: Uri = FileProvider.getUriForFile(
                this@AddActivity,
                getString(R.string.package_name),
                it
            )
            currentPhotoPath = it.absolutePath
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            launcherIntentCamera.launch(intent)
        }
    }

    private fun uploadImage() {
        val des = binding.tvDes.text.toString()
        when {
            getFile == null -> {
                Toast.makeText(
                    this@AddActivity,
                    getString(R.string.input_picture),
                    Toast.LENGTH_SHORT
                ).show()


            }
            des.trim().isEmpty() -> {
                Toast.makeText(
                    this@AddActivity,
                    getString(R.string.input_des),
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {
                val file = getFile as File
                val description = des.toRequestBody("text/plain".toMediaType())
                val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                    "photo",
                    file.name,
                    requestImageFile
                )
                addViewModel.upload(imageMultipart, description, token)

            }
        }


    }

    private fun showToast(msg: String) {
        Toast.makeText(
            this@AddActivity,
            StringBuilder(getString(R.string.message)).append(msg),
            Toast.LENGTH_SHORT
        ).show()

        if (msg == "Story created successfully") {
            startActivity(Intent(this@AddActivity, HomeActivity::class.java))
            finish()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }


    private fun select() {
        val items = arrayOf<CharSequence>(

            getString(R.string.from_galeri),
            getString(R.string.take_picture),
            getString(R.string.cancel)
        )

        val title = TextView(this)
        title.text = getString(R.string.select_photo)
        title.gravity = Gravity.CENTER
        title.setPadding(10, 15, 15, 10)
        title.setTextColor(resources.getColor(R.color.dark_blue, theme))
        title.textSize = 22f
        val builder = AlertDialog.Builder(
            this
        )
        builder.setCustomTitle(title)
        builder.setItems(items) { dialog, item ->
            when {
                items[item] == getString(R.string.from_galeri) -> {
                    startGallery()

                }
                items[item] == getString(R.string.take_picture) -> {
                    startTakePhoto()

                }
                items[item] == getString(R.string.cancel) -> {
                    dialog.dismiss()
                }
            }
        }
        builder.show()
    }


    @SuppressLint("RestrictedApi")
    private fun setActionBar() {
        val actionBar = supportActionBar
        actionBar?.title = getString(R.string.share_story)
        actionBar?.setDefaultDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val FILENAME_FORMAT = "MMddyyyy"
    }
}