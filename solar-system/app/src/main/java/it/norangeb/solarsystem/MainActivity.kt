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
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.canonicalName
    private lateinit var arFragment: ArFragment
    private var isModelAdded = false
    private lateinit var planets: Map<Planet, ModelRenderable>
    private lateinit var loadPlanetsJob: Job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!checkIsSupportedDeviceOrFinish(this, TAG))
            return

        setContentView(R.layout.activity_main)
        arFragment = supportFragmentManager.findFragmentById(R.id.ar_fragment) as ArFragment

        loadPlanetsJob = GlobalScope.launch (Dispatchers.Main) {
            planets = loadPlanets(this@MainActivity)
        }

        arFragment.setOnTapArPlaneListener(this::placeSolarSystem)
    }

    private fun placeSolarSystem(hitResult: HitResult, plane: Plane, motionEvent: MotionEvent) {
        GlobalScope.launch(Dispatchers.Main) {
            loadPlanetsJob.join()
            addTransformableNodeToScene(arFragment, hitResult.createAnchor(), planets[Planet.EARTH]!!)
        }
    }
}
