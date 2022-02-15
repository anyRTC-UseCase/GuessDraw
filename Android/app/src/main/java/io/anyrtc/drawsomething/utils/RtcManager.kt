package io.anyrtc.drawsomething.utils

import android.content.Context
import android.view.TextureView
import io.anyrtc.drawsomething.BuildConfig
import io.anyrtc.teamview.App
import org.ar.rtc.Constants
import org.ar.rtc.IRtcEngineEventHandler
import org.ar.rtc.RtcEngine
import org.ar.rtc.VideoEncoderConfiguration
import org.ar.rtc.video.VideoCanvas
import org.ar.rtm.*

class RtcManager private constructor() {

  companion object {
    val INSTANCE by lazy {
      RtcManager()
    }
  }

  private lateinit var rtmClient: RtmClient
  private lateinit var rtcEngine: RtcEngine

  private var rtmChannel: RtmChannel? = null
  private lateinit var rtmCallManager: RtmCallManager
  private var callbackList: MutableList<RtcHandler?> = mutableListOf()

  private val rtmCallListener by lazy {
    RtmCallListener(callbackList)
  }
  private val rtmListener by lazy {
    RtmListener(callbackList)
  }

  fun initRtc(context: Context, appId: String/*, rtcListener: IRtcEngineEventHandler*/) {
    rtcEngine = RtcEngine.create(
      context, appId, rtmListener
    )
  }

  /**
   * init rtmClint and rtmCallManager
   */
  fun initRtm(appId: String/*listener: RtmClientListener*/) {
    rtmClient = RtmClient.createInstance(
      App.app.applicationContext,
      appId,
      rtmListener.rtmClientListenerImpl/*listener*/
    )
    rtmCallManager = rtmClient.rtmCallManager
    rtmCallManager.setEventListener(rtmCallListener)
  }

  fun loginRtm(
    rtmToken: String,
    uid: String,
    loginFailedCallback: (filed: Boolean, var2: ErrorInfo?) -> Unit
  ) {
    logoutRtm()
    rtmClient.login(rtmToken, uid, object : ResultCallback<Void> {
      override fun onSuccess(var1: Void?) {
        loginFailedCallback.invoke(false, null)
      }

      override fun onFailure(var1: ErrorInfo?) {
        if (BuildConfig.DEBUG)
          loginFailedCallback.invoke(true, var1)
      }
    })
  }

  fun logoutRtm() {
    rtmClient.logout(null)
  }

  fun enableVideo() = rtcEngine.enableVideo()

  fun disableVideo() = rtcEngine.disableVideo()

  fun enableAudio(enable: Boolean) {
    rtcEngine.enableLocalAudio(enable)
    rtcEngine.muteLocalAudioStream(!enable)
    rtcEngine.setEnableSpeakerphone(true)
    //rtcEngine.enableAudio()
  }

  fun setChannelAttributes(channelId: String, list: List<RtmChannelAttribute>) {
    rtmClient.addOrUpdateChannelAttributes(
      channelId,
      list,
      ChannelAttributeOptions(true),
      null
    )
  }

  fun removeChannelAttributes(channelId: String, key: String) {
    rtmClient.deleteChannelAttributesByKeys(
      channelId,
      listOf(key),
      ChannelAttributeOptions(true),
      null
    )
  }

  fun getChannelAttributes(roomId: String, result: (List<RtmChannelAttribute>?) -> Unit) {
    rtmClient.getChannelAttributes(roomId, object : ResultCallback<List<RtmChannelAttribute>> {
      override fun onSuccess(var1: List<RtmChannelAttribute>?) {
        result.invoke(var1)
      }

      override fun onFailure(var1: ErrorInfo?) {
        result.invoke(null)
      }
    })
  }

  fun switchCamera() {
    rtcEngine.switchCamera()
  }

  /*fun getMemberInfo(uid: String, callback: (RtmChannelMember?) -> Unit) {
      this.getMemberList { list ->
          list ?: return@getMemberList
          list.forEach {
              if (it.userId == uid) {
                  callback.invoke(it)
                  return@getMemberList
              }
          }
          callback.invoke(null)
      }
  }*/

  fun joinChannel(
    rtcToken: String, roomId: String,
    userId: String, joinCallback: (failed: Boolean) -> Unit
  ) {
    rtcEngine.run {
      setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
      setClientRole(Constants.CLIENT_ROLE_BROADCASTER)
      joinChannel(rtcToken, roomId, "", userId)
    }
    rtmChannel?.leave(null)

    rtmChannel = rtmClient.createChannel(roomId, rtmListener)
    rtmChannel?.join(object : ResultCallback<Void> {
      override fun onSuccess(var1: Void?) {
        joinCallback.invoke(false)
      }

      override fun onFailure(var1: ErrorInfo?) {
        joinCallback.invoke(true)
      }
    })
  }

