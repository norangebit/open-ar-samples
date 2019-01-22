/*
 * Copyright (c) 2019, norangebit
 *
 * This file is part of solar-system.
 *
 *      solar-system is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      solar-system is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with solar-system.  If not, see <http://www.gnu.org/licenses/>
 *
 */

package it.norangeb.solarsystem

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.google.ar.core.Anchor
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.rendering.*
import com.google.ar.sceneform.ux.ArFragment
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.util.concurrent.CompletableFuture

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

suspend fun loadPlanets(context: Context): Map<Planet, ModelRenderable> {
    threadLog("load planets")
    val sun = loadPlanet(context, Planet.SUN)
    val mercury = loadPlanet(context, Planet.MERCURY)
    val venus = loadPlanet(context, Planet.VENUS)
    val earth = loadPlanet(context, Planet.EARTH)
    val mars = loadPlanet(context, Planet.MARS)
    val jupiter = loadPlanet(context, Planet.JUPITER)
    val saturn = loadPlanet(context, Planet.SATURN)
    val uranus = loadPlanet(context, Planet.URANUS)
    val neptune = loadPlanet(context, Planet.NEPTUNE)

    return mapOf(
        Pair(Planet.SUN, sun.await()),
        Pair(Planet.MERCURY, mercury.await()),
        Pair(Planet.VENUS, venus.await()),
        Pair(Planet.EARTH, earth.await()),
        Pair(Planet.MARS, mars.await()),
        Pair(Planet.JUPITER, jupiter.await()),
        Pair(Planet.SATURN, saturn.await()),
        Pair(Planet.URANUS, uranus.await()),
        Pair(Planet.NEPTUNE, neptune.await())
    )
}

fun loadPlanet(context: Context, planet: Planet): Deferred<ModelRenderable> {
    val modelSource = fetchModel(context, Uri.parse(planet.value))
    val futureRenderable = buildFutureRenderable(
        context,
        modelSource,
        Uri.parse(planet.value)
    )

    return GlobalScope.async(Dispatchers.IO) {
        threadLog("start loading: $planet")
        val renderable = futureRenderable.get()
        threadLog("end loading: $planet")
        renderable
    }
}

fun buildFutureRenderable(
    context: Context,
    model: RenderableSource,
    modelUri: Uri
): CompletableFuture<ModelRenderable> =
    ModelRenderable.builder()
        .setRegistryId(modelUri)
        .setSource(context, model)
        .build()

fun fetchModel(
    context: Context,
    source: Uri
): RenderableSource =
    RenderableSource.builder()
        .setSource(context, source, RenderableSource.SourceType.GLTF2)
        .setRecenterMode(RenderableSource.RecenterMode.ROOT)
        .build()

fun addNodeToScene(arFragment: ArFragment, anchor: Anchor, node: Node) {
    val anchorNode = AnchorNode(anchor)
    anchorNode.addChild(node)
    arFragment.arSceneView.scene.addChild(anchorNode)
}

enum class Planet(val value: String) {
    SUN("https://poly.googleusercontent.com/downloads/69ejysWdDXG/2Gi-kna7W1j/Sun_01.gltf"),
    MERCURY("https://poly.googleusercontent.com/downloads/9PBDNYhgXDS/0ikSBktlxQd/model.gltf"),
    VENUS("https://poly.googleusercontent.com/downloads/5BKXL2tO5C6/7l37kn7jUdv/model.gltf"),
    EARTH("https://poly.googleusercontent.com/downloads/dQcv6bKXuQD/3NFGAgmwv5x/model.gltf"),
    MARS("https://poly.googleusercontent.com/downloads/8sNKYRTUFAe/f9eC5eU749w/model.gltf"),
    JUPITER("https://poly.googleusercontent.com/downloads/6jv2Cl_xK2i/9lyFWlFV2GO/model.gltf"),
    SATURN("https://poly.googleusercontent.com/downloads/b-y9HDTsu7q/4y6CcWQ0Yu0/model.gltf"),
    URANUS("https://poly.googleusercontent.com/downloads/7qZDvQcXQWN/2GW1CXvX6g4/model.gltf"),
    NEPTUNE("https://poly.googleusercontent.com/downloads/38LOedsa9ES/9oKqBu_hVcY/model.gltf")
}

fun threadLog(message: String) = Log.d(
    "COROUTINES",
    "[${Thread.currentThread().name}]: $message"
)
