package com.example.cards_app.account_management.country_code_picker

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * A VisualTransformation that formats raw digits into a masked string.
 * For example, "1234567890" with a mask of "(###) ###-####" becomes "(123) 456-7890".
 *
 * @param mask The formatting mask. Use '#' as the placeholder for a digit.
 * @param maskChar The character in the mask that represents a digit placeholder.
 */
class NumberVisualTransformation(
    private val mask: String,
    private val maskChar: Char = '#'
) : VisualTransformation {
    val maxLength = mask.count { it == maskChar }
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length > maxLength) text.text.take(maxLength) else text.text
        val annotatedString = AnnotatedString.Builder()
        var maskIndex = 0
        var textIndex = 0

        while (textIndex < trimmed.length && maskIndex < mask.length){
            if (mask[maskIndex] != maskChar) {
                annotatedString.append(mask[maskIndex])
                maskIndex++
            }else{
                annotatedString.append(trimmed[textIndex])
                textIndex++
                maskIndex++
            }
        }
        return TransformedText(
            annotatedString.toAnnotatedString(),
            MaskOffsetMapper(mask, maskChar)
        )
    }
}

/**
 * An OffsetMapping that correctly maps cursor positions between the original (raw)
 * text and the transformed (masked) text. This is essential for proper editing.
 */
private class MaskOffsetMapper(private val mask: String, private val maskChar: Char) : OffsetMapping {
    override fun originalToTransformed(offset: Int): Int {
        var nonMaskCharCount = 0
        var i = 0
        var digitCount = 0
        while (i < mask.length && digitCount < offset) {
            if (mask[i] == maskChar) {
                digitCount++
            } else {
                nonMaskCharCount++
            }
            i++
        }
        return offset + nonMaskCharCount
    }

    override fun transformedToOriginal(offset: Int): Int {
        return offset - mask.take(offset).count { it != maskChar }
    }

}