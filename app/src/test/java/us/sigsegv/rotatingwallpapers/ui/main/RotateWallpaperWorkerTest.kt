/*
 * Copyright (c) 2020. Irvin Owens Jr
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package us.sigsegv.rotatingwallpapers.ui.main

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Point
import androidx.work.testing.TestWorkerBuilder
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner
import java.util.concurrent.Executors

@RunWith(MockitoJUnitRunner::class)
class RotateWallpaperWorkerTest {
    @Mock
    private lateinit var mockContext: Context
    private lateinit var mockResources: Resources
    private lateinit var mockConfiguration : Configuration
    private var service = Executors.newSingleThreadExecutor()
    @Test
    fun rotateWallpaperWorker_adjustWindowSizePoint_returnsPortraitAlways() {
        mockContext = mock(Context::class.java)
        mockResources = mock(Resources::class.java)
        mockConfiguration = mock(Configuration::class.java)
        mockConfiguration.orientation = Configuration.ORIENTATION_LANDSCAPE
        `when`(mockContext.resources).thenReturn(mockResources)
        `when`(mockResources.configuration).thenReturn(mockConfiguration)
        val worker: RotateWallpaperWorker = TestWorkerBuilder<RotateWallpaperWorker>(mockContext, service).build()
        val windowSizePoint = FakePoint(1000, 500)
        windowSizePoint.set(1000, 500)
        val newWindowSizePoint = worker.adjustWindowSizePointForLandscape(windowSizePoint)
        Assert.assertEquals("X should be 500", 500, newWindowSizePoint.x)
        Assert.assertEquals("Y should be 1000", 1000, newWindowSizePoint.y)
    }

    @Test
    fun rotateWallpaperWorker_adjustWindowSizePoint_portraitReturnsPortraitAlways() {
        mockContext = mock(Context::class.java)
        mockResources = mock(Resources::class.java)
        mockConfiguration = mock(Configuration::class.java)
        mockConfiguration.orientation = Configuration.ORIENTATION_PORTRAIT
        `when`(mockContext.resources).thenReturn(mockResources)
        `when`(mockResources.configuration).thenReturn(mockConfiguration)
        val worker: RotateWallpaperWorker = TestWorkerBuilder<RotateWallpaperWorker>(mockContext, service).build()
        val windowSizePoint = FakePoint(500, 1000)
        windowSizePoint.set(500, 1000)
        val newWindowSizePoint = worker.adjustWindowSizePointForLandscape(windowSizePoint)
        Assert.assertEquals("X should be 500", 500, newWindowSizePoint.x)
        Assert.assertEquals("Y should be 1000", 1000, newWindowSizePoint.y)
    }

    class FakePoint(x: Int, y: Int) : Point(x, y) {
        var myx: Int = 0
        var myy: Int = 0

        fun setx(x: Int) {
            myx = x
        }

        fun sety(y: Int) {
            myy = y
        }

        fun gety() : Int {
            return myy
        }

        fun getx() : Int {
            return myx
        }
        override fun set(x: Int, y: Int) {
            super.x = x
            super.y = y
            this.x = x
            this.y = y
            myy = y
            myx = x
        }
    }
}