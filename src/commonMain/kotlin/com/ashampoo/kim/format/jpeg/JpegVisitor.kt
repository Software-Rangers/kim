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
package com.ashampoo.kim.format.jpeg

internal interface JpegVisitor {

    /** Return false to exit before reading image data. */
    fun beginSOS(): Boolean

    fun visitSOS(
        marker: Int,
        markerBytes: ByteArray,
        imageData: ByteArray
    )

    /** Return false to exit traversal. */
    fun visitSegment(
        marker: Int,
        markerBytes: ByteArray,
        segmentLength: Int,
        segmentLengthBytes: ByteArray,
        segmentBytes: ByteArray
    ): Boolean
}
