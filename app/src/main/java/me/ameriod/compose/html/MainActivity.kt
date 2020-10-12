package me.ameriod.compose.html

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Text
import androidx.compose.foundation.currentTextStyle
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.unit.dp
import me.ameriod.compose.html.ui.ComposeHtmlTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeHtmlTheme {
                LazyColumnFor(items = getHtmlFilesFromAssets()) {
                    Text(
                        modifier = Modifier.padding(16.dp),
                        text = it.htmlToSpannableString(currentTextStyle())
                    )

                    Divider()
                }
            }
        }
    }

    private fun getHtmlFilesFromAssets(): List<String> =
        application.assets.list("")
            ?.filter { file ->
                file.endsWith(".html")
            }
            ?.map { file ->
                application.assets.open(file).bufferedReader().use {
                    it.readText()
                }
            } ?: emptyList()
}