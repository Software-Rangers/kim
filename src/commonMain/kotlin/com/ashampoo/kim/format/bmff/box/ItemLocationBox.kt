/*
 * Copyright 2025 Ashampoo GmbH & Co. KG
 * Copyright 2002-2023 Drew Noakes and contributors
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
package com.ashampoo.kim.format.bmff.box

import com.ashampoo.kim.format.bmff.BMFFConstants.BMFF_BYTE_ORDER
import com.ashampoo.kim.format.bmff.BoxType
import com.ashampoo.kim.format.bmff.Extent
import com.ashampoo.kim.input.ByteArrayByteReader
import com.ashampoo.kim.input.read2BytesAsInt
import com.ashampoo.kim.input.read4BytesAsInt
import com.ashampoo.kim.input.read8BytesAsLong
import com.ashampoo.kim.input.readByteAsInt
import com.ashampoo.kim.input.readBytes
import com.ashampoo.kim.input.readXBytesAtInt
import com.ashampoo.kim.input.skipBytes

/**
 * EIC/ISO 14496-12 iloc box
 */
public class ItemLocationBox(
    offset: Long,
    size: Long,
    largeSize: Long?,
    payload: ByteArray
) : Box(BoxType.ILOC, offset, size, largeSize, payload) {

    /**
     * The version of the box.
     */
    public val version: Int

    /**
     * Flags that provide additional information about the box.
     */
    public val flags: ByteArray

    /**
     * The size (in bytes) of the offset field in each item location entry.
     */
    public val offsetSize: Int

    /**
     * The size (in bytes) of the length field in each item location entry.
     */
    public val lengthSize: Int

    /**
     * The size (in bytes) of the base offset field in each item location entry.
     */
    public val baseOffsetSize: Int

    /**
     * This part contains the actual entries describing the location of items within the file.
     */
    public val indexSize: Int

    public val itemCount: Int

    public val extents: List<Extent>

    init {

        val byteReader = ByteArrayByteReader(payload)

        version = byteReader.readByteAsInt()

        /* Fail fast if the code needs to be updated for a newer version. */
        check(version in 0..2) {
            "Unsupported ILOC version: $version"
        }

        flags = byteReader.readBytes("flags", 3)

        val offsetAndLengthSize = byteReader.readByteAsInt()
        offsetSize = (offsetAndLengthSize and 0xF0) shr 4
        lengthSize = offsetAndLengthSize and 0x0F

        val baseOffsetSizeAndIndexSize = byteReader.readByteAsInt()
        baseOffsetSize = (baseOffsetSizeAndIndexSize and 0xF0) shr 4

        indexSize = if (version in 1..2)
            baseOffsetSizeAndIndexSize and 0x0F
        else
            0 // Unused

        itemCount = if (version < 2)
            byteReader.read2BytesAsInt("itemCount", BMFF_BYTE_ORDER)
        else if (version == 2)
            byteReader.read4BytesAsInt("itemCount", BMFF_BYTE_ORDER)
        else
            error("Unknown version $version")

        val extents = mutableListOf<Extent>()

        repeat(itemCount) {

            val itemId: Int = if (version < 2)
                byteReader.read2BytesAsInt("itemId", BMFF_BYTE_ORDER)
            else if (version == 2)
                byteReader.read4BytesAsInt("itemId", BMFF_BYTE_ORDER)
            else
                error("Unknown version $version")

            if (version in 1..2)
                byteReader.skipBytes("constructionMethod", 2)

            byteReader.skipBytes("dataReferenceIndex", 2)

            val baseOffset: Long = when (baseOffsetSize) {
                4 -> byteReader.read4BytesAsInt("baseOffset", BMFF_BYTE_ORDER).toLong()
                8 -> byteReader.read8BytesAsLong("baseOffset", BMFF_BYTE_ORDER)
                else -> 0
            }

            val extentCount = byteReader.read2BytesAsInt("extentCount", BMFF_BYTE_ORDER)

            repeat(extentCount) {

                val extentIndex: Long? = if (version in 1..2 && indexSize > 0)
                    byteReader.readXBytesAtInt("extentIndex", indexSize, BMFF_BYTE_ORDER)
                else
                    null

                val extentOffset = byteReader.readXBytesAtInt("extentOffset", offsetSize, BMFF_BYTE_ORDER)
                val extentLength = byteReader.readXBytesAtInt("extentLength", lengthSize, BMFF_BYTE_ORDER)

                extents.add(
                    Extent(
                        itemId = itemId,
                        index = extentIndex,
                        offset = extentOffset + baseOffset,
                        length = extentLength
                    )
                )
            }
        }

        /*
         * Sort by offset to support reading fields in order.
         * Warning: This is important for other logic to function properly.
         */
        extents.sortBy { it.offset }

        this.extents = extents
    }

    override fun toString(): String =
        "$type " +
            "offsetSize=$offsetSize " +
            "lengthSize=$lengthSize " +
            "baseOffsetSize=$baseOffsetSize " +
            "indexSize=$indexSize " +
            "itemCount=$itemCount " +
            "extents=$extents"
}
