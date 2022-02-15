package io.anyrtc.drawsomething.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gyf.immersionbar.ImmersionBar
import io.anyrtc.drawsomething.R
import io.anyrtc.drawsomething.utils.ScreenUtils

open class BaseActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
	    ScreenUtils.adapterScreen(this, 375, adapterScreenVertical())
        super.onCreate(savedInstanceState)
        ImmersionBar.with(this).fitsSystemWindows(true).statusBarColor(statusBarColor()).statusBarDarkFont(
            statusBarColor() == R.color.white
        ).navigationBarColor(statusBarColor(), 0.2f).navigationBarDarkIcon(statusBarColor() == R.color.white).init()
	}

	protected open fun statusBarColor() = R.color.white
    protected open fun adapterScreenVertical() = false
    protected open fun targetDP() = 375

    fun changeScreenAdapter(isVertical: Boolean, dpi: Int) {
        ScreenUtils.resetScreen(this)
        ScreenUtils.adapterScreen(this, dpi, !isVertical)
    }

    override fun onDestroy() {
        ScreenUtils.resetScreen(this)
        super.onDestroy()
    }
}
