package io.anyrtc.drawsomething.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.anyrtc.arboard.ARBoardEnumerates.*
import io.anyrtc.arboard.ARBoardHandler
import io.anyrtc.arboard.ARBoardKit
import io.anyrtc.arboard.ARBoardStructures.ARBoardAuthParam
import io.anyrtc.arboard.ARBoardStructures.ARBoardBaseParam
import io.anyrtc.drawsomething.R
import io.anyrtc.drawsomething.databinding.ActivityMainBinding
import io.anyrtc.drawsomething.fragments.InputDialogFragment
import io.anyrtc.drawsomething.utils.*
import io.anyrtc.drawsomething.widgets.SeekBarWidget
import org.ar.rtm.*
import java.io.File

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var kit: ARBoardKit

    private lateinit var uid: str
    private lateinit var roomId: str

    private var drawing = false
    private var disconnected = false

    private var onlineCount = 1

    private val chatAdapter by lazy {
        ChatAdapter()
    }
    private val inputFragmentDialog by lazy {
        InputDialogFragment()
    }

    private val colorTransition = SeekBarWidget.ColorTransition(
        Color.parseColor("#00000000"),
        Color.parseColor("#99000000")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        uid = intent.getStringExtra("uid")!!
        roomId = intent.getStringExtra("roomId")!!
        val appId = resources.getString(R.string.app_id)

        initCore(appId)
        initWidget()
    }

    private fun initWidget() {
        val behavior = BottomSheetBehavior.from(binding.toolsMenu)
        behavior.peekHeight = 0
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    binding.behaviorMask.visibility = View.GONE
                    binding.colorPicker.isChecked = false
                    binding.pen.isChecked = false
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (slideOffset > 0.0f && binding.behaviorMask.visibility == View.GONE)
                    binding.behaviorMask.visibility = View.VISIBLE

                val color = colorTransition.getValue(slideOffset)
                binding.behaviorMask.setBackgroundColor(color)
            }
        })

        binding.run {
            roomId.text = String.format("ID:%s", this@MainActivity.roomId)
            totalOnline.text = String.format("在线人数：%d", onlineCount)

            seekRed.setOnProgressChangListener(progressChange(redSeekNum))
            seekGreen.setOnProgressChangListener(progressChange(greenSeekNum))
            seekBlue.setOnProgressChangListener(progressChange(blueSeekNum))
            seekPen.setOnProgressChangListener(
                progressChange(
                    penThinNum,
                    notifyColorRefresh = false
                )
            )
        }

        binding.pen.setOnClickListener {
            binding.rgbGroup.visibility = View.GONE
            binding.brushThinGroup.visibility = View.VISIBLE
            behavior.state = BottomSheetBehavior.STATE_EXPANDED

            binding.toolsConfirm.setOnClickListener {
                kit.brushThin = binding.seekPen.progress
                behavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
        binding.colorPicker.setOnClickListener {
            binding.rgbGroup.visibility = View.VISIBLE
            binding.brushThinGroup.visibility = View.GONE
            behavior.state = BottomSheetBehavior.STATE_EXPANDED

            val currentColor = Color.parseColor(kit.brushColor)
            val redNum = Color.red(currentColor)
            val greenNum = Color.green(currentColor)
            val blueNum = Color.blue(currentColor)
            binding.redSeekNum.text = "$redNum"
            binding.greenSeekNum.text = "$greenNum"
            binding.blueSeekNum.text = "$blueNum"

            binding.toolsConfirm.setOnClickListener {
                val color = refreshColors()
                val strColor = String.format("#%06X", 0xFFFFFF and color)
                kit.brushColor = strColor
                binding.currentColor.backgroundColor = color
                behavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }

            binding.seekRed.progress = redNum
            binding.seekGreen.progress = greenNum
            binding.seekBlue.progress = blueNum
            binding.currentColor.backgroundColor = currentColor
        }
        binding.undo.setOnClickListener {
            kit.undo()
        }
        binding.clear.setOnClickListener {
            kit.clearDraws()
        }
        binding.saveScreenshotBg.setOnClickListener {
            val file = File(
                Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM
                ).absolutePath + "/Screenshots", String.format("%s.png", System.currentTimeMillis())
            )
            file.createNewFile()
            kit.snapshot(file.absolutePath)
        }

        binding.behaviorMask.setOnClickListener {
            behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        binding.chatList.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = chatAdapter
        }
        binding.chat.setOnClickListener {
            inputFragmentDialog.show(supportFragmentManager) { msg ->
                RtcManager.INSTANCE.sendMessage(msg)
                chatAdapter.addData(MyConstants.ChatData(uid, msg, isSelf = true))
            }
        }
        binding.voice.setOnClickListener {
            RtcManager.INSTANCE.enableAudio(binding.voice.isChecked)
        }
        binding.startDraw.setOnClickListener {
            if (disconnected) {
                Toast.makeText(this, "请检查网络", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            RtcManager.INSTANCE.setChannelAttributes(
                roomId,
                listOf(RtmChannelAttribute("ARKeyChannel", uid))
            )
        }
        binding.quitDraw.setOnClickListener {
            if (disconnected) {
                Toast.makeText(this, "请检查网络", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            RtcManager.INSTANCE.removeChannelAttributes(roomId, "ARKeyChannel")
            switchToVisitor()
        }
        binding.exit.setOnClickListener {
            finish()
            startActivity(Intent(this, LoginActivity::class.java))
        }
        binding.loadingView.setOnClickListener { }
    }

    private fun progressChange(
        textView: TextView,
        notifyColorRefresh: Boolean = true
    ): SeekBarWidget.OnProgressChangeListener {
        return SeekBarWidget.OnProgressChangeListener { progress ->
            textView.text = String.format("%d", progress)
            if (notifyColorRefresh) refreshColors()
        }
    }

    private fun refreshColors(): Int {
        val color = Color.rgb(
            binding.seekRed.progress,
            binding.seekGreen.progress,
            binding.seekBlue.progress
        )
        binding.currentColor.backgroundColor = color
        return color
    }

    private fun switchToDrawMode() {
        kit.isDrawEnable = true
        binding.whoDrawing.text = "正在作画中.."
        binding.run {
            drawToolsGroup.visibility = View.VISIBLE
            startDraw.visibility = View.GONE
            quitDraw.visibility = View.VISIBLE
        }
        drawing = true
    }

    private fun switchToVisitor(otherUserDrawing: bool = false) {
        kit.isDrawEnable = false
        binding.whoDrawing.text = ""
        binding.run {
            drawToolsGroup.visibility = View.GONE
            quitDraw.visibility = View.GONE
            startDraw.visibility = if (otherUserDrawing) View.GONE else View.VISIBLE
        }
        drawing = false
    }

    private fun initCore(appId: String) {
        // board
        val authConfig = ARBoardAuthParam(appId, "", uid)
        val baseParams = ARBoardBaseParam()
        baseParams.authConfig.progressEnable = false
        baseParams.config.ratio = "1:1"
        baseParams.styleConfig.run {
            brushThin = 2
            brushColor = "#FF0000"
        }

        kit = ARBoardKit(this, authConfig, roomId, baseParams, MyBoardHandler())
        val boardView = kit.arBoardView
        binding.boardParent.addView(boardView)

        // rtc&rtm
        RtcManager.INSTANCE.run {
            initRtm(appId)
            initRtc(this@MainActivity, appId)
            disableVideo()
            loginRtm("", uid) { failed, failInfo ->
                if (failed) runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "加入房间失败${failInfo.toString()}",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
            joinChannel("", roomId, uid) { failed ->
                if (failed) runOnUiThread {
                    Toast.makeText(this@MainActivity, "加入房间失败", Toast.LENGTH_SHORT).show()
                    finish()
                    return@runOnUiThread
                }
                runOnUiThread {
                    checkOnlineUser()
                    enableAudio(true)
                }
            }
            binding.voice.isChecked = true
            addRtcHandler(MyRtcHandler())
        }
    }

    override fun onDestroy() {
        if (drawing) RtcManager.INSTANCE.removeChannelAttributes(roomId, "ARKeyChannel")
        RtcManager.INSTANCE.clearSelf()
        kit.leaveChannel()
        super.onDestroy()
    }

    @Synchronized
    private fun checkOnlineUser() {
        synchronized(kit) {
            RtcManager.INSTANCE.getMemberList {
                runOnUiThread {
                    onlineCount = it?.size ?: 0
                    binding.totalOnline.text = String.format("在线人数：%d", onlineCount)
                }
            }
        }
    }

    private inner class MyRtcHandler : RtcManager.RtcHandler() {

        override fun selfNetworkChange(disconnect: bool) {
            runOnUiThread {
                if (!disconnect)
                    binding.loadingView.hideLoading()
                else
                    binding.loadingView.showLoading("网络异常")
            }

            if (disconnect) {
                disconnected = disconnect
            }
            if (disconnected && !disconnect) {
                synchronized(kit) {
                    RtcManager.INSTANCE.getChannelAttributes(roomId) { attrs ->
                        if (null != attrs && attrs.isNotEmpty()) for (attr in attrs) {
                            if (attr.key == "ARKeyChannel" && attr.value.isNotBlank() && attr.lastUpdateUserId != uid) {
                                runOnUiThread {
                                    if (drawing) {
                                        switchToVisitor()
                                    } else {
                                        binding.whoDrawing.text =
                                            String.format("%s开始作画", attr.lastUpdateUserId)
                                        binding.startDraw.visibility = View.GONE
                                    }
                                }
                                return@getChannelAttributes
                            }
                        }
                        if (attrs != null && attrs.isEmpty() && !drawing) runOnUiThread {
                            binding.whoDrawing.text = ""
                            binding.startDraw.visibility = View.VISIBLE
                        }
                    }
                    disconnected = false
                }
            }
        }

        override fun onAttributesUpdated(channelAttrs: List<RtmChannelAttribute>?) {
            if (null == channelAttrs || channelAttrs.isEmpty()) {
                runOnUiThread {
                    binding.whoDrawing.text = ""
                    binding.startDraw.visibility = View.VISIBLE
                }
                return
            }

            synchronized(kit) {
                val attr = channelAttrs[0]
                if (attr.key == "ARKeyChannel" && attr.value.isNotBlank() && attr.lastUpdateUserId != uid) {
                    runOnUiThread {
                        switchToVisitor(true)
                        binding.whoDrawing.text = String.format("%s开始作画", attr.lastUpdateUserId)
                    }
                    return@synchronized
                }
                if (attr.lastUpdateUserId == uid) runOnUiThread {
                    switchToDrawMode()
                }
            }
        }

        override fun onMessageReceived(var1: RtmMessage, var2: RtmChannelMember) {
            runOnUiThread { chatAdapter.addData(MyConstants.ChatData(var2.userId, var1.text)) }
        }

        override fun onRtmTokenExpired() {
            super.onRtmTokenExpired()
        }

        override fun onRtcTokenExpired() {
            super.onRtcTokenExpired()
        }

        override fun onRtcTokenPrivilegeWillExpire(token: String?) {
            super.onRtcTokenPrivilegeWillExpire(token)
        }

        override fun onUserJoined(uid: String?, elapsed: Int) {
            super.onUserJoined(uid, elapsed)
        }

        override fun onUserLeave(uid: String?, reason: Int) {
            super.onUserLeave(uid, reason)
        }

        override fun onMemberJoined(var1: RtmChannelMember?) {
            super.onMemberJoined(var1)
            checkOnlineUser()
        }

        override fun onMemberLeft(var1: RtmChannelMember?) {
            checkOnlineUser()
        }

        override fun onOtherClientLogin() {
            super.onOtherClientLogin()
        }

        override fun joinChannelSuccess(channel: String?, uid: String?, elapsed: Int) {
            synchronized(kit) {
                RtcManager.INSTANCE.getChannelAttributes(roomId) { attrs ->
                    if (null != attrs && attrs.isNotEmpty()) for (attr in attrs) {
                        if (attr.key == "ARKeyChannel" && attr.value.isNotBlank() && attr.lastUpdateUserId != uid) {
                            runOnUiThread {
                                if (drawing) {
                                    switchToVisitor()
                                } else {
                                    binding.whoDrawing.text =
                                        String.format("%s开始作画", attr.lastUpdateUserId)
                                    binding.startDraw.visibility = View.GONE
                                }
                            }
                            return@getChannelAttributes
                        }
                    }
                    if (attrs != null && attrs.isEmpty() && !drawing) runOnUiThread {
                        binding.whoDrawing.text = ""
                        binding.startDraw.visibility = View.VISIBLE
                    }
                }
                disconnected = false
            }
        }

        override fun onCallLocalAccept(var1: LocalInvitation?) {
            super.onCallLocalAccept(var1)
        }

        override fun onCallLocalReject(var1: LocalInvitation?) {
            super.onCallLocalReject(var1)
        }

        override fun onCallRemoteFailure(var1: RemoteInvitation?, var2: Int) {
            super.onCallRemoteFailure(var1, var2)
        }

        override fun onCallFailure(var1: LocalInvitation?, var2: Int) {
            super.onCallFailure(var1, var2)
        }
    }

    private inner class MyBoardHandler : ARBoardHandler {
        override fun onOccurError(p0: ARBoardErrorCode?, p1: String?) {
        }

        override fun onBoardInit() {
        }

        override fun connectionChangedToState(
            p0: ARBoardConnectionState?,
            p1: ARBoardConnectionChangedReason?
        ) {
        }

        override fun onAddBoard(p0: String?, p1: String?) {
        }

        override fun onDeleteBoard(p0: String?, p1: String?) {
        }

        override fun onGotoBoard(p0: String?, p1: String?) {
        }

        override fun onHistoryDataSyncCompleted() {
            kit.isDrawEnable = false
        }

        override fun onUndoStatusChanged(p0: Boolean) {
        }

        override fun onRedoStatusChanged(p0: Boolean) {
        }

        override fun onSnapshot(p0: String, p1: ARBoardSnapshotCode) {
            if (p1 == ARBoardSnapshotCode.AR_BOARD_SNAPSHOT_OK) {
                Toast.makeText(this@MainActivity, "图片保存成功", Toast.LENGTH_SHORT).show()
                return
            }
            Toast.makeText(this@MainActivity, "failed: $p1", Toast.LENGTH_SHORT).show()
        }

        override fun onScaleChanged(p0: String?, p1: Int) {
        }

        override fun onBoardReset(p0: String?, p1: String?) {
        }

        override fun onBoardClear(p0: String?, p1: String?) {
        }
    }
}