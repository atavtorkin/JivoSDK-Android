package com.jivosite.sdk.ui.chat.items.message.mediaimport android.view.Viewimport com.jivosite.sdk.support.ext.Intentsimport com.jivosite.sdk.ui.chat.items.HistoryMessageEntryimport com.jivosite.sdk.ui.chat.items.message.general.MessageItemViewHolder/** * Created on 25.10.2021. * * @author Alexander Tavtorkin (av.tavtorkin@gmail.com) */open class MediaItemViewHolder<T : HistoryMessageEntry>(    itemView: View,    private val viewModel: MediaItemViewModel<T>) : MessageItemViewHolder<T>(itemView, viewModel) {    fun onDownload() {        val state = viewModel.newState.value        if (state is MediaItemState.Success) {            val media = state.media            Intents.downloadFile(context, media.path, media.name)        }    }    fun show() {        val state = viewModel.newState.value        if (state is MediaItemState.Success) {            val media = state.media            Intents.showActivityImageViewer(context, media.path, media.name)        }    }}