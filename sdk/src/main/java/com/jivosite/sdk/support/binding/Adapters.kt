package com.jivosite.sdk.support.binding

import android.graphics.Paint
import android.text.SpannableStringBuilder
import android.text.format.DateFormat
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.imageLoader
import coil.load
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import coil.transform.RoundedCornersTransformation
import com.jivosite.sdk.Jivo
import com.jivosite.sdk.R
import com.jivosite.sdk.model.pojo.agent.Agent
import com.jivosite.sdk.model.pojo.agent.AgentStatus
import com.jivosite.sdk.model.pojo.file.SupportFileTypes.Companion.TYPE_AUDIO
import com.jivosite.sdk.model.pojo.file.SupportFileTypes.Companion.TYPE_DOCUMENT
import com.jivosite.sdk.model.pojo.file.SupportFileTypes.Companion.TYPE_IMAGE
import com.jivosite.sdk.model.pojo.file.SupportFileTypes.Companion.TYPE_VIDEO
import com.jivosite.sdk.model.pojo.message.MessageStatus
import com.jivosite.sdk.model.repository.upload.FileState
import com.jivosite.sdk.model.repository.upload.UploadState
import com.jivosite.sdk.support.coil.ImageTransformation
import com.jivosite.sdk.support.dg.AdapterDelegateItem
import com.jivosite.sdk.support.dg.adapters.BaseAdapter
import com.jivosite.sdk.support.ext.cutName
import com.jivosite.sdk.support.ext.dp
import com.jivosite.sdk.support.ext.getFileType
import com.jivosite.sdk.ui.chat.items.message.media.MediaItemState
import java.util.*
import kotlin.math.pow

/**
 * Created on 22.09.2020.
 *
 * @author Alexander Tavtorkin (av.tavtorkin@gmail.com)
 */
@BindingAdapter("avatarUrl")
fun loadAvatar(view: AppCompatImageView, url: String?) {
    if (url.isNullOrBlank()) {
        view.setImageResource(R.drawable.vic_avatar_empty)
    } else {
        Jivo.d("Load avatar by url - $url")
        view.load(url) {
            placeholder(R.drawable.vic_avatar_empty)
            error(R.drawable.vic_avatar_empty)
            transformations(CircleCropTransformation())
        }
    }
}

@BindingAdapter("imageUrl")
fun loadImage(view: AppCompatImageView, url: String?) {
    if (url.isNullOrBlank()) {
        view.setImageResource(R.drawable.vic_avatar_empty)
    } else {
        val cornerRadius = view.resources.getDimensionPixelSize(R.dimen.message_bubble_corner).toFloat()
        view.load(url) {
            placeholder(R.drawable.vic_avatar_empty)
            error(R.drawable.vic_avatar_empty)
            transformations(RoundedCornersTransformation(cornerRadius))
        }
    }
}

@BindingAdapter("setFileSize")
fun setFileSize(view: AppCompatTextView, size: Long) {
    val context = view.context
    when (size) {
        in 0..999 -> view.text = context.getString(R.string.format_file_size_b, size.toDouble())
        in 1000..999999 -> view.text = context.getString(R.string.format_file_size_kb, size.div(1000.0))
        else -> view.text = context.getString(R.string.format_file_size_mb, size.div(10.0.pow(6)))
    }
}

@BindingAdapter("uploadState")
fun setUploadState(view: AppCompatTextView, state: FileState?) {
    val context = view.context
    val uploadingState = state?.uploadState ?: return
    when (uploadingState) {
        is UploadState.Uploading -> {
            view.isClickable = false
            view.isFocusable = false
            view.text = "${uploadingState.percent}% ${context.getString(R.string.download_status_loading)}"
        }
        is UploadState.Error -> {
            view.isClickable = false
            view.isFocusable = false
            view.text = context.getString(R.string.download_status_error)
        }
    }
}

@BindingAdapter("items")
fun <T> setItems(view: RecyclerView, items: List<AdapterDelegateItem<T>>?) {
    if (view.adapter is BaseAdapter<*>) {
        (view.adapter as BaseAdapter<T>).items = items ?: Collections.emptyList()
    }
}

@BindingAdapter("messageStatus")
fun setMessageStatus(view: AppCompatImageView, status: MessageStatus?) {
    val (isVisible, imageResId) = when (status) {
        is MessageStatus.Sending -> true to R.drawable.vic_message_status_sending
        is MessageStatus.Sent -> true to R.drawable.vic_message_status_sent
        is MessageStatus.Delivered -> true to R.drawable.vic_message_status_delivered
        is MessageStatus.Error -> true to R.drawable.vic_message_status_error
        else -> false to 0
    }
    view.setImageResource(imageResId)
    view.isInvisible = !isVisible
}

