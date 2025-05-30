/*
 * Copyright 2025 Ashampoo GmbH & Co. KG
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
package com.ashampoo.kim.format.webp

import com.ashampoo.kim.common.toFourCCTypeString

/**
 * Type of a WebP chunk.
 */
public data class WebPChunkType(
    val bytes: ByteArray,
    val name: String,
    val intValue: Int
) {

    override fun equals(other: Any?): Boolean {

        if (this === other)
            return true

        if (other !is WebPChunkType)
            return false

        return bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int =
        bytes.contentHashCode()

    override fun toString(): String =
        name

    public companion object {

        /** Standard lossy VP8 */
        public val VP8: WebPChunkType = of("VP8 ".encodeToByteArray())

        /** Lossless VP8 */
        public val VP8L: WebPChunkType = of("VP8L".encodeToByteArray())

        /** Extended VP8 */
        public val VP8X: WebPChunkType = of("VP8X".encodeToByteArray())

        /** EXIF metadata */
        public val EXIF: WebPChunkType = of("EXIF".encodeToByteArray())

        /** XMP metadata */
        public val XMP: WebPChunkType = of("XMP ".encodeToByteArray())

        @Suppress("MagicNumber")
        public fun of(typeBytes: ByteArray): WebPChunkType {

            require(typeBytes.size == WebPConstants.TPYE_LENGTH) {
                "ChunkType must be always 4 bytes, but got ${typeBytes.size} bytes!"
            }

            @Suppress("UnnecessaryParentheses")
            val intValue =
                (typeBytes[0].toInt() shl 24) or
                    (typeBytes[1].toInt() shl 16) or
                    (typeBytes[2].toInt() shl 8) or
                    (typeBytes[3].toInt() shl 0)

            return WebPChunkType(
                bytes = typeBytes,
                name = intValue.toFourCCTypeString(),
                intValue = intValue
            )
        }
    }
}
