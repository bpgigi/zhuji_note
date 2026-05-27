package com.zhuji.note.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val ZhujiShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

object ZhujiCornerTokens {
    val Chip = RoundedCornerShape(999.dp)
    val ButtonPrimary = RoundedCornerShape(14.dp)
    val NoteCard = RoundedCornerShape(20.dp)
    val Sheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
    val Dialog = RoundedCornerShape(24.dp)
    val Input = RoundedCornerShape(12.dp)
    val FAB = RoundedCornerShape(18.dp)
    val Tag = RoundedCornerShape(10.dp)
}
