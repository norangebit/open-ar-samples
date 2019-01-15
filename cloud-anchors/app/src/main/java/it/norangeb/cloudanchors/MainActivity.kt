/*
 * Copyright (c) 2019, norangebit
 *
 * This file is part of cloud-anchors.
 *
 *      cloud-anchors is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      cloud-anchors is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with cloud-anchors.  If not, see <http://www.gnu.org/licenses/>
 *
 */

package it.norangeb.cloudanchors

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.google.ar.core.*
import com.google.ar.sceneform.FrameTime
import com.google.ar.core.Anchor


class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.canonicalName

    private lateinit var arFragment: CloudArFragment
    private lateinit var storageManager: StorageManager
    private var cloudAnchorState = CloudAnchorState.LOCAL
    private var cloudAnchor: Anchor? = null
        set(value) {
            field?.detach()
            field = value
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!checkIsSupportedDeviceOrFinish(this, TAG))
            return

        storageManager = StorageManager(this)

        setContentView(R.layout.activity_main)

        arFragment = supportFragmentManager.findFragmentById(R.id.ar_fragment) as CloudArFragment

        arFragment.setOnTapArPlaneListener(this::addModel)

        arFragment.arSceneView.scene
            .addOnUpdateListener(this::checkCloudAnchor)
    }

    private fun addModel(hitResult: HitResult, plane: Plane, motionEvent: MotionEvent) {
        if (cloudAnchorState != CloudAnchorState.LOCAL)
            return

        cloudAnchor = arFragment.arSceneView.session
            .hostCloudAnchor(hitResult.createAnchor())

        cloudAnchorState = CloudAnchorState.HOSTING

        buildRenderable(this, Uri.parse("model.sfb")) {
            addTransformableNodeToScene(
                arFragment,
                cloudAnchor ?: return@buildRenderable,
                it
            )
        }
    }

    private fun checkCloudAnchor(frameTime: FrameTime) {
        if (cloudAnchorState != CloudAnchorState.HOSTING
            && cloudAnchorState != CloudAnchorState.RESOLVING
        )
            return

        val cloudState = cloudAnchor?.cloudAnchorState ?: return

        if (cloudState.isError) {
            toastError()
            cloudAnchorState = CloudAnchorState.LOCAL
            return
        }

        if (cloudState != Anchor.CloudAnchorState.SUCCESS)
            return

        if (cloudAnchorState == CloudAnchorState.HOSTING)
            checkHosting(cloudState)
        else
            checkResolving(cloudState)
    }

    private fun checkResolving(state: Anchor.CloudAnchorState) {
        Toast.makeText(this, "Anchor resolved!", Toast.LENGTH_LONG)
            .show()
        cloudAnchorState = CloudAnchorState.RESOLVED
    }

    private fun checkHosting(state: Anchor.CloudAnchorState) {
        storageManager.nextShortCode { shortCode ->
            if (shortCode == null) {
                toastError()
                return@nextShortCode
            }
            storageManager.storeUsingShortCode(shortCode, cloudAnchor?.cloudAnchorId)

            Toast.makeText(this, "Anchor hosted with code $shortCode", Toast.LENGTH_LONG)
                .show()
            Log.d("NORANGEBIT", "$shortCode")
        }

        cloudAnchorState = CloudAnchorState.HOSTED
    }

    private fun toastError() =
        Toast.makeText(this, "Error", Toast.LENGTH_LONG)
            .show()

    private fun onResolveOkPressed(dialogValue: String) {
        val shortCode = Integer.parseInt(dialogValue)
        storageManager.getCloudAnchorID(shortCode) {
            cloudAnchor = arFragment.arSceneView.session
                .resolveCloudAnchor(it)

            buildRenderable(this, Uri.parse("model.sfb")) {
                val anchor = cloudAnchor ?: return@buildRenderable
                addTransformableNodeToScene(arFragment, anchor, it)
                cloudAnchorState = CloudAnchorState.RESOLVING
            }
        }
    }

    fun onClearClick(view: View) {
        cloudAnchor = null
        cloudAnchorState = CloudAnchorState.LOCAL
    }

    fun onResolveClick(view: View) {
        if (cloudAnchor != null)
            return

        val dialog = ResolveDialogFragment()
        dialog.setOkListener(this::onResolveOkPressed)
        dialog.show(supportFragmentManager, "Resolve")
    }
}
