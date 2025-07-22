package com.refactoringlife.lizimportadosadmin.core.composablesLipsy

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import com.refactoringlife.lizimportadosadmin.core.utils.ProductConstants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LipsyDropdown(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    
    // Determinar si es la opción por defecto
    val isDefaultOption = selectedOption == ProductConstants.SELECT_OPTION

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
                .focusRequester(focusRequester),
            enabled = enabled,
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                unfocusedTextColor = if (isDefaultOption) Color.Gray else Color.Unspecified,
                focusedTextColor = if (isDefaultOption) Color.Gray else Color.Unspecified
            ),
            isError = isDefaultOption
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { 
                        Text(
                            text = option,
                            color = if (option == ProductConstants.SELECT_OPTION) Color.Gray else Color.Unspecified
                        )
                    },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
} 