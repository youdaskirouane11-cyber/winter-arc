package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.ProgressPoint

@Composable
fun InteractiveTrendLineChart(
    points: List<ProgressPoint>,
    lineColor: Color,
    modifier: Modifier = Modifier,
    unitSuffix: String = "%"
) {
    if (points.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(140.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No trend data available yet.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val maxVal = (points.maxOfOrNull { it.score } ?: 100).coerceAtLeast(10).toFloat()
    val minVal = 0f

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)

            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val paddingBottom = 16.dp.toPx()
                val paddingTop = 12.dp.toPx()
                val availableHeight = height - paddingTop - paddingBottom

                // Grid lines (3 horizontal lines)
                for (i in 0..2) {
                    val y = paddingTop + (availableHeight / 2) * i
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                if (points.size >= 2) {
                    val stepX = width / (points.size - 1)
                    val path = Path()
                    val fillPath = Path()

                    val coordinates = points.mapIndexed { index, p ->
                        val x = index * stepX
                        val normalized = ((p.score - minVal) / (maxVal - minVal)).coerceIn(0f, 1f)
                        val y = paddingTop + availableHeight * (1f - normalized)
                        Offset(x, y)
                    }

                    path.moveTo(coordinates[0].x, coordinates[0].y)
                    fillPath.moveTo(coordinates[0].x, height - paddingBottom)
                    fillPath.lineTo(coordinates[0].x, coordinates[0].y)

                    for (i in 1 until coordinates.size) {
                        val prev = coordinates[i - 1]
                        val curr = coordinates[i]
                        // Smooth cubic bezier or clean lines
                        val midX = (prev.x + curr.x) / 2
                        path.cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                        fillPath.cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                    }

                    fillPath.lineTo(coordinates.last().x, height - paddingBottom)
                    fillPath.close()

                    // Draw area gradient
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                lineColor.copy(alpha = 0.35f),
                                lineColor.copy(alpha = 0.0f)
                            ),
                            startY = paddingTop,
                            endY = height - paddingBottom
                        )
                    )

                    // Draw line
                    drawPath(
                        path = path,
                        color = lineColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw dots on each point
                    coordinates.forEach { pt ->
                        drawCircle(
                            color = lineColor,
                            radius = 4.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 2.dp.toPx(),
                            center = pt
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // X-Axis labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            points.forEach { pt ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = pt.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${pt.score}$unitSuffix",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
