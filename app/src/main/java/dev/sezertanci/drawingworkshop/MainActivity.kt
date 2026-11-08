package dev.sezertanci.drawingworkshop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import dagger.hilt.android.AndroidEntryPoint
import dev.sezertanci.drawingworkshop.domain.sound.SoundPlayer
import dev.sezertanci.drawingworkshop.ui.components.LocalSoundPlayer
import dev.sezertanci.drawingworkshop.ui.navigation.AppNavHost
import dev.sezertanci.drawingworkshop.ui.theme.DrawingWorkshopTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var soundPlayer: SoundPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        hideSystemBars()
        setContent {
            DrawingWorkshopTheme {
                CompositionLocalProvider(LocalSoundPlayer provides soundPlayer) { AppNavHost() }
            }
        }
    }

    /** Tam ekran: sistem çubukları gizlenir, kenardan kaydırınca geçici olarak görünür. */
    private fun hideSystemBars() {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
    }
}
