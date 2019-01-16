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

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.canonicalName
    private val AU_TO_METERS = 1.1f

    private lateinit var arFragment: ArFragment
    private var isModelAdded = false
    private lateinit var renderablePlanets: Map<Planet, ModelRenderable>
    private lateinit var loadPlanetsJob: Job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!checkIsSupportedDeviceOrFinish(this, TAG))
            return

        setContentView(R.layout.activity_main)
        arFragment = supportFragmentManager.findFragmentById(R.id.ar_fragment) as ArFragment

        loadPlanetsJob = GlobalScope.launch(Dispatchers.Main) {
            renderablePlanets = loadPlanets(this@MainActivity)
        }

        arFragment.setOnTapArPlaneListener(this::placeSolarSystem)
    }

    private fun placeSolarSystem(hitResult: HitResult, plane: Plane, motionEvent: MotionEvent) {
        if (isModelAdded)
            return

        GlobalScope.launch(Dispatchers.Main) {
            loadPlanetsJob.join()

            val solarSystem = createSolarSystem(renderablePlanets)
            addNodeToScene(arFragment, hitResult.createAnchor(), solarSystem)
            isModelAdded = true
        }
    }

    private fun createSolarSystem(renderablePlanets: Map<Planet, ModelRenderable>): Node {
        val base = Node()

        val sun = Node()
        sun.setParent(base)
        sun.localPosition = Vector3(0.0f, 0.5f, 0.0f)

        val sunVisual = Node()
        sunVisual.setParent(sun)
        sunVisual.renderable = renderablePlanets[Planet.SUN]
        sunVisual.localScale = Vector3(0.5f, 0.5f, 0.5f)

        createPlanetNode(Planet.MERCURY, sun, 0.4f, 47f, renderablePlanets)
        createPlanetNode(Planet.VENUS, sun, 0.7f, 35f, renderablePlanets)
        createPlanetNode(Planet.EARTH, sun, 1.0f, 29f, renderablePlanets)
        createPlanetNode(Planet.MARS, sun, 1.5f, 24f, renderablePlanets)
        createPlanetNode(Planet.JUPITER, sun, 2.2f, 13f, renderablePlanets)
        createPlanetNode(Planet.SATURN, sun, 3.5f, 9f, renderablePlanets)
        createPlanetNode(Planet.URANUS, sun, 5.2f, 7f, renderablePlanets)
        createPlanetNode(Planet.NEPTUNE, sun, 6.1f, 5f, renderablePlanets)

        return base
    }

    private fun createPlanetNode(
        planet: Planet,
        parent: Node,
        auFromParent: Float,
        orbitDegreesPerSecond: Float,
        renderablePlanets: Map<Planet, ModelRenderable>
    ) {
        val orbit = RotationNode()
        orbit.degreesPerSecond = orbitDegreesPerSecond
        orbit.setParent(parent)

        val renderable = renderablePlanets[planet] ?: return
        val planetNode = PlanetNode(renderable)
        planetNode.setParent(orbit)
        planetNode.localPosition = Vector3(AU_TO_METERS * auFromParent, 0.0f, 0.0f)
    }


}
