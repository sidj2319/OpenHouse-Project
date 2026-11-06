package com.example.catalogue.ui.enquiry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.catalogue.ui.common.CatalogueTopBar

@Composable
fun EnquiryScreen(
    viewModel: EnquiryViewModel,
    onBack: () -> Unit,
    onDone: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(topBar = { CatalogueTopBar("Enquire", onBack) }) { padding ->
        if (state.submitted) {
            ThankYou(onDone, Modifier.padding(padding))
        } else {
            Column(
                Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                state.productTitle?.let {
                    Text("Enquiring about: $it", style = MaterialTheme.typography.titleMedium)
                }
                FormField(
                    value = state.name,
                    onValueChange = viewModel::onNameChange,
                    label = "Name",
                    error = state.nameError,
                )
                FormField(
                    value = state.email,
                    onValueChange = viewModel::onEmailChange,
                    label = "Email",
                    error = state.emailError,
                    keyboardType = KeyboardType.Email,
                )
                FormField(
                    value = state.phone,
                    onValueChange = viewModel::onPhoneChange,
                    label = "Phone (optional)",
                    error = state.phoneError,
                    keyboardType = KeyboardType.Phone,
                )
                FormField(
                    value = state.message,
                    onValueChange = viewModel::onMessageChange,
                    label = "Message (optional)",
                    error = state.messageError,
                    singleLine = false,
                    minLines = 3,
                )
                state.submitError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
                Button(
                    onClick = viewModel::submit,
                    enabled = !state.submitting,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (state.submitting) {
                        CircularProgressIndicator(Modifier.height(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Send enquiry")
                    }
                }
            }
        }
    }
}

@Composable
private fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        singleLine = singleLine,
        minLines = minLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun ThankYou(onDone: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Thank you!", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "We've received your enquiry and our sales team will be in touch soon.",
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onDone) { Text("Back to catalogue") }
    }
}