  fun sendMessage(json: String) {
    rtmChannel?.sendMessage(
      rtmClient.createMessage(json), null
    )
  }

  fun changeRoleToBroadcaster() {
    rtcEngine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER)
  }

  fun sendChannelMsg(msg: String) {
    rtmChannel?.sendMessage(rtmClient.createMessage(msg), null)
  }

  fun setUpTextureView(isLocal: Boolean, textureView: TextureView, uid: String) {
    val videoCanvas = VideoCanvas(textureView, Constants.RENDER_MODE_HIDDEN, uid)
    if (isLocal)
      rtcEngine.setupLocalVideo(videoCanvas)
    else
      rtcEngine.setupRemoteVideo(videoCanvas)
  }

  fun clearSelf() {
    RtcEngine.destroy()
    rtmChannel?.let {
      it.leave(null)
      it.release()
    }
    //RtmListener.INSTANCE.removeAllHandler()
    //logoutRtm()
    //rtmClient.logout(null)
  }

  fun getMemberList(callback: (List<RtmChannelMember>?) -> Unit) {
    if (rtmChannel == null) {
      callback.invoke(null)
      return
    }
    rtmChannel?.getMembers(object : ResultCallback<List<RtmChannelMember>> {
      override fun onSuccess(var1: List<RtmChannelMember>?) {
        callback.invoke(var1)
      }

      override fun onFailure(var1: ErrorInfo?) {
        callback.invoke(null)
      }
    })
  }

  fun addRtcHandler(handler: RtcHandler) {
    callbackList.add(handler)
  }

  fun removeRtcHandler(handler: RtcHandler) {
    val removed = callbackList.remove(handler)
  }

  fun sendCall(
    uid: String,
    content: String,
    sent: (success: bool, err: ErrorInfo?, localInvitation: LocalInvitation?) -> Unit
  ) {
    val localInvitation = rtmCallManager.createLocalInvitation(uid)
    if (localInvitation == null) {
      sent.invoke(false, ErrorInfo(0, "send failed"), null)
      return
    }
    localInvitation.content = content
    rtmCallManager.sendLocalInvitation(localInvitation, object : ResultCallback<Void> {
      override fun onSuccess(var1: Void?) {
        sent.invoke(true, null, localInvitation)
      }

      override fun onFailure(var1: ErrorInfo?) {
        sent.invoke(false, var1, localInvitation)
      }
    })
  }

  fun cancelCall(
    localInvitation: LocalInvitation,
    callback: (success: bool, err: ErrorInfo?) -> Unit
  ) {
    rtmCallManager.cancelLocalInvitation(localInvitation, object : ResultCallback<Void> {
      override fun onSuccess(var1: Void?) {
        callback.invoke(true, null)
      }

      override fun onFailure(var1: ErrorInfo?) {
        callback.invoke(false, var1)
      }
    })
  }

  fun createRendererView(context: Context): TextureView {
    return RtcEngine.CreateRendererView(context)
  }

  fun specifyLocalRenderMode() {
    rtcEngine.setVideoEncoderConfiguration(
      VideoEncoderConfiguration(VideoEncoderConfiguration.VideoDimensions(640, 480),
        VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15, 800,
        VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_LANDSCAPE)
    )
    rtcEngine.setLocalRenderMode(Constants.RENDER_MODE_FIT, Constants.VIDEO_MIRROR_MODE_AUTO)
  }

  abstract class RtcHandler() {
    open fun selfNetworkChange(disconnect: bool) {}

    open fun onAttributesUpdated(channelAttrs: List<RtmChannelAttribute>?) {}

    open fun onMessageReceived(var1: RtmMessage, var2: RtmChannelMember) {}

    open fun onRtmTokenExpired() {}

    open fun onRtcTokenExpired() {}

    open fun onRtcTokenPrivilegeWillExpire(token: String?) {}

    open fun onUserJoined(uid: String?, elapsed: Int) {}

    open fun onUserLeave(uid: String?, reason: Int) {}

    open fun onMemberJoined(var1: RtmChannelMember?) {}

    open fun onMemberLeft(var1: RtmChannelMember?) {}

    /**
     * 其他端登录
     */
    open fun onOtherClientLogin() {}

    open fun joinChannelSuccess(channel: String?, uid: String?, elapsed: Int) {}

    open fun onCallLocalAccept(var1: LocalInvitation?) {}

    open fun onCallLocalReject(var1: LocalInvitation?) {}

    open fun onCallRemoteFailure(var1: RemoteInvitation?, var2: Int) {}

    open fun onCallFailure(var1: LocalInvitation?, var2: Int) {}
  }

  private class RtmListener constructor(private val callbackList: MutableList<RtcHandler?>) :
    RtmChannelListener, IRtcEngineEventHandler() {
    private var disconnecting = false

    val rtmClientListenerImpl = object : RtmClientListener {
      override fun onConnectionStateChanged(var1: Int, var2: Int) {
        when (var1) {
          Constants.CONNECTION_STATE_RECONNECTING -> { // offline
            if (!disconnecting) {
              disconnecting = true
              callbackList.forEach {
                it?.selfNetworkChange(true)
              }
            }
          }
          Constants.CONNECTION_STATE_CONNECTED -> { // reconnected
            if (disconnecting) {
              disconnecting = false
              callbackList.forEach {
                it?.selfNetworkChange(false)
              }
            }
          }
        }
        if (var2 == 8) callbackList.forEach {
          it?.onOtherClientLogin()
        }
      }

      override fun onMessageReceived(var1: RtmMessage?, var2: String?) {
      }

      override fun onTokenExpired() {
        callbackList.forEach {
          it?.onRtmTokenExpired()
        }
      }

      override fun onPeersOnlineStatusChanged(var1: MutableMap<String, Int>?) {
      }
    }

    // -- client listener --
    override fun onConnectionStateChanged(state: Int, reason: Int) {
    }

    override fun onTokenPrivilegeWillExpire(token: String?) {
      callbackList.forEach {
        it?.onRtcTokenPrivilegeWillExpire(token)
      }
    }

    override fun onRequestToken() {
      callbackList.forEach {
        it?.onRtcTokenExpired()
      }
    }

    // -- channel listener --
    override fun onMemberCountUpdated(var1: Int) {
    }

    override fun onAttributesUpdated(var1: MutableList<RtmChannelAttribute>?) {
      callbackList.forEach {
        it?.onAttributesUpdated(var1)
      }
      /*var1.forEach {
          if (it.key == "allAudioState") {
              val mute = it.value == "1"
              callbackList.forEach { item ->
                  item?.onAttributesUpdated(mute)
              }
          }
      }*/
    }

    override fun onMessageReceived(var1: RtmMessage?, var2: RtmChannelMember?) {
      var1 ?: return
      var2 ?: return
      callbackList.forEach { item ->
        item?.onMessageReceived(var1, var2)
      }
    }

    override fun onMemberJoined(var1: RtmChannelMember?) {
      callbackList.forEach { item ->
        item?.onMemberJoined(var1)
      }
    }

    override fun onMemberLeft(var1: RtmChannelMember?) {
      callbackList.forEach { item ->
        item?.onMemberLeft(var1)
      }
    }

    override fun onJoinChannelSuccess(channel: String?, uid: String?, elapsed: Int) {
      callbackList.forEach {
        it?.joinChannelSuccess(channel, uid, elapsed)
      }
    }

    override fun onUserJoined(uid: String?, elapsed: Int) {
      callbackList.forEach {
        it?.onUserJoined(uid, elapsed)
      }
    }

    override fun onUserOffline(uid: String?, reason: Int) {
      callbackList.forEach {
        it?.onUserLeave(uid, reason)
      }
    }
  }

  private class RtmCallListener(private val callbackList: MutableList<RtcHandler?>) :
    RtmCallEventListener {
    override fun onLocalInvitationReceivedByPeer(var1: LocalInvitation?) {
    }

    override fun onLocalInvitationAccepted(var1: LocalInvitation?, var2: String?) {
      // 接受邀请
      callbackList.forEach { it?.onCallLocalAccept(var1) }
    }

    override fun onLocalInvitationRefused(var1: LocalInvitation?, var2: String?) {
      // 拒绝邀请
      callbackList.forEach { it?.onCallLocalReject(var1) }
    }

    override fun onLocalInvitationCanceled(var1: LocalInvitation?) {
    }

    override fun onLocalInvitationFailure(var1: LocalInvitation?, var2: Int) {
      callbackList.forEach { it?.onCallFailure(var1, var2) }
    }

    override fun onRemoteInvitationReceived(var1: RemoteInvitation?) {
    }

    override fun onRemoteInvitationAccepted(var1: RemoteInvitation?) {
    }

    override fun onRemoteInvitationRefused(var1: RemoteInvitation?) {
    }

    override fun onRemoteInvitationCanceled(var1: RemoteInvitation?) {
    }

    override fun onRemoteInvitationFailure(var1: RemoteInvitation?, var2: Int) {
      callbackList.forEach { it?.onCallRemoteFailure(var1, var2) }
    }
  }

  data class ChannelMsgData(
    val uid: String,
    val json: String
  )

  data class AudioStateChangeData(
    val uid: String,
    val mute: Boolean
  )
}
