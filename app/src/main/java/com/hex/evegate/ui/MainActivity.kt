package com.hex.evegate.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.hex.evegate.AppEx
import com.hex.evegate.R
import com.hex.evegate.api.StationApi
import com.hex.evegate.api.dto.NowPlayingDto
import com.hex.evegate.net.RetrofitClient
import com.hex.evegate.radio.PlaybackStatus
import com.hex.evegate.radio.RadioManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import retrofit2.Response
import retrofit2.Retrofit

class MainActivity : AppCompatActivity(),NavigationView.OnNavigationItemSelectedListener {

    lateinit var tvCount: TextView
    lateinit var tvSongName: TextView
    lateinit var ivImage: ImageView
    lateinit var ivBackground: ImageView
    lateinit var chbHQ: CheckBox
    lateinit var ibPlayPause: ImageButton
    private lateinit var dlDrawer: DrawerLayout
    private lateinit var nvMenu: NavigationView

    private lateinit var radioManager: RadioManager

    private lateinit var streamURL: String
    private var lastBackPressTime: Long = 0

    private var compositeDisposable: CompositeDisposable? = null
    private var retrofit: Retrofit? = null
    private var stationApi: StationApi? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initialize()
        configNet()
        getNowPlayingDto()
    }

    private fun initialize() {
        setContentView(R.layout.activity_main)

        radioManager = RadioManager.with(this)
        tvCount = findViewById(R.id.tvCount)
        tvSongName = findViewById(R.id.tvSongName)
        ivImage = findViewById(R.id.ivImage)
        ivBackground = findViewById(R.id.ivBackground)
        dlDrawer = findViewById(R.id.dlDrawer)
        nvMenu = findViewById(R.id.nvMenu)
        val toggle = ActionBarDrawerToggle(
                this, dlDrawer, findViewById(R.id.toolbar), R.string.on, R.string.off)
        dlDrawer.addDrawerListener(toggle)
        toggle.syncState()
        nvMenu.setNavigationItemSelectedListener(this)

        chbHQ = findViewById(R.id.chbHQ)
        chbHQ.isChecked = AppEx.instance!!.shpHQ
        streamURL = if (AppEx.instance!!.shpHQ) { resources.getString(R.string.evegateradio_high)
        } else { resources.getString(R.string.evegateradio_low) }
        chbHQ.setOnCheckedChangeListener { buttonView, isChecked ->
            AppEx.instance!!.shpHQ = isChecked
            streamURL = if (isChecked) { resources.getString(R.string.evegateradio_high)
            } else { resources.getString(R.string.evegateradio_low) }
        }

        ibPlayPause = findViewById(R.id.ibPlayPause)
        ibPlayPause.setOnClickListener { v ->
            if (!TextUtils.isEmpty(streamURL))
                radioManager.playOrPause(streamURL)
        }
    }

    private fun getNowPlayingDto() {
        compositeDisposable!!.add(stationApi!!.nowPlaying()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleNowPlayingResponse, this::handleNowPlayingError)
        )
    }

    private fun handleNowPlayingResponse(result: Response<NowPlayingDto>) {
        if (result.isSuccessful) {
            if (result.body() != null) {
                tvCount.text = result.body()!!.listeners.total
                tvSongName.text = result.body()!!.now_playing.song.text
                try {
                    Glide.with(this).load(result.body()!!.now_playing.song.art)
                            .into(ivBackground)
                } catch (e: Exception) {
                    /*ignored*/
                }
            }
        } else { Toast.makeText(this, "Ашипко!", Toast.LENGTH_SHORT).show() }
    }

    private fun handleNowPlayingError(error: Throwable) {
        Toast.makeText(this, "Ашипко! ${error.message}", Toast.LENGTH_SHORT).show()
    }

    private fun configNet() {
        compositeDisposable = CompositeDisposable()
        retrofit = RetrofitClient.getInstance()
        stationApi = retrofit!!.create(StationApi::class.java)
    }

    public override fun onStart() {

        super.onStart()

        EventBus.getDefault().register(this)
    }

    public override fun onStop() {

        EventBus.getDefault().unregister(this)

        super.onStop()
    }

    override fun onDestroy() {

        radioManager.unbind()
        compositeDisposable!!.clear()

        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()

        radioManager.bind()
    }

    override fun onBackPressed() {
        if (dlDrawer.isDrawerOpen(GravityCompat.START)) {
            dlDrawer.closeDrawer(GravityCompat.START)
        } else {
            val now = System.currentTimeMillis()
            if (System.currentTimeMillis() - lastBackPressTime < 1000) {
                finish()
            } else {
                lastBackPressTime = now
                val toast = Toast.makeText(this@MainActivity, "Нажмите \"Назад\" еще раз для выхода их приложения.\nДля продолжения прослушивания в фоновом режиме нажмите \"Свернуть\"", Toast.LENGTH_LONG)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
            }
        }

    }

    @Subscribe
    fun onEvent(status: String) {

        when (status) {

            PlaybackStatus.LOADING -> {
            }

            PlaybackStatus.ERROR ->

                Toast.makeText(this, R.string.no_stream, Toast.LENGTH_SHORT).show()
        }

        ibPlayPause.setImageResource(if (status == PlaybackStatus.PLAYING)
            android.R.drawable.ic_media_pause
        else
            android.R.drawable.ic_media_play)

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.history -> startActivity(Intent(this, HistoryActivity::class.java))
            R.id.community -> startActivity(Intent(this, CommsActivity::class.java))
        }
        dlDrawer.closeDrawer(GravityCompat.START)
        return true
    }
}
