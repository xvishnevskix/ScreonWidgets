package videoTrade.screonPlayer.app.presentation.screens.about

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator

actual class AboutScreen actual constructor() : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val ctx = LocalContext.current
        val scroll = rememberScrollState()

        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Bottom
            ) {


                Text("←", fontSize = 30.sp, modifier = Modifier.align(Alignment.CenterVertically) .clip(RoundedCornerShape(8.dp)) .clickable{ navigator?.pop() })
                Spacer(modifier = Modifier.width(12.dp))
                Text("О приложении")
            }

            Column(
                modifier = Modifier

                    .fillMaxSize()
                    .verticalScroll(scroll)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Информация о продукте
                Card {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Screon", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)

                        Divider(Modifier.padding(vertical = 8.dp))

                        Text("© 2024–2025 ООО «Центр Дистрибьюции» (LLC \"Centr Distribyucii\").")
                        Text("Все права защищены.")
                        Text("Продукт: ПО «Screon» (плеер, админ-панель и компоненты), документация и все входящие материалы.")
                        Text("Правообладатель: ООО «Центр Дистрибьюции».")
                        TextButton(
                            onClick = {
                                ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("mailto:Complaint@screon.ru")))
                            }
                        ) {
                            Text("Почта: Complaint@screon.ru", textDecoration = TextDecoration.Underline)
                        }
                    }
                }

                // Лицензии
                Card {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Лицензии третьих лиц", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

                        Text(
                            "Это приложение использует libVLC for Android " +
                                    "(org.videolan.android:libvlc-all:3.6.2), " +
                                    "© 1996–2025 VideoLAN and contributors. Лицензия: GNU LGPL v2.1-or-later. " +
                                    "Библиотека подключена как разделяемая (.so)."
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = { navigator?.push(
                                    LicenseViewerScreen(
                                        "LGPL v2.1",
                                        "licenses/lgpl-2.1.txt"
                                    )
                                ) }
                            ) { Text("Текст LGPL v2.1") }

                            OutlinedButton(
                                onClick = { navigator?.push(
                                    LicenseViewerScreen(
                                        "Third-party notices",
                                        "licenses/third-party-notices.txt"
                                    )
                                ) }
                            ) { Text("Сторонние компоненты") }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))


                Button(
                    onClick = { navigator?.pop() },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .height(46.dp)
                        .fillMaxWidth(0.54f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8CA068),
                        contentColor   = Color(0xFF0F120A),
                        disabledContainerColor = Color(0x668CA068),
                        disabledContentColor   = Color(0xFF2B2E25)
                    )
                ) {
                    Text("Вернуться в плеер")
                }
            }
        }
    }
}
