package com.example.storyapp

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.storyapp.databinding.ActivityMainBinding
import com.example.storyapp.dataclass.RequestLogin
import com.example.storyapp.viewmodel.LoginViewModel
import com.example.storyapp.viewmodel.MainViewModel
import com.example.storyapp.viewmodel.ViewModelFactory

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setAction()
        playAnimation()

        val pref = MyPreference.getInstance(dataStore)
        val loginViewModel =
            ViewModelProvider(this, ViewModelFactory(pref))[LoginViewModel::class.java]


        loginViewModel.getLoginState().observe(this) { state ->
            if (state) {
                val intent = Intent(this@MainActivity, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        mainViewModel.message.observe(this) {
            val usr = mainViewModel.userlogin.value
            checkResponseeLogin(it, usr?.loginResult?.token, mainViewModel.isError, loginViewModel)
        }

        mainViewModel.isLoading.observe(this) {
            showLoading(it)
        }


    }

    private fun setAction() {
        binding.btLogin.setOnClickListener {
            binding.tiEmail.clearFocus()
            binding.tiPass.clearFocus()

            if (isDataValid()) {
                val user = RequestLogin(
                    binding.tiEmail.text.toString().trim(),
                    binding.tiPass.text.toString().trim()
                )
                mainViewModel.getResponseLogin(user)
            }

        }


        binding.signIn.setOnClickListener {
            val intent = Intent(this, RegisActivity::class.java)
            startActivity(intent)
        }

        binding.seePassword.setOnClickListener {
            if (binding.seePassword.isChecked) {
                binding.tiPass.transformationMethod = HideReturnsTransformationMethod.getInstance()
            } else {
                binding.tiPass.transformationMethod = PasswordTransformationMethod.getInstance()
            }
        }

    }


    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.flatIllus, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val title = ObjectAnimator.ofFloat(binding.welcome, View.ALPHA, 1f).setDuration(500)
        val login = ObjectAnimator.ofFloat(binding.tvLogin, View.ALPHA, 1f).setDuration(500)
        val email =
            ObjectAnimator.ofFloat(binding.emailInputLayout, View.ALPHA, 1f).setDuration(500)
        val pass = ObjectAnimator.ofFloat(binding.passInputLayout, View.ALPHA, 1f).setDuration(500)
        val btLogin = ObjectAnimator.ofFloat(binding.btLogin, View.ALPHA, 1f).setDuration(500)
        val btSignIn = ObjectAnimator.ofFloat(binding.signIn, View.ALPHA, 1f).setDuration(500)
        val checkBox = ObjectAnimator.ofFloat(binding.seePassword, View.ALPHA, 1f).setDuration(500)
        val together = AnimatorSet().apply {
            playTogether(login, email, pass, checkBox)
        }
        val together2 = AnimatorSet().apply {
            playTogether(btLogin, btSignIn)
        }


        AnimatorSet().apply {
            playSequentially(title, together, together2)
            start()
        }

    }

    private fun isDataValid(): Boolean {
        return binding.tiEmail.isEmailValid && binding.tiPass.isPassValid
    }

    private fun checkResponseeLogin(
        msg: String,
        tkn: String?,
        isError: Boolean,
        vm: LoginViewModel
    ) {
        if (!isError) {
            Toast.makeText(
                this,
                "${getString(R.string.success_login)} $msg",
                Toast.LENGTH_LONG
            ).show()
            vm.saveLoginState(true)
            if (tkn != null) vm.saveToken(tkn)
            vm.saveName(mainViewModel.userlogin.value?.loginResult?.name.toString())
        } else {
            when (msg) {
                "Unauthorized" -> {
                    Toast.makeText(this, getString(R.string.unauthorized), Toast.LENGTH_SHORT)
                        .show()
                    binding.tiEmail.apply {
                        setText("")
                        requestFocus()
                    }
                    binding.tiPass.setText("")

                }
                "timeout" -> {
                    Toast.makeText(this, getString(R.string.timeout), Toast.LENGTH_SHORT)
                        .show()
                }
                else -> {
                    Toast.makeText(
                        this,
                        "${getString(R.string.error_message)} $msg",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}