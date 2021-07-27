package com.example.lblelinkplugin

import android.content.Context
import android.util.Log
import androidx.annotation.NonNull
import com.hpplay.sdk.source.api.IConnectListener
import com.hpplay.sdk.source.api.ILelinkPlayerListener
import com.hpplay.sdk.source.api.LelinkPlayerInfo
import com.hpplay.sdk.source.api.LelinkSourceSDK
import com.hpplay.sdk.source.browse.api.LelinkServiceInfo
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable

class LeBUtil private constructor() {
    var events: EventChannel.EventSink? = null
    val sdk: LelinkSourceSDK = LelinkSourceSDK.getInstance()
    val deviceList = mutableListOf<LelinkServiceInfo>()
    var selectLelinkServiceInfo: LelinkServiceInfo? = null
    var isCompleted: Boolean = false;
    var lastLinkIp by SharedPreference("lastLinkIp", "")
    var lastLinkName by SharedPreference("lastLinkName", "")
    var lastLinkUid by SharedPreference("lastLinkUid", "")

    private fun initListener() {
        sdk.run {
            setBrowseResultListener { code, resultList ->
                deviceList.clear()
                deviceList.addAll(resultList)

                var finalList = deviceList.map {
                    mapOf("tvName" to it.name, "tvUID" to it.uid, "ipAddress" to it.ip)
                }.toList()
                Observable.just(resultList).observeOn(AndroidSchedulers.mainThread()).subscribe {
                    events?.success(
                            buildResult(ResultType.divice, finalList)
                    )
                }

            }
            setConnectListener(object : IConnectListener {
                override fun onConnect(p0: LelinkServiceInfo?, p1: Int) {
                    Observable.just(p0).observeOn(AndroidSchedulers.mainThread()).subscribe {
                        if (p0!!.uid == null) {
                            return@subscribe
                        }
                        events?.success(
                                buildResult(ResultType.connect, "connect")
                        )

                        if (p0.uid != null) {
                            p0.run {
                                lastLinkIp = ip
                                lastLinkName = name
                                lastLinkUid = uid
                            }
                        }

                        Log.d("乐播云", "连接成功")
                        playListener()
                    }
                }

                override fun onDisconnect(p0: LelinkServiceInfo?, p1: Int, p2: Int) {
                    if (p1 == IConnectListener.CONNECT_INFO_DISCONNECT) {
                        events?.success(
                                buildResult(ResultType.disConnect, "disConnect")
                        )
                    } else if (p1 == IConnectListener.CONNECT_ERROR_FAILED) {
                        events?.success(buildResult(ResultType.connectError, "disConnect"))
                    }

                }
            })

        }
    }

    private fun LelinkSourceSDK.playListener() {
        setPlayListener(object : ILelinkPlayerListener {
            override fun onLoading() {
                Log.d("乐播云", "onLoading")
                Observable.just(1).observeOn(AndroidSchedulers.mainThread()).subscribe {
                    events?.success(buildResult(ResultType.load, "load"))
                }
                //                    events?.success( buildResult(ResultType.load))
            }

            override fun onPause() {
                Log.d("乐播云", "onPause")
                Observable.just(1).observeOn(AndroidSchedulers.mainThread()).subscribe {
                    events?.success(buildResult(ResultType.pause, "pause"))
                }
//                events?.success(Result().addParam("type", ResultType.pause))
            }

            override fun onCompletion() {
                Log.d("乐播云", "onCompletion")
                Observable.just(1).observeOn(AndroidSchedulers.mainThread()).subscribe {
                    events?.success(buildResult(ResultType.complete, "complete"))
                }
//                events?.success(Result().addParam("type", ResultType.complete))
            }

            override fun onStop() {
                Log.d("乐播云", "onStop")
                Observable.just(1).observeOn(AndroidSchedulers.mainThread()).subscribe {
                    events?.success(buildResult(ResultType.stop, "stop"))
                }
//                events?.success(Result().addParam("type", ResultType.stop))
            }

            override fun onSeekComplete(p0: Int) {
                Log.d("乐播云", "onSeekComplete")
//                events?.success(Result().addParam("type", ResultType.seek))
            }

            override fun onInfo(p0: Int, p1: Int) {
                Log.d("乐播云", "onInfo")
//                events?.success(Result().addParam("type", ResultType.info))
            }

            override fun onInfo(p0: Int, p1: String?) {
                Log.d("乐播云", "onInfo___$p1")
            }

            override fun onVolumeChanged(p0: Float) {
                Log.d("乐播云", "onVolumeChanged")
                Observable.just(1).observeOn(AndroidSchedulers.mainThread()).subscribe {
                    //                    events?.success(buildResult(ResultType., "onPositionUpdate"))
                }
            }

            override fun onPositionUpdate(p0: Long, p1: Long) {
                Log.d("乐播云", "onPositionUpdate")
//                events?.success(Result().addParam("type", ResultType.position))
                Observable.just(1).observeOn(AndroidSchedulers.mainThread()).subscribe {
                    events?.success(buildResult(ResultType.position, p1))
                }
            }

            override fun onError(p0: Int, p1: Int) {
                Log.d("乐播云", "onError")
//                events?.success(Result().addParam("type", ResultType.error))
                Observable.just(1).observeOn(AndroidSchedulers.mainThread()).subscribe {
                    events?.success(buildResult(ResultType.error, "error"))
                }
            }

            override fun onStart() {
                Log.d("乐播云", "star");

                Observable.just(1).observeOn(AndroidSchedulers.mainThread()).subscribe {
                    events?.success(buildResult(ResultType.start, "start"))
                }

            }
        })
    }

