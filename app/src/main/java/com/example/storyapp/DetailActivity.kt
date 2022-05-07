package com.example.storyapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.storyapp.databinding.ActivityDetailBinding
import com.example.storyapp.dataclass.ListStory

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val story = intent.getParcelableExtra<ListStory>(EXTRA_STORY) as ListStory
        setActionBar(story.name)
        setStory(story)

    }

    private fun setStory(story: ListStory) {
        binding.apply {
            tvName.text = story.name
            tvDate.text = story.createdAt
            tvDes.text = story.description
        }
        Glide.with(this)
            .load(story.photoUrl)
            .placeholder(R.drawable.ic_launcher_foreground)
            .into(binding.imgPhoto)
    }

    @SuppressLint("RestrictedApi")
    private fun setActionBar(story: String) {
        val actionBar = supportActionBar
        actionBar?.title = getString(R.string.detail_title, story)
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
        const val EXTRA_STORY = "extra_story"
    }
}