package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

enum class KeyType {
    NUMBER,
    OPERATOR,
    FUNCTION,
    ACTION,
    CLEAR
}

@Composable
fun CalcButton(
    text: String,
    type: KeyType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false,
    fontSize: Int = 20
) {
    val bgColor = when (type) {
        KeyType.NUMBER -> KeyNumberColor
        KeyType.OPERATOR -> if (isHighlighted) MathPrimaryDark else KeyOperatorColor
        KeyType.FUNCTION -> KeyFunctionColor
        KeyType.ACTION -> KeyActionColor
        KeyType.CLEAR -> KeyClearColor
    }

    val textColor = when (type) {
        KeyType.NUMBER -> KeyNumberTextColor
        KeyType.OPERATOR -> if (isHighlighted) Color.White else KeyOperatorTextColor
        KeyType.FUNCTION -> KeyFunctionTextColor
        KeyType.ACTION -> KeyActionTextColor
        KeyType.CLEAR -> KeyClearTextColor
    }

    val shape = RoundedCornerShape(16.dp)

    Surface(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .testTag("calc_btn_${text.replace(" ", "_")}"),
        shape = shape,
        color = bgColor,
        tonalElevation = if (type == KeyType.ACTION) 4.dp else 1.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = textColor,
                fontSize = fontSize.sp,
                fontWeight = if (type == KeyType.ACTION || type == KeyType.NUMBER) FontWeight.SemiBold else FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}
