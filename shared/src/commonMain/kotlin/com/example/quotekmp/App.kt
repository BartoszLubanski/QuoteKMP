package com.example.quotekmp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.quotekmp.viewmodel.QuoteViewModel
import org.koin.compose.koinInject

@Composable
@Preview
fun App() {
    MaterialTheme {
        val viewModel = koinInject<QuoteViewModel>()
        val quote by viewModel.uiState.collectAsState()

        Column(
            modifier = Modifier.fillMaxSize().safeContentPadding().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val currentState = quote
            if (currentState == null) {
                Text("Loading...")
            } else {
                Text(text = "\"${currentState.quote.quote}\"")
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "— ${currentState.quote.author}", fontStyle = FontStyle.Italic)
                if (currentState.isFromCache) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Offline · showing cached quote",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = { viewModel.refresh() }) {
                Text("New quote")
            }
        }
    }
}