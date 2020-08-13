package online.vapcom.dashboard.ui

import android.animation.ValueAnimator
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import online.vapcom.dashboard.DashboardApplication
import online.vapcom.dashboard.R
import online.vapcom.dashboard.databinding.ActivityDashboardBinding
import online.vapcom.dashboard.viewmodels.DashboardViewModel

private const val TAG = "DashBrdAct"

/**
 * Активити для виджетов спидометра и тахометра.
 * Приборы переключаются свайпом двумя пальцами.
 */
class DashboardActivity : AppCompatActivity() {

    private lateinit var viewModel: DashboardViewModel
    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "-- CREATE")
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) // оставляем экран всегда включенным

        val repo = (applicationContext as DashboardApplication).sensorsRepository
        viewModel = ViewModelProvider(this, DashboardViewModel.FACTORY(repo)).get(DashboardViewModel::class.java)
        subscribeUI()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_dashboard)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
    }

    private fun subscribeUI() {
        // здесь могла бы быть подписка на viewModel.state, например, для обработки ошибок получения данных.
        // Но в нашем простом случае её нет, все данные в виджеты идут через байдинг.
    }

    override fun onResume() {
        super.onResume()
        scrollAnimation.apply { if(isStarted && isPaused) resume() }

        viewModel.startSensorsDisplay()
    }

    override fun onPause() {
        super.onPause()
        scrollAnimation.apply { if(isStarted) pause() }
    }

    override fun onDestroy() {
        super.onDestroy()
        scrollAnimation.cancel()
    }


    override fun onBackPressed() {
        Log.i(TAG, "-- ON BACK PRESSED")

        // игнорируем железную кнопку Назад
        //super.onBackPressed()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    /**
     * Включает полноэкранный immersive режим.
     *
     * Подробности здесь: https://developer.android.com/training/system-ui/immersive
     */
    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }


    var doScroll = false
    var prevScrollX = 0F
    var instrumentWidth = 0F


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if(event == null) return false

        Log.i(TAG, "-- ON TOUCH: event: $event")

        when(event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                scrollAnimation.cancel()
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                val index = event.actionIndex
                if (index == 1) { // второй палец, начинаем скролл
                    doScroll = true
                    prevScrollX = event.getX(event.getPointerId(index))
                    instrumentWidth = binding.speed.width.toFloat()

                    Log.w(TAG, "-- ON TOUCH: START SCROLL: x: $prevScrollX, instWidth: $instrumentWidth")

                    return true
                }
            }

            MotionEvent.ACTION_POINTER_UP -> {
                val index = event.actionIndex
                if (index == 0) {
                    // второй палец отпущен, останавливаем скролл
                    Log.w(TAG, "-- ON TOUCH: STOP SCROLL")
                    doScroll = false
                    return true
                }
            }

            MotionEvent.ACTION_UP -> {
                Log.w(TAG, "-- ON TOUCH: UP")
                startScrollAnimation()
            }

            MotionEvent.ACTION_MOVE -> {
                if(doScroll && event.pointerCount == 2 ) {  // скроллируем только двумя пальцами
                    val currentX = event.getX(event.getPointerId(1))
                    val deltaX = currentX - prevScrollX

                    Log.w(TAG, "-- ON TOUCH: MOVE: curr: $currentX, prev: $prevScrollX, delta: $deltaX")
                    moveInstruments(deltaX)

                    prevScrollX = currentX
                    return true
                }
            }

            else -> {}
        }

        return false
    }

    private fun moveInstruments(deltaX: Float) {
        binding.speed.moveOnDeltaX(deltaX)
        binding.tachometer.moveOnDeltaX(deltaX)
        constrainInstrumentsX()
    }

    private val maxScroll = 0.75F   // при скролле приборы перекрываем на 25%

    /**
     * Ограничение горизонтального скролла приборов
     */
    private fun constrainInstrumentsX() {

        // ограничение скролла спидометра
        binding.speed.apply {
            val leftBorder = -instrumentWidth * maxScroll
            val sx = x
            if(sx < leftBorder) x = leftBorder
            else if(sx > 0F) x = 0F

            alpha = if(sx < 0F) 1 - sx/leftBorder else 1F
            visibility = if(alpha < 0.05) View.INVISIBLE else View.VISIBLE
        }

        // ограничение скролла тахометра
        binding.tachometer.apply {
            val rightBorder = instrumentWidth * maxScroll
            val tx = x
            if (tx > rightBorder) x = rightBorder
            else if (tx < 0F) x = 0F

            alpha = if (tx > 0F) 1 - tx / rightBorder else 1F
            visibility = if(alpha < 0.05) View.INVISIBLE else View.VISIBLE
        }

    }

    // анимация сдвига прибора для его выравнивания по краю экрана после скролла
    private val scrollAnimation: ValueAnimator = ValueAnimator.ofFloat().apply {
        duration = 200
        addUpdateListener { anim ->
            Log.i(TAG, "-- ANIMATION: ${anim.animatedValue}")

            val x: Float = anim.animatedValue as Float
            binding.speed.x = x
            binding.tachometer.x = x + instrumentWidth * maxScroll

            constrainInstrumentsX()
        }
    }

    /**
     * Запускает анимацию завершения скролла
     */
    private fun startScrollAnimation() {
        val leftBorder = -instrumentWidth * maxScroll

        val sx = binding.speed.x

        if(sx > leftBorder/2)
            scrollAnimation.setFloatValues(sx, 0F)
        else scrollAnimation.setFloatValues(sx, leftBorder)

        scrollAnimation.start()
    }

}