/*
 * Copyright (c) 2019, norangebit
 *
 * This file is part of runtime-building-models.
 *
 *      runtime-building-models is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      runtime-building-models is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with runtime-building-models.  If not, see <http://www.gnu.org/licenses/>
 *
 */

package it.norangeb.runtimebuildingmodels

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.ux.ArFragment

class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.canonicalName
    private lateinit var arFragment: ArFragment
    private var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!checkIsSupportedDeviceOrFinish(this, TAG))
            return

        setContentView(R.layout.activity_main)
        arFragment = supportFragmentManager.findFragmentById(R.id.ar_fragment) as ArFragment

        arFragment.setOnTapArPlaneListener(this::addModel)
    }

    private fun addModel(hitResult: HitResult, plane: Plane, motionEvent: MotionEvent) {
        val color = generateColor()

        buildMaterial(this, color) {
            val node = addTransformableNodeToScene(
                arFragment,
                hitResult.createAnchor(),
                buildShape(generateShape(), it)
            )

            node.setOnTapListener {_ , _ ->
                changeColorOfMaterial(
                    this,
                    generateColor(),
                    node.renderable
                )
            }
        }
    }

    private fun generateColor(): Color {
        val color = when (count++%11) {
            0 -> android.graphics.Color.CYAN
            1 -> android.graphics.Color.BLUE
            2 -> android.graphics.Color.GREEN
            3 -> android.graphics.Color.MAGENTA
            5 -> android.graphics.Color.BLACK
            6 -> android.graphics.Color.DKGRAY
            7 -> android.graphics.Color.LTGRAY
            8 -> android.graphics.Color.RED
            9 -> android.graphics.Color.TRANSPARENT
            10 -> android.graphics.Color.WHITE
            else -> android.graphics.Color.YELLOW
        }

        return Color(color)
    }

    private fun generateShape(): Shape {
        return when (count++%3) {
            0 -> Shape.CYLINDER
            1 -> Shape.CUBE
            else -> Shape.SPHERE
        }
    }
}
