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
package com.ashampoo.kim.output

import com.ashampoo.kim.common.ByteOrder

public abstract class BinaryByteWriter(
    public val byteWriter: ByteWriter
) : ByteWriter by byteWriter {

    public abstract fun write2Bytes(value: Int)

    // abstract fun write3Bytes(value: Int)

    public abstract fun write4Bytes(value: Int)

    public companion object {

        @kotlin.jvm.JvmStatic
        public fun createBinaryByteWriter(byteWriter: ByteWriter, byteOrder: ByteOrder): BinaryByteWriter =
            when (byteOrder) {
                ByteOrder.LITTLE_ENDIAN -> LittleEndianBinaryByteWriter(byteWriter)
                ByteOrder.BIG_ENDIAN -> BigEndianBinaryByteWriter(byteWriter)
            }
    }
}
