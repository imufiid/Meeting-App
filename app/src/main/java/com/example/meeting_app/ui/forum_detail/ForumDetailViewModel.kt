package com.example.meeting_app.ui.forum_detail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.meeting_app.api.ApiConfig
import com.example.meeting_app.data.entity.ForumEntity
import com.example.meeting_app.data.entity.ReplyEntity
import com.example.meeting_app.utils.SingleLiveEvent
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.HttpException

class EventViewModel: ViewModel() {
    private var forumDetail = MutableLiveData<ForumEntity>()
    private var state: SingleLiveEvent<ReplyState> = SingleLiveEvent()
    private var api = ApiConfig.instance()

    fun getDetailForum(idRapat: Int?, idForum: Int?) {
        state.value = ReplyState.IsLoading(true)
        CompositeDisposable().add(
            api.getDetailForum(idRapat, idForum)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    when(it.status) {
                        200 -> forumDetail.postValue(it.data)
                        else -> state.value = ReplyState.Error(it.message)
                    }
                    state.value = ReplyState.IsLoading()
                }, {
                    val httpException = it as HttpException
                    when(httpException.code()) {
                        404 -> {
                            val message = "Data tidak ditemukan"
                            state.value = ReplyState.Error(message)
                        }
                        else -> state.value = ReplyState.Error(it.message())
                    }
                    state.value = ReplyState.IsLoading()
                })
        )
    }

    fun replyForum( idForum: String?, idRapat: String?,  idUser: String?, isi: String?) {
        state.value = ReplyState.IsLoading(true)
        CompositeDisposable().add(
            api.replyForum(idForum,idRapat, idUser, isi)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    when(it.status) {
                        201 -> state.value = ReplyState.ReplyForum(it.message, it.data)
                        else -> state.value = ReplyState.Error(it.message)
                    }
                    state.value = ReplyState.IsLoading()
                }, {
                    state.value = ReplyState.Error(it.message)
                    state.value = ReplyState.IsLoading()
                })
        )
    }

    fun getDetailForum() = forumDetail
    fun getState() = state
}

sealed class ReplyState() {
    data class ReplyForum(var message: String?, var data: ReplyEntity?): ReplyState()
    data class IsLoading(var state: Boolean = false): ReplyState()
    data class Error(var err: String?): ReplyState()
}