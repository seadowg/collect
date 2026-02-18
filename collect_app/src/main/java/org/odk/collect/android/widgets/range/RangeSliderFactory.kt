package org.odk.collect.android.widgets.range

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun RangeSliderFactory(
    sliderState: RangeSliderState,
    onValueChanging: (Boolean) -> Unit,
    onValueChangeFinished: (RangeSliderState) -> Unit,
    onRangeInvalid: () -> Unit
) {
    var value by remember(sliderState.sliderValue) { mutableStateOf(sliderState.sliderValue) }

    LaunchedEffect(Unit) {
        if (!sliderState.isValid) {
            onRangeInvalid()
        }
    }

    Surface {
        if (sliderState.isHorizontal) {
            HorizontalRangeSlider(
                value = value,
                valueLabel = sliderState.valueLabel,
                steps = sliderState.numOfSteps,
                ticks = sliderState.numOfTicks,
                enabled = sliderState.isEnabled,
                startLabel = sliderState.startLabel,
                endLabel = sliderState.endLabel,
                onValueChanging = onValueChanging,
                onValueChange = {
                    value = it
                },
                onValueChangeFinished = {
                    onValueChangeFinished(sliderState.copy(sliderValue = value))
                }
            )
        } else {
            VerticalRangeSlider(
                value = value,
                steps = sliderState.numOfSteps,
                enabled = sliderState.isEnabled,
                valueLabel = sliderState.valueLabel,
                startLabel = sliderState.startLabel,
                endLabel = sliderState.endLabel,
                ticks = sliderState.numOfTicks,
                onValueChanging = onValueChanging,
                onValueChangeFinished = {
                    onValueChangeFinished(sliderState.copy(sliderValue = value))
                },
                onValueChange = {
                    value = it
                }
            )
        }
    }
}
