/*
 * Copyright 2025 Ashampoo GmbH & Co. KG
 * Copyright 2007-2023 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ashampoo.kim.format.tiff

import com.ashampoo.kim.common.ByteOrder
import com.ashampoo.kim.common.HEX_RADIX
import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.common.RationalNumber
import com.ashampoo.kim.common.RationalNumbers
import com.ashampoo.kim.common.toSingleNumberHexes
import com.ashampoo.kim.format.tiff.TiffTags.getTag
import com.ashampoo.kim.format.tiff.fieldtype.FieldType
import com.ashampoo.kim.format.tiff.taginfo.TagInfo
import com.ashampoo.kim.format.tiff.taginfo.TagInfoGpsText

/**
 * A TIFF field in a TIFF directory.
 */
public class TiffField(
    /** Offset relative to TIFF header */
    public val offset: Int,
    public val tag: Int,
    public val directoryType: Int,
    public val fieldType: FieldType<out Any>,
    public val count: Int,
    /** Set if field has a local value. */
    public val localValue: Int?,
    /** Set if field has a offset pointer to its value. */
    public val valueOffset: Int?,
    public val valueBytes: ByteArray,
    public val byteOrder: ByteOrder,
    public val sortHint: Int
) {

    /**
     * Returns the offset with padding.
     * Because TIFF files can be as big as 4 GB we need 10 digits to present that.
     */
    public val offsetFormatted: String =
        offset.toString().padStart(10, '0')

    /** Return a proper Tag ID like 0x0100 */
    public val tagFormatted: String =
        "0x" + tag.toString(HEX_RADIX).padStart(4, '0')

    /** TagInfo, if the tag is found in our registry. */
    public val tagInfo: TagInfo? = getTag(directoryType, tag)

    public val value: Any = if (tagInfo is TagInfoGpsText)
        tagInfo.getValue(this)
    else
        fieldType.getValue(this.valueBytes, this.byteOrder)

    public val valueDescription: String by lazy {
        try {

            if (value is ByteArray) {

                if (value.size == 1)
                    return@lazy value.first().toString()

                if (value.size <= MAX_ARRAY_LENGTH_DISPLAY_SIZE)
                    return@lazy "[${value.toSingleNumberHexes()}]"

                return@lazy "[${value.size} bytes]"
            }

            if (value is IntArray) {

                if (value.size == 1)
                    return@lazy value.first().toString()

                if (value.size <= MAX_ARRAY_LENGTH_DISPLAY_SIZE)
                    return@lazy value.contentToString()

                return@lazy "[${value.size} ints]"
            }

            if (value is ShortArray) {

                if (value.size == 1)
                    return@lazy value.first().toString()

                if (value.size <= MAX_ARRAY_LENGTH_DISPLAY_SIZE)
                    return@lazy value.contentToString()

                return@lazy "[${value.size} shorts]"
            }

            if (value is DoubleArray) {

                if (value.size == 1)
                    return@lazy value.first().toString()

                if (value.size <= MAX_ARRAY_LENGTH_DISPLAY_SIZE)
                    return@lazy value.contentToString()

                return@lazy "[${value.size} doubles]"
            }

            if (value is FloatArray) {

                if (value.size == 1)
                    return@lazy value.first().toString()

                if (value.size <= MAX_ARRAY_LENGTH_DISPLAY_SIZE)
                    return@lazy value.contentToString()

                return@lazy "[${value.size} floats]"
            }

            if (value is RationalNumbers) {

                if (value.values.size == 1)
                    return@lazy value.values.first().toString()

                if (value.values.size <= MAX_ARRAY_LENGTH_DISPLAY_SIZE)
                    return@lazy value.values.contentToString()

                return@lazy "[${value.values.size} rationals]"
            }

            value.toString()

        } catch (ex: ImageReadException) {
            "Invalid value: " + ex.message
        }
    }

    public fun toStringValue(): String {

        if (value is List<*>) {

            /*
             * If the field is all NULLs, this wil result in an empty list.
             */
            val firstValue = value.firstOrNull() ?: return ""

            return firstValue.toString()
        }

        if (value !is String)
            throw ImageReadException("Expected String for $tagFormatted, but got: $value")

        return value
    }

    public fun toIntArray(): IntArray {

        if (value is Number)
            return intArrayOf(value.toInt())

        if (value is IntArray)
            return value

        if (value is ShortArray) {

            val result = IntArray(value.size)

            repeat(result.size) { index ->
                result[index] = 0xFFFF and value[index].toInt()
            }

            return result
        }

        throw ImageReadException("Can't format value of tag $tagFormatted as int: $value")
    }

    public fun toInt(): Int = when (value) {
        is ByteArray -> value.first().toInt()
        is ShortArray -> value.first().toInt()
        is IntArray -> value.first()
        else -> (value as Number).toInt()
    }

    public fun toShort(): Short = when (value) {
        is ByteArray -> value.first().toShort()
        is ShortArray -> value.first()
        is IntArray -> value.first().toShort()
        else -> (value as Number).toShort()
    }

    public fun toDouble(): Double = when (value) {
        is RationalNumbers -> value.values.first().doubleValue()
        is RationalNumber -> value.doubleValue()
        is ByteArray -> value.first().toDouble()
        is ShortArray -> value.first().toDouble()
        is IntArray -> value.first().toDouble()
        is FloatArray -> value.first().toDouble()
        is DoubleArray -> value.first()
        else -> (value as Number).toDouble()
    }

    /*
     * Note that we need to show the local 'tagFormatted', because
     * 'tagInfo' might be an Unknown tag and show a placeholder.
     */
    override fun toString(): String =
        "$offsetFormatted $tagFormatted ${tagInfo?.name ?: "Unknown"} = $valueDescription"

    internal fun createOversizeValueElement(): TiffElement? =
        valueOffset?.let { OversizeValueElement(it, valueBytes.size) }

    internal inner class OversizeValueElement(offset: Int, length: Int) : TiffElement(
        debugDescription = "Value of $tagInfo ($fieldType) @ $offset",
        offset = offset,
        length = length
    ) {

        override fun toString(): String =
            debugDescription
    }

    private companion object {

        /**
         * Limit to 16 bytes, so that a GeoTiff ModelTransformationTag
         * is still displayed in full, but not values greater than that.
         */
        private const val MAX_ARRAY_LENGTH_DISPLAY_SIZE = 16
    }
}
