package com.example.storyapp


import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.storyapp.databinding.ItemRowStoryBinding
import com.example.storyapp.dataclass.ListStory


class ListStoryAdapter(private val listStory: List<ListStory>) :
    RecyclerView.Adapter<ListStoryAdapter.ListViewHolder>() {
    private lateinit var onItemClickCallback: OnItemClickCallback

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding =
            ItemRowStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.bind(listStory[position])
        holder.itemView.setOnClickListener {
            onItemClickCallback.onItemClicked(listStory[holder.bindingAdapterPosition])
        }
    }

    class ListViewHolder(private var binding: ItemRowStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: ListStory) {
            binding.mystory = data
            binding.executePendingBindings()
        }
    }

    override fun getItemCount(): Int = listStory.size

    interface OnItemClickCallback {
        fun onItemClicked(data: ListStory)
    }

    companion object {

        @JvmStatic
        @BindingAdapter("setPhoto")
        fun setPhoto(img_photo: ImageView, url: String) {
            Glide.with(img_photo)
                .load(url)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .fallback(R.drawable.ic_launcher_foreground)
                .into(img_photo)
        }
    }


}