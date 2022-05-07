package com.example.storyapp

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewPropertyAnimator
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.storyapp.databinding.ActivitySplashBinding
import com.example.storyapp.viewmodel.LoginViewModel
import com.example.storyapp.viewmodel.ViewModelFactory

class SplashActivity : AppCompatActivity() {
    private var startSplash: ViewPropertyAnimator? = null
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val img = findViewById<TextView>(R.id.tes)
        val pref = MyPreference.getInstance(dataStore)
        val loginViewModel =
            ViewModelProvider(this, ViewModelFactory(pref))[LoginViewModel::class.java]

        loginViewModel.getLoginState().observe(this) {
            ObjectAnimator.ofFloat(binding.flatIllus, View.ALPHA, 1f).apply {
                duration = 1000
                start()
            }

            startSplash = img.animate().setDuration(splashDelay).alpha(1f).withEndAction {
                if (it) {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                } else {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }

        }

    }

    override fun onDestroy() {
        startSplash?.cancel()
        super.onDestroy()
    }

    companion object {
        private var splashDelay: Long = 2_500L
    }

}