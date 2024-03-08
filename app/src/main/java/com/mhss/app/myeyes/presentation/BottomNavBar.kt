package com.mhss.app.myeyes.presentation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.mhss.app.myeyes.CURRENCY_DETECTOR
import com.mhss.app.myeyes.OBJECT_DETECTOR
import com.mhss.app.myeyes.R
import com.mhss.app.myeyes.TEXT_DETECTOR

@Composable
fun BottomNavBar(selectedOption: Int, onOptionSelected: (Int) -> Unit) {
    NavigationBar {
        NavigationBarItem(
            selected = selectedOption == OBJECT_DETECTOR,
            onClick = { onOptionSelected(OBJECT_DETECTOR) },
            label = { Text("Discover") },
            icon = {
                Icon(
                    painterResource(id = R.drawable.ic_radar),
                    contentDescription = null
                )
            },
        )
        NavigationBarItem(
            selected = selectedOption == TEXT_DETECTOR,
            onClick = { onOptionSelected(TEXT_DETECTOR) },
            label = { Text("Text") },
            icon = {
                Icon(
                    painterResource(id = R.drawable.ic_abc),
                    contentDescription = null
                )
            },
        )
        NavigationBarItem(
            selected = selectedOption == CURRENCY_DETECTOR,
            onClick = { onOptionSelected(CURRENCY_DETECTOR) },
            label = { Text("Money") },
            icon = {
                Icon(
                    painterResource(id = R.drawable.ic_wallet),
                    contentDescription = null
                )
            },
        )

    }
}