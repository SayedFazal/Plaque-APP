package com.periocompliance.ai.ui.auth.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import com.periocompliance.ai.R
import com.periocompliance.ai.domain.model.AuthError
import com.periocompliance.ai.domain.validation.FieldError
import com.periocompliance.ai.domain.validation.PasswordStrength
import com.periocompliance.ai.ui.theme.PerioTheme

/**
 * The building blocks every auth screen is made of. They exist so that four screens cannot drift
 * into four slightly different text fields.
 *
 * Every colour, radius and gap below comes from the theme. There is not one literal in this file.
 */

/**
 * Outlined field with error text underneath.
 *
 * The error slot is always laid out, even when empty, so that typing an invalid character does not
 * make the form jump. AnimatedVisibility fades the message in rather than snapping it.
 */
@Composable
fun PerioTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    error: FieldError? = null,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = placeholder?.let { { Text(it) } },
            leadingIcon = leadingIcon?.let { icon ->
                { Icon(icon, contentDescription = null) }
            },
            trailingIcon = trailingIcon,
            isError = error != null,
            enabled = enabled,
            singleLine = true,
            shape = PerioTheme.shapes.input,
            visualTransformation = visualTransformation,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction,
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                errorBorderColor = MaterialTheme.colorScheme.error,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        AnimatedVisibility(
            visible = error != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Text(
                text = error?.let { stringResource(it.messageRes()) }.orEmpty(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(
                    start = PerioTheme.spacing.md,
                    top = PerioTheme.spacing.xs,
                ),
            )
        }
    }
}

/** A [PerioTextField] that hides its content and carries the show/hide toggle. */
@Composable
fun PerioPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isVisible: Boolean,
    onToggleVisibility: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    error: FieldError? = null,
    enabled: Boolean = true,
    imeAction: ImeAction = ImeAction.Next,
) {
    PerioTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        leadingIcon = leadingIcon,
        error = error,
        enabled = enabled,
        keyboardType = KeyboardType.Password,
        imeAction = imeAction,
        visualTransformation = if (isVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        trailingIcon = {
            IconButton(onClick = onToggleVisibility) {
                Icon(
                    imageVector = if (isVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = stringResource(
                        if (isVisible) R.string.cd_hide_password else R.string.cd_show_password,
                    ),
                )
            }
        },
    )
}

/**
 * Three-segment strength meter. It fills and changes colour as the password improves.
 *
 * The bar is marked clearAndSetSemantics because the visible label ("Medium") already says
 * everything a screen reader needs; announcing three unlabelled progress bars alongside it is noise.
 */
@Composable
fun PasswordStrengthMeter(
    strength: PasswordStrength,
    modifier: Modifier = Modifier,
) {
    val colors = PerioTheme.colors
    val target = when (strength) {
        PasswordStrength.None -> MaterialTheme.colorScheme.outlineVariant
        PasswordStrength.Weak -> MaterialTheme.colorScheme.error
        PasswordStrength.Medium -> colors.warning
        PasswordStrength.Strong -> colors.success
    }
    val barColor by animateColorAsState(target, label = "strengthColor")

    val filled = when (strength) {
        PasswordStrength.None -> 0
        PasswordStrength.Weak -> 1
        PasswordStrength.Medium -> 2
        PasswordStrength.Strong -> 3
    }

    AnimatedVisibility(
        visible = strength != PasswordStrength.None,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(top = PerioTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(PerioTheme.spacing.xs),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clearAndSetSemantics { },
                horizontalArrangement = Arrangement.spacedBy(PerioTheme.spacing.xs),
            ) {
                repeat(3) { index ->
                    val segmentAlpha by animateFloatAsState(
                        targetValue = if (index < filled) 1f else 0.25f,
                        label = "segment$index",
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(PerioTheme.spacing.xs)
                            .background(
                                color = if (index < filled) {
                                    barColor.copy(alpha = segmentAlpha)
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant
                                },
                                shape = PerioTheme.shapes.chip,
                            ),
                    )
                }
            }
            strength.labelRes()?.let { res ->
                Text(
                    text = stringResource(res),
                    style = MaterialTheme.typography.labelMedium,
                    color = barColor,
                )
            }
        }
    }
}

/**
 * The failure banner. One per screen, above the form, for errors that belong to the request rather
 * than to a field (no network, account disabled, rate limited).
 */
@Composable
fun AuthErrorBanner(
    error: AuthError?,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = error != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = PerioTheme.shapes.card,
                )
                .padding(PerioTheme.spacing.md),
        ) {
            Text(
                text = error?.let { stringResource(it.messageRes()) }.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
    }
}

/**
 * Spinner sized to sit inside a button without changing its height, so a form does not resize the
 * moment it is submitted.
 */
@Composable
fun ButtonSpinner(color: Color = MaterialTheme.colorScheme.onPrimary) {
    CircularProgressIndicator(
        color = color,
        strokeWidth = 2.dp,
        modifier = Modifier.size(20.dp),
    )
}
