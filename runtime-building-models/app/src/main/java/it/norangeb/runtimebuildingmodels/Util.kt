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

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.google.ar.core.Anchor
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.*
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode

private val MIN_OPENGL_VERSION = 3.0

@SuppressLint("ObsoleteSdkInt")
fun checkIsSupportedDeviceOrFinish(activity: Activity, tag: String): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
        Log.e(tag, "Sceneform requires Android N or later")
        Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show()
        activity.finish()
        return false
    }
    val openGlVersionString = (activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
        .deviceConfigurationInfo
        .glEsVersion
    if (java.lang.Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
        Log.e(tag, "Sceneform requires OpenGL ES 3.0 later")
        Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
            .show()
        activity.finish()
        return false
    }
    return true
}

fun buildMaterial(
    context: Context,
    color: Color,
    onSuccess: (material: Material) -> Unit
) {
    MaterialFactory
        .makeOpaqueWithColor(context, color)
        .thenAccept(onSuccess)
}

fun buildShape(
    shape: Shape,
    material: Material
): ModelRenderable {
    val dimension = Vector3(0.1f, 0.1f, 0.1f)
    return when (shape) {
        Shape.CUBE -> ShapeFactory
            .makeCube(dimension, Vector3(0.0f, 0.0f, 0.0f), material)
        Shape.CYLINDER -> ShapeFactory
            .makeCylinder(0.1f, 0.3f, Vector3(0.0f, 0.0f, 0.0f), material)
        Shape.SPHERE -> ShapeFactory
            .makeSphere(0.1f, dimension, material)
    }
}

fun changeColorOfMaterial(
    context: Context,
    color: Color,
    renderable: Renderable
) {
    buildMaterial(context, color) {
        renderable.material = it
    }
}

fun addTransformableNodeToScene(arFragment: ArFragment, anchor: Anchor, renderable: Renderable): Node {
    val anchorNode = AnchorNode(anchor)
    val transformableNode = TransformableNode(arFragment.transformationSystem)
    transformableNode.renderable = renderable
    transformableNode.setParent(anchorNode)
    arFragment.arSceneView.scene.addChild(anchorNode)
    transformableNode.select()
    return transformableNode
}

enum class Shape {
    CUBE,
    SPHERE,
    CYLINDER
}