    companion object {
        val instance by lazy {
            LeBUtil()
        }
    }

    ///初始化SDK
    fun initUtil(ctx: Context, appId: String, secret: String, result: MethodChannel.Result) {
        sdk.bindSdk(ctx, appId, secret) {
            Observable.just(it).observeOn(AndroidSchedulers.mainThread()).subscribe { isComplete ->
                if (!isCompleted) {
                    isCompleted = true
                    result.success(isComplete)
                }

            }
            if (it) {
                sdk.setDebugMode(true)
                initListener()
            }
        }
    }

    ///连接设备
    fun connectService(id: String, name: String) {
        deviceList.forEach {
            //循环数据
            if (id == it.ip) {//确定连接项
                selectLelinkServiceInfo = it
            }
        }
//        sdk.connect(selectLelinkServiceInfo)
        events?.success(
                buildResult(ResultType.connect, null)
        )
    }

    fun buildResult(type: Int, data: Any?): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        map["type"] = type
        data?.run {
            map["data"] = this
        }
        return map
    }

    ///设备断链
    fun disConnect(@NonNull result: MethodChannel.Result) {
        sdk.connectInfos.run {
            if (sdk.disconnect(this[0])) {
                result.success(0)
            } else {
                result.success(-1)
            }

        }
    }

    ///暂停播放
    fun pause() {
        sdk.pause()
    }

    ///重新播放
    fun resumePlay() {
        sdk.resume()
    }

    ///停止播放
    fun stop() {
        sdk.stopPlay()
    }

    ///停止搜索
    fun stopSearch() {
        sdk.stopBrowse()
    }

    ///搜索设备
    fun searchDevice() {
        deviceList.clear()
        sdk.startBrowse()
    }

    ///播放视频
    fun play(url: String, position: Int, header: String?) {
        sdk.resume()
        var playerInfo = LelinkPlayerInfo()
        playerInfo?.run {
            this.url = url
            loopMode = LelinkPlayerInfo.LOOP_MODE_SINGLE
            type = LelinkSourceSDK.MEDIA_TYPE_VIDEO
            this.header = header
            lelinkServiceInfo = selectLelinkServiceInfo
            startPosition = position
        }
        sdk.startPlayMedia(playerInfo)
    }

    ///跳转到对应进度
    fun seek2Position(seekPosition: Int) {
        sdk.seekTo(seekPosition)
    }

    fun initEvent(events: EventChannel.EventSink?) {
        this.events = events
    }

    fun removeEvent() {
        events = null
    }

    fun getLastIp(result: MethodChannel.Result) {
        result.success(mapOf("tvName" to lastLinkName, "tvUID" to lastLinkUid, "ipAddress" to lastLinkIp))
    }

}