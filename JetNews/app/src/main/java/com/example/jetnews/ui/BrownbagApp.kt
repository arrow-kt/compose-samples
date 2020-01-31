package com.example.jetnews.ui

import androidx.compose.Composable
import androidx.compose.Model
import androidx.compose.state
import androidx.compose.unaryPlus
import androidx.ui.core.Alignment
import androidx.ui.core.Text
import androidx.ui.core.dp
import androidx.ui.core.sp
import androidx.ui.foundation.DrawImage
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.graphics.Color
import androidx.ui.layout.Column
import androidx.ui.layout.Container
import androidx.ui.layout.Expanded
import androidx.ui.layout.HeightSpacer
import androidx.ui.layout.Row
import androidx.ui.layout.Size
import androidx.ui.layout.Spacing
import androidx.ui.layout.Stack
import androidx.ui.layout.WidthSpacer
import androidx.ui.material.Button
import androidx.ui.material.ContainedButtonStyle
import androidx.ui.material.surface.Surface
import androidx.ui.res.imageResource
import androidx.ui.text.TextStyle
import androidx.ui.text.font.FontFamily
import androidx.ui.text.font.FontWeight
import androidx.ui.tooling.preview.Preview
import com.example.jetnews.R

@Model
class CounterState(var count: Int = 0)

@Composable
fun BrownbagApp() {
    previewExample5()
}

@Composable
fun BadgeEnvelope(count: Int) {
    Envelope(count = count) {
        if (count > 0) {
            Badge(text = if (count > 10) "10+" else "$count")
        }
    }
}

@Composable
fun Envelope(count: Int, children: @Composable() () -> Unit) {
    val iconResource = when {
        count <= 0 -> R.mipmap.envelope_empty
        count in 1..8 -> R.mipmap.envelope_some
        else -> R.mipmap.envelope_full
    }
    val image = +imageResource(iconResource)
    Row(modifier = Size(256.dp, 300.dp)) {
        Stack {
            aligned(Alignment.Center) {
                Container(modifier = Expanded) {
                    DrawImage(image)
                }
            }
            aligned(Alignment.TopRight) {
                Container(alignment = Alignment.TopRight, children = children)
            }
        }
    }
}

@Composable
fun Badge(text: String) {
    Surface(shape = RoundedCornerShape(40.dp), color = Color.Red) {
        Text(
            modifier = Spacing(16.dp), text = text, style = TextStyle(
                color = Color.White, fontFamily = FontFamily("Roboto"),
                fontWeight = FontWeight.Normal,
                fontSize = 24.sp
            )
        )
    }
}

@Composable
fun AddEmailButton(counterState: CounterState) {
    Button(
        text = "Add email (${counterState.count})",
        onClick = { counterState.count++ },
        style = ContainedButtonStyle(color = if (counterState.count > 5) Color.Green else Color.White)
    )
}

@Composable
fun AddEmailButton(count: Int, countCb: (Int) -> Unit) {
    Button(
        text = "Add email (${count})",
        onClick = { countCb(count + 1) },
        style = ContainedButtonStyle(color = if (count > 5) Color.Green else Color.White)
    )
}

@Composable
fun DeleteEmailButton(counterState: CounterState) {
    Button(
        text = "Delete email",
        onClick = { if (counterState.count > 0) counterState.count-- },
        style = ContainedButtonStyle(color = Color.Red)
    )
}

@Composable
fun DeleteEmailButton(count: Int, countCb: (Int) -> Unit) {
    Button(
        text = "Delete email",
        onClick = { if (count > 0) countCb(count - 1) },
        style = ContainedButtonStyle(color = Color.Red)
    )
}

@Composable
fun Greeting(name: String) {
    Text("Hello $name")
}

@Preview("Greeting")
@Composable
private fun previewExample1() {
    Greeting(name = "47 Degrees!")
}

@Preview("Multi Greeting")
@Composable
private fun previewExample2() {
    Greeting(name = "47 Degrees!")
    Greeting(name = "San Fernando!")
    Greeting(name = "Madrid!")
    Greeting(name = "London!")
    Greeting(name = "Seattle!")
}

@Preview("Multi Greeting fixed")
@Composable
private fun previewExample3() {
    Column(modifier = Spacing(16.dp)) {
        Greeting(name = "47 Degrees!")
        Greeting(name = "San Fernando!")
        Greeting(name = "Madrid!")
        Greeting(name = "London!")
        Greeting(name = "Seattle!")
    }
}

//@Preview("envelope example4")
@Composable
private fun previewExample4() {
    val counterState = CounterState()
    Column(modifier = Spacing(16.dp)) {
        BadgeEnvelope(counterState.count)
        HeightSpacer(16.dp)
        Row {
            AddEmailButton(counterState)
            WidthSpacer(16.dp)
            DeleteEmailButton(counterState)
        }
    }
}

@Preview("envelope example5")
@Composable
private fun previewExample5() {
    val (counterState, counterStateCb) = +state { 11 }
    Column(modifier = Spacing(16.dp)) {
        BadgeEnvelope(counterState)
        HeightSpacer(16.dp)
        Row {
            AddEmailButton(counterState, counterStateCb)
            WidthSpacer(16.dp)
            DeleteEmailButton(counterState, counterStateCb)
        }
    }
}




















