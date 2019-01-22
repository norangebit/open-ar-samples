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

import android.animation.ObjectAnimator
import android.view.animation.LinearInterpolator
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.QuaternionEvaluator
import com.google.ar.sceneform.math.Vector3

class RotationNode : Node() {
    private val DEGREE = 360000

    private var orbitAnimation: ObjectAnimator? = null
    var degreesPerSecond = 90.0f

    private val animationDuration: Long
        get() = (DEGREE / degreesPerSecond)
            .toLong()

    override fun onActivate() {
        startAnimation()
    }

    override fun onDeactivate() {
        stopAnimation()
    }

    private fun startAnimation() {
        if (orbitAnimation != null)
            return

        orbitAnimation = createAnimator()
        orbitAnimation!!.target = this
        orbitAnimation!!.duration = animationDuration
        orbitAnimation!!.start()
    }

    private fun stopAnimation() {
        if (orbitAnimation == null)
            return

        orbitAnimation!!.cancel()
        orbitAnimation = null
    }

    private fun createAnimator(): ObjectAnimator {
        val orientations = arrayOf(0f, 120f, 240f, 360f)
            .map { Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), it) }

        val orbitAnimation = ObjectAnimator()
        orbitAnimation.setObjectValues(*orientations.toTypedArray())

        orbitAnimation.propertyName = "localRotation"

        orbitAnimation.setEvaluator(QuaternionEvaluator())

        orbitAnimation.repeatCount = ObjectAnimator.INFINITE
        orbitAnimation.repeatMode = ObjectAnimator.RESTART
        orbitAnimation.interpolator = LinearInterpolator()
        orbitAnimation.setAutoCancel(true)

        return orbitAnimation
    }
}
