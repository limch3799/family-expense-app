package com.moaga.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.moaga.app.R

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)

val moaga_one_regular = FontFamily(Font(R.font.moaga_one_regular))
val moaga_one_bold = FontFamily(Font(R.font.moaga_one_bold))
val moaga_two_regular = FontFamily(Font(R.font.moaga_two_regular))
val moaga_two_bold= FontFamily(Font(R.font.moaga_two_bold))


val moaga_primary_light = FontFamily(Font(R.font.moaga_primary_light))
val moaga_primary_medium = FontFamily(Font(R.font.moaga_primary_medium))
val moaga_primary_bold = FontFamily(Font(R.font.moaga_primary_bold))

val font_paperlogy_1 = FontFamily(Font(R.font.font_paperlogy_1))
val font_paperlogy_2 = FontFamily(Font(R.font.font_paperlogy_2))
val font_paperlogy_3 = FontFamily(Font(R.font.font_paperlogy_3))
val font_paperlogy_4 = FontFamily(Font(R.font.font_paperlogy_4))
val font_paperlogy_5 = FontFamily(Font(R.font.font_paperlogy_5))
val font_paperlogy_6 = FontFamily(Font(R.font.font_paperlogy_6))
val font_paperlogy_7 = FontFamily(Font(R.font.font_paperlogy_7))
val font_paperlogy_8 = FontFamily(Font(R.font.font_paperlogy_8))
val font_paperlogy_9 = FontFamily(Font(R.font.font_paperlogy_9))

val font_sketch = FontFamily(Font(R.font.font_sketch))


val font_gothic_1 = FontFamily(Font(R.font.font_gothic_1))
val font_gothic_2 = FontFamily(Font(R.font.font_gothic_2))
val font_gothic_3 = FontFamily(Font(R.font.font_gothic_3))
val font_gothic_4 = FontFamily(Font(R.font.font_gothic_4))
val font_gothic_5 = FontFamily(Font(R.font.font_gothic_5))