@BindingAdapter("typing")
fun setAgentsTyping(view: AppCompatTextView, agents: List<Agent>) {
    view.isInvisible = agents.isNullOrEmpty()
    if (agents.isNullOrEmpty()) return
    val context = view.context
    view.text = when (agents.size) {
        1 -> context.getString(R.string.chat_system_one_agent_typing, agents[0].name)
        2 -> context.getString(R.string.chat_system_two_agents_typing, agents[0].name.cutName(), agents[1].name.cutName())
        else -> context.getString(
            R.string.chat_system_few_agents_typing,
            agents[0].name.cutName(),
            agents[1].name.cutName(),
            agents.size - 2
        )
    }
}

@BindingAdapter("appBarAvatar")
fun setAppBarAvatar(view: AppCompatImageView, agents: List<Agent>?) {

    if (!Jivo.getConfig().isLogoVisible) {
        view.isVisible = false
        return
    }

    val agentsInChat = agents?.filter { it.hasOnlineInChat && it.status !is AgentStatus.Offline } ?: Collections.emptyList()
    when {
        agentsInChat.isEmpty() -> {
            view.isVisible = true
            view.load(Jivo.getConfig().logo ?: R.drawable.vic_logo) {
                transformations(CircleCropTransformation())
            }
        }
        agentsInChat.size == 1 -> {
            view.isVisible = true
            view.load(agentsInChat[0].photo) {
                placeholder(R.drawable.vic_avatar_empty)
                transformations(CircleCropTransformation())
                error(R.drawable.vic_avatar_empty)
            }
        }
        agentsInChat.size > 1 -> {
            view.isVisible = false
        }
    }
}

@BindingAdapter("appBarTitle")
fun setAppBarTitle(view: AppCompatTextView, agents: List<Agent>?) {
    val agentsInChat = agents?.filter { it.hasOnlineInChat && it.status !is AgentStatus.Offline } ?: Collections.emptyList()
    when {
        agentsInChat.isEmpty() -> {
            view.setText(Jivo.getConfig().title ?: R.string.chat_title_placeholder)
        }
        agentsInChat.size == 1 -> view.text = agentsInChat[0].name
        agentsInChat.size > 1 -> {
            view.text = SpannableStringBuilder().apply {
                agentsInChat.forEachIndexed { index, agent ->
                    append(agent.name.split(" ").first())
                    if (index < agentsInChat.size - 1) {
                        append(", ")
                    }
                }
            }.toString()
        }
    }
    val color = AppCompatResources.getColorStateList(view.context, Jivo.getConfig().titleTextColor ?: R.color.white)
    view.setTextColor(color)
}

@BindingAdapter("agentImageLoader")
fun agentImageLoader(layout: ViewGroup, state: MediaItemState?) {
    if (state == null) return

    var viewHolder: ImageViewHolder? = layout.tag as ImageViewHolder?
    if (viewHolder == null) {
        viewHolder = ImageViewHolder(layout)
    }

    when (state) {
        MediaItemState.Initial -> {
            viewHolder.imageView.setImageResource(R.drawable.ic_placeholder_image)
        }
        MediaItemState.Loading -> {
            viewHolder.imageView.setImageResource(R.drawable.ic_placeholder_image)
            viewHolder.errorText?.isVisible = false
            viewHolder.progressView?.isVisible = true
        }
        is MediaItemState.Success -> {
            viewHolder.errorText?.isVisible = false
            val imageLoader = viewHolder.imageView.context.imageLoader
            val request = ImageRequest.Builder(viewHolder.imageView.context)
                .data(state.media.path)
                .placeholder(R.drawable.ic_placeholder_image)
                .error(R.drawable.vic_broken_image)
                .transformations(
                    ImageTransformation(
                        layout.resources.getDimensionPixelSize(R.dimen.message_bubble_corner).toFloat()
                    )
                )
                .target(
                    onStart = { placeholder ->
                        viewHolder.imageView.setImageDrawable(placeholder)
                        viewHolder.progressView?.isVisible = true
                    },
                    onSuccess = { result ->
                        viewHolder.imageView.setImageDrawable(result)
                        viewHolder.progressView?.isVisible = false
                    },
                    onError = { error ->
                        viewHolder.imageView.setImageDrawable(error)
                        viewHolder.progressView?.isVisible = false
                    }
                )
                .build()
            imageLoader.enqueue(request)
        }
        MediaItemState.Expired -> {
            viewHolder.imageView.setImageResource(R.drawable.vic_broken_image)
            viewHolder.errorText?.isVisible = true
        }
        is MediaItemState.Error -> {
            viewHolder.imageView.setImageResource(R.drawable.vic_broken_image)
        }
    }
}

