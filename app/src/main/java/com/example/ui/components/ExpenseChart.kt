package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MarketExpense
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate600
import com.example.ui.util.AppLanguage
import com.example.ui.util.BanglaFormatter
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

data class DayExpenseBar(
    val isoDate: String,
    val dayLabel: String,
    val amount: Double
)

@Composable
fun ExpenseTrendCard(
    expenses: List<MarketExpense>,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val isBn = language == AppLanguage.BANGLA
    val today = remember { LocalDate.now(BanglaFormatter.DHAKA_ZONE) }

    // Prepare last 7 days
    val barsData = remember(expenses, language) {
        val list = mutableListOf<DayExpenseBar>()
        val bnDayNames = mapOf(
            1 to "সোম", 2 to "মঙ্গল", 3 to "বুধ", 4 to "বৃহঃ", 5 to "শুক্র", 6 to "শনি", 7 to "রবি"
        )
        for (i in 6 downTo 0) {
            val date = today.minusDays(i.toLong())
            val iso = date.toString()
            val dayOfWeekVal = date.dayOfWeek.value
            val label = if (isBn) {
                bnDayNames[dayOfWeekVal] ?: ""
            } else {
                date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US)
            }
            val totalForDay = expenses.filter { it.date == iso }.sumOf { it.amount }
            list.add(DayExpenseBar(iso, label, totalForDay))
        }
        list
    }

    val maxAmount = remember(barsData) {
        val max = barsData.maxOfOrNull { it.amount } ?: 0.0
        if (max <= 0.0) 100.0 else max
    }

    // Total for the 7 days
    val total7Days = remember(barsData) { barsData.sumOf { it.amount } }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBn) "গত ৭ দিনের বাজার খরচ" else "Last 7 Days Market Trend",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isBn) "মোট ${BanglaFormatter.formatCurrency(total7Days, true)}" else "Total ${BanglaFormatter.formatCurrency(total7Days, false)}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = Slate600
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Custom Bar Chart with values on top
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Amounts above bars
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    for (item in barsData) {
                        Text(
                            text = BanglaFormatter.toBanglaDigits(item.amount.toInt().toString()),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = Slate600
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(95.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val barCount = barsData.size
                    val spacing = width / barCount
                    val barWidth = spacing * 0.42f
                    val baselineY = height - 2f

                    // Draw subtle baseline
                    drawLine(
                        color = Slate200,
                        start = Offset(0f, baselineY),
                        end = Offset(width, baselineY),
                        strokeWidth = 1.5f
                    )

                    for ((index, item) in barsData.withIndex()) {
                        val centerX = index * spacing + spacing / 2f
                        val left = centerX - barWidth / 2f
                        val isToday = index == barCount - 1
                        val barHeight = ((item.amount / maxAmount) * (baselineY - 10f)).toFloat()
                        val top = baselineY - (if (barHeight < 4f) 4f else barHeight)

                        // Highlight today with darker emerald, past days with soft mint
                        val barColor = if (isToday) {
                            Color(0xFF047857)
                        } else {
                            if (item.amount > 0) Color(0xFF6EE7B7) else Slate200
                        }

                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset(left, top),
                            size = Size(barWidth, baselineY - top),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Day Labels cleanly spaced below baseline without clipping
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    for ((index, item) in barsData.withIndex()) {
                        val isToday = index == barsData.size - 1
                        val displayLabel = if (isToday) (if (isBn) "আজ" else "Today") else item.dayLabel
                        Text(
                            text = displayLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isToday) Color(0xFF047857) else Slate600
                        )
                    }
                }
            }
        }
    }
}
