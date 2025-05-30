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
package com.ashampoo.kim.format

import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.format.bmff.BaseMediaFileFormatImageParser
import com.ashampoo.kim.format.gif.GifImageParser
import com.ashampoo.kim.format.jpeg.JpegImageParser
import com.ashampoo.kim.format.png.PngImageParser
import com.ashampoo.kim.format.raf.RafImageParser
import com.ashampoo.kim.format.tiff.TiffImageParser
import com.ashampoo.kim.format.webp.WebPImageParser
import com.ashampoo.kim.input.ByteReader
import com.ashampoo.kim.model.ImageFormat
import kotlin.jvm.JvmStatic

public fun interface ImageParser {

    @Throws(ImageReadException::class)
    public fun parseMetadata(byteReader: ByteReader): ImageMetadata

    public companion object {

        @JvmStatic
        public fun forFormat(imageFormat: ImageFormat): ImageParser? =
            when (imageFormat) {

                ImageFormat.JPEG -> JpegImageParser

                ImageFormat.PNG -> PngImageParser

                ImageFormat.WEBP -> WebPImageParser

                ImageFormat.TIFF,
                ImageFormat.CR2,
                ImageFormat.NEF,
                ImageFormat.ARW,
                ImageFormat.RW2,
                ImageFormat.ORF -> TiffImageParser

                ImageFormat.RAF -> RafImageParser

                ImageFormat.HEIC,
                ImageFormat.AVIF,
                ImageFormat.CR3,
                ImageFormat.JXL -> BaseMediaFileFormatImageParser

                ImageFormat.GIF -> GifImageParser

                else -> null
            }
    }
}