@BindingAdapter("clientImageLoader")
fun clientImageLoader(layout: ViewGroup, state: FileState?) {
    val uri = state?.uri ?: return

    var viewHolder: ImageViewHolder? = layout.tag as ImageViewHolder?
    if (viewHolder == null) {
        viewHolder = ImageViewHolder(layout)
    }

    val imageLoader = viewHolder.imageView.context.imageLoader
    val requestBuilder = ImageRequest.Builder(viewHolder.imageView.context)
        .data(uri)
        .placeholder(R.drawable.ic_placeholder_image)
        .error(R.drawable.vic_broken_image)
        .transformations(ImageTransformation(12f.dp))

    if (URLUtil.isContentUrl(uri)) {
        viewHolder.progressView?.isVisible = state.uploadState is UploadState.Uploading
        viewHolder.status?.setImageResource(
            when (state.uploadState) {
                is UploadState.Uploading -> R.drawable.vic_message_status_sending
                is UploadState.Error -> R.drawable.vic_message_status_error
            }
        )
        requestBuilder.target(
            onStart = { placeholder ->
                viewHolder.imageView.setImageDrawable(placeholder)
            },
            onSuccess = { result ->
                viewHolder.imageView.setImageDrawable(result)
            },
            onError = { error ->
                viewHolder.imageView.setImageDrawable(error)
            }
        )
    } else {
        requestBuilder.target(
            onStart = { placeholder ->
                viewHolder.imageView.setImageDrawable(placeholder)
                viewHolder.progressView?.isVisible = true
            },
            onSuccess = { result ->
                viewHolder.imageView.setImageDrawable(result)
                viewHolder.progressView?.isVisible = false
            },
            onError = { error ->
                viewHolder.imageView.setImageDrawable(error)
                viewHolder.progressView?.isVisible = false
            }
        )
    }
    imageLoader.enqueue(requestBuilder.build())
}

private class ImageViewHolder(layout: ViewGroup) {
    val imageView: AppCompatImageView = layout.findViewById(R.id.image)
    val progressView: ProgressBar? = layout.findViewById(R.id.progress)
    val status: AppCompatImageView? = layout.findViewById(R.id.status)
    val time: TextView? = layout.findViewById(R.id.time)
    val errorText: TextView? = layout.findViewById(R.id.error)
}

@BindingAdapter("fileIcon")
fun setFileIcon(view: AppCompatImageView, type: String?) {

    view.load(
        when (type?.getFileType()) {
            TYPE_DOCUMENT -> R.drawable.vic_file
            TYPE_IMAGE -> R.drawable.vic_image
            TYPE_VIDEO -> R.drawable.vic_video
            TYPE_AUDIO -> R.drawable.vic_audio
            else -> R.drawable.vic_file
        }
    )
}

@BindingAdapter("time")
fun setTime(view: TextView, time: Long?) {
    if (time == null || time == 0L) {
        return
    } else {
        view.text = DateFormat.getTimeFormat(view.context).format(Date(time * 1000))
    }
}

@BindingAdapter("agentName")
fun setAgentName(view: AppCompatTextView, name: String?) {
    if (name.isNullOrBlank()) {
        view.setText(R.string.agent_name_default)
    } else {
        view.text = name
    }
}

@BindingAdapter("mediaStatus")
fun setMediaStatus(view: TextView, state: MediaItemState?) {

    if (state == null) return
    val context = view.context

    when (state) {

        MediaItemState.Initial -> {
        }

        MediaItemState.Loading -> {
            view.isClickable = false
            view.text = context.getString(R.string.file_link_checking)
        }
        is MediaItemState.Success -> {
            if (!state.media.isExpired) {
                view.isClickable = true
                view.text = context.getString(R.string.message_download)
                view.paintFlags = view.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            } else {
                view.isInvisible = false
            }
        }

        MediaItemState.Expired -> view.isVisible = false

        is MediaItemState.Error -> {
            view.isClickable = false
            view.text = context.getString(R.string.download_status_error)
        }
    }

}

@BindingAdapter("fileName")
fun setFileName(view: TextView, state: MediaItemState?) {
    if (state == null) return
    when (state) {
        is MediaItemState.Success -> view.text = state.media.name
        MediaItemState.Expired -> view.text = view.context.getString(R.string.file_download_expired)
        else -> view.text = ""
    }
}
