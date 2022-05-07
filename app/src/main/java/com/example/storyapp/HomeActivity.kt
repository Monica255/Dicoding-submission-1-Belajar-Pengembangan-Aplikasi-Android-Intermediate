package com.example.storyapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storyapp.*
import com.example.storyapp.databinding.ActivityHomeBinding
import com.example.storyapp.dataclass.ListStory
import com.example.storyapp.viewmodel.HomeViewModel
import com.example.storyapp.viewmodel.LoginViewModel
import com.example.storyapp.viewmodel.ViewModelFactory


class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var token: String
    private val homeViewModel: HomeViewModel by viewModels()
    private var isFinished = false


    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setAction()

        val layoutManager = LinearLayoutManager(this)
        binding.rvStories.layoutManager = layoutManager
        val itemDecoration = DividerItemDecoration(this, layoutManager.orientation)
        binding.rvStories.addItemDecoration(itemDecoration)

        val pref = MyPreference.getInstance(dataStore)
        val loginViewModel =
            ViewModelProvider(this, ViewModelFactory(pref))[LoginViewModel::class.java]



        loginViewModel.getToken().observe(this) {
            token = it
            homeViewModel.getStories(token)
        }

        homeViewModel.message.observe(this) {
            setUserData(homeViewModel.storiess)
            showToast(it)
        }


        homeViewModel.isLoading.observe(this) {
            showLoading(it)
        }


    }

    private fun setAction() {
        binding.fab.setOnClickListener {
            startActivity(Intent(this, AddActivity::class.java))
        }

        binding.pullRefresh.setOnRefreshListener {
            homeViewModel.getStories(token)
            binding.pullRefresh.isRefreshing = false
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showNoData(isNoData: Boolean) {
        binding.imgNoData.visibility = if (isNoData) View.VISIBLE else View.GONE
        binding.tvNoData.visibility = if (isNoData) View.VISIBLE else View.GONE

    }

    private fun showToast(msg: String) {
        if (homeViewModel.isError && !isFinished) {
            Toast.makeText(
                this,
                "${getString(R.string.error_load)} $msg",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun setUserData(story: List<ListStory>) {

        if (story.isEmpty()) {
            showNoData(true)
        } else {
            showNoData(false)
            val listUserAdapter = ListStoryAdapter(story)
            binding.rvStories.adapter = listUserAdapter

            listUserAdapter.setOnItemClickCallback(object : ListStoryAdapter.OnItemClickCallback {
                override fun onItemClicked(data: ListStory) {
                    sendSelectedUser(data)

                }
            })
        }

    }

    private fun sendSelectedUser(story: ListStory) {
        val intent = Intent(this@HomeActivity, DetailActivity::class.java)
        intent.putExtra(DetailActivity.EXTRA_STORY, story)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_items, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.lang) {
            startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
            return true
        } else if (id == R.id.logout) {
            showAlertDialog()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showAlertDialog() {
        val builder = AlertDialog.Builder(this)
        val alert = builder.create()
        builder
            .setTitle(getString(R.string.logout))
            .setMessage(getString(R.string.you_sure))
            .setPositiveButton(getString(R.string.no)) { _, _ ->
                alert.cancel()
            }
            .setNegativeButton(getString(R.string.yes)) { _, _ ->
                logout()
            }
            .show()
    }


    private fun logout() {
        val pref = MyPreference.getInstance(dataStore)
        val loginViewModel =
            ViewModelProvider(this, ViewModelFactory(pref))[LoginViewModel::class.java]
        loginViewModel.apply {
            saveLoginState(false)
            saveToken("")
            saveName("")
        }
        isFinished = true
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}