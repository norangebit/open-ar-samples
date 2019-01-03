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

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.ar.core.AugmentedImage
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.ux.ArFragment
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName
    private val IMAGE_NAME = "earth"
    private val IMAGE_FILE_NAME = "earth.jpg"
    private val MODEL_NAME = "earth.sfb"

    private var session: Session? = null
    private lateinit var arFragment: ArFragment
    private lateinit var arSceneView: ArSceneView
    private var isModelAdded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!checkIsSupportedDeviceOrFinish(this, TAG))
            return

        setContentView(R.layout.activity_main)

        arFragment = supportFragmentManager.findFragmentById(R.id.ar_fragment) as ArFragment
        arSceneView = arFragment.arSceneView

        arSceneView.scene.addOnUpdateListener(this::detectAndPlaceAugmentedImage)

        arFragment {
            planeDiscoveryController.hide()
            planeDiscoveryController.setInstructionView(null)
        }
    }

    private fun detectAndPlaceAugmentedImage(frameTime: FrameTime) {
        if (isModelAdded)
            return

        val augmentedImage = arSceneView.arFrame
            .getUpdatedTrackables(AugmentedImage::class.java)
            .filter { isTrackig(it) }
            .find { it.name.contains(IMAGE_NAME) }
            ?: return

        val augmentedImageAnchor = augmentedImage.createAnchor(augmentedImage.centerPose)

        buildRenderable(this, Uri.parse(MODEL_NAME)) {
            addTransformableNodeToScene(
                arFragment,
                augmentedImageAnchor,
                it
            )
        }

        isModelAdded = true
    }

    override fun onPause() {
        super.onPause()

        arSceneView.pause()
        session?.pause()
    }

    override fun onResume() {
        super.onResume()

        if (session != null)
            return

        session = Session(this)
        configureSession()
        arSceneView.setupSession(session)
    }

    private fun configureSession() {
        val config = Config(session)
        if (!setupAugmentedImageDb(config)) {
            Toast.makeText(
                this,
                "Unable to setup augmented image database",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
        session!!.configure(config)
    }

    private fun setupAugmentedImageDb(config: Config): Boolean {
        val image = loadImage(IMAGE_FILE_NAME) ?: return false

        val augmentedImageDb = AugmentedImageDatabase(session)
        augmentedImageDb.addImage(IMAGE_NAME, image)
        config.augmentedImageDatabase = augmentedImageDb
        return true
    }

    private fun loadImage(imageFileName: String): Bitmap? {
        try {
            val inputStrema = assets.open(imageFileName)
            return BitmapFactory.decodeStream(inputStrema)
        } catch (e: IOException) {
            Log.e(TAG, "IO Exception wile loading image", e)
            return null
        }
    }
}
