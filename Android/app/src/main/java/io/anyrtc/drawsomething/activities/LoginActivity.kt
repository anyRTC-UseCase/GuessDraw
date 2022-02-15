package io.anyrtc.drawsomething.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.widget.doOnTextChanged
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import io.anyrtc.drawsomething.databinding.ActivityLoginBinding
import io.anyrtc.drawsomething.utils.MyConstants
import io.anyrtc.drawsomething.utils.SpUtil
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class LoginActivity : BaseActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        reqPermission()

        var uid = SpUtil.get().getString(MyConstants.UID, "")!!
        if (uid.isBlank()) {
            uid = Random.nextInt(100000, 999999).toString()
            SpUtil.get().edit {
                putString(MyConstants.UID, uid)
            }
        }

        binding.roomId.doOnTextChanged { _, _, _, _ ->
            binding.joinRoom.isEnabled = binding.roomId.text?.toString()?.length ?: 0 > 0
        }
        binding.joinRoom.setOnClickListener {
            val roomId = binding.roomId.text.toString()
            if (roomId.isEmpty()) {
                return@setOnClickListener
            }
            val intent = Intent(it.context, MainActivity::class.java)
            intent.putExtra("uid", uid)
            intent.putExtra("roomId", roomId)
            startActivity(intent)
            finish()
        }
    }

    private fun reqPermission() {
        XXPermissions.with(this).permission(
            Permission.RECORD_AUDIO,
            Permission.MANAGE_EXTERNAL_STORAGE,
            Permission.CAMERA
        ).request { _, all ->
            if (!all) {
                Toast.makeText(this, "请开启权限", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}