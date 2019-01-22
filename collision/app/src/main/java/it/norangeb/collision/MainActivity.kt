/*
 * Copyright (c) 2019, norangebit
 *
 * This file is part of collision.
 *
 *      collision is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      collision is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with collision.  If not, see <http://www.gnu.org/licenses/>
 *
 */

package it.norangeb.collision

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.widget.Toast
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.ux.ArFragment

class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.canonicalName

    private lateinit var arFragment: ArFragment
    private lateinit var arScene: Scene
    private var lastNode: Node? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!checkIsSupportedDeviceOrFinish(this, TAG))
            return

        setContentView(R.layout.activity_main)

        arFragment = supportFragmentManager.findFragmentById(R.id.ar_fragment) as ArFragment
        arScene = arFragment.arSceneView.scene

        arFragment.setOnTapArPlaneListener(this::addShape)

        arScene.addOnUpdateListener(this::onUpdate)
    }

    private fun onUpdate(frameTime: FrameTime) {
        val node = lastNode ?: return
        val overlappedNodes = arScene.overlapTestAll(node)
        if (overlappedNodes.isNotEmpty())
            onCollision(overlappedNodes)
    }

    private fun onCollision(nodes: List<Node>) {
        Toast.makeText(this, "collision", Toast.LENGTH_LONG).show()
        lastNode?.isEnabled = false
        lastNode = null
    }

    private fun addShape(hitResult: HitResult, plane: Plane, motionEvent: MotionEvent) {
        val red = Color(android.graphics.Color.RED)

        buildMaterial(this, red) {
            val cube = buildShape(Shape.CUBE, it)

            lastNode = addNodeToScene(arFragment, hitResult.createAnchor(), cube)
        }
    }
}
