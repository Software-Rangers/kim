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
package com.ashampoo.kim.format.tiff.fieldtype

import com.ashampoo.kim.common.ByteOrder
import com.ashampoo.kim.common.ImageWriteException
import com.ashampoo.kim.format.tiff.constant.TiffConstants

/**
 * An 8-bit byte that may contain anything,
 * depending on the definition of the field.
 */
public data object FieldTypeUndefined : FieldType<ByteArray> {

    override val type: Int = TiffConstants.FIELD_TYPE_UNDEFINED_INDEX

    override val name: String = "Undefined"

    override val size: Int = 1

    override fun getValue(bytes: ByteArray, byteOrder: ByteOrder): ByteArray =
        bytes

    override fun writeData(data: Any, byteOrder: ByteOrder): ByteArray =
        when (data) {
            is Byte -> byteArrayOf(data)
            is ByteArray -> data
            else -> throw ImageWriteException("Unsupported type: $data")
        }
}
