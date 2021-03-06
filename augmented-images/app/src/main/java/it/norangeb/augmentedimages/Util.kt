/*
 * Copyright © 2019, norangebit
 *
 * This file is part of augmented-images.
 *
 *     augmented-images is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     augmented-images is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with augmented-images.  If not, see <http://www.gnu.org/licenses/>
 */

package it.norangeb.augmentedimages

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.google.ar.core.Anchor
import com.google.ar.core.Trackable
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
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

operator fun ArFragment.invoke(λ: ArFragment.() -> Unit) = λ()

fun isTrackig(trackable: Trackable) = trackable.trackingState == TrackingState.TRACKING

fun buildRenderable(
    context: Context,
    model: Uri,
    onSuccess: (renderable: Renderable) -> Unit
) {
    ModelRenderable.builder()
        .setSource(context, model)
        .build()
        .thenAccept(onSuccess)
        .exceptionally {
            Log.e("SCENEFORM", "unable to load model", it)
            return@exceptionally null
        }
}

fun addTransformableNodeToScene(arFragment: ArFragment, anchor: Anchor, renderable: Renderable) {
    val anchorNode = AnchorNode(anchor)
    val transformableNode = TransformableNode(arFragment.transformationSystem)
    transformableNode.renderable = renderable
    transformableNode.setParent(anchorNode)
    arFragment.arSceneView.scene.addChild(anchorNode)
    transformableNode.select()
}

