/*
 * Copyright (c) 2019, norangebit
 *
 * This file is part of augmented-images.
 *
 *      runtime-fetching-models is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      runtime-fetching-models is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with runtime-fetching-models.  If not, see <http://www.gnu.org/licenses/>
 *
 */

package it.norangeb.runtimefetchingmodels

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MotionEvent
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.ux.ArFragment

class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.canonicalName
    private val MODEL_SOURCE = "https://poly.googleusercontent.com/downloads/9-bJ2cXrk8S/8ey98BspXsw/Andy.gltf"
    private lateinit var arFragment: ArFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(!checkIsSupportedDeviceOrFinish(this, TAG))
            return

        setContentView(R.layout.activity_main)
        arFragment = supportFragmentManager.findFragmentById(R.id.ar_fragment) as ArFragment

        arFragment.setOnTapArPlaneListener(this::fetchAndPlaceModel)

    }

    private fun fetchAndPlaceModel(hitResult: HitResult, plane: Plane, motionEvent: MotionEvent) {
        val modelUri = Uri.parse(MODEL_SOURCE)
        val fetchedModel = fetchModel(this, modelUri)
        buildRenderable(this, fetchedModel, modelUri) {
            addTransformableNodeToScene(arFragment, hitResult.createAnchor(), it)
        }
    }
}
