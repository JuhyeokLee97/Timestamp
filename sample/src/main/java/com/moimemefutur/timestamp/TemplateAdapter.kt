package com.moimemefutur.timestamp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.moimemefutur.timestamp.databinding.ItemTemplateSelectionBinding
import com.moimemefutur.timestamp.template.TemplateInfo

class TemplateAdapter(
    private val onTemplateSelected: (TemplateInfo) -> Unit
) : ListAdapter<TemplateInfo, TemplateAdapter.TemplateViewHolder>(TemplateDiffCallback()) {

    private var selectedTemplateId: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TemplateViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemTemplateSelectionBinding.inflate(inflater, parent, false)
        return TemplateViewHolder(binding, onTemplateSelected)
    }

    override fun onBindViewHolder(holder: TemplateViewHolder, position: Int) {
        val template = getItem(position)
        val isSelected = template.id == selectedTemplateId
        holder.bind(template, isSelected)
    }

    fun setSelectedTemplate(templateId: String) {
        val previousSelectedPosition = currentList.indexOfFirst { it.id == selectedTemplateId }
        val newSelectedPosition = currentList.indexOfFirst { it.id == templateId }

        selectedTemplateId = templateId

        if (previousSelectedPosition != -1) {
            notifyItemChanged(previousSelectedPosition)
        }
        if (newSelectedPosition != -1) {
            notifyItemChanged(newSelectedPosition)
        }
    }

    class TemplateViewHolder(
        private val binding: ItemTemplateSelectionBinding,
        private val onTemplateSelected: (TemplateInfo) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(template: TemplateInfo, isSelected: Boolean) = with(binding) {
            template.thumbnailRes?.let {
                ivThumbnail.setImageResource(it)
            }
            vUnselectedOverlay.isVisible = !isSelected
            root.setOnClickListener {
                onTemplateSelected(template)
            }
        }
    }

    class TemplateDiffCallback : DiffUtil.ItemCallback<TemplateInfo>() {
        override fun areItemsTheSame(oldItem: TemplateInfo, newItem: TemplateInfo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TemplateInfo, newItem: TemplateInfo): Boolean {
            return oldItem == newItem
        }
    }
}