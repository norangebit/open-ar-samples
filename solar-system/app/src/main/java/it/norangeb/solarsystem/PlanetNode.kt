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

import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable

class PlanetNode(
    private val planetRenderable: ModelRenderable
) : Node() {
    private val planetScale = 0.6f
    private var planetVisual: RotationNode? = null

    override fun onActivate() {
        if (scene == null)
            throw IllegalStateException("Scene is null!")

        if (planetVisual == null)
            initRotationNode()

    }

    override fun onUpdate(frameTime: FrameTime?) {
        if (scene == null)
            return
    }

    fun initRotationNode() {
        planetVisual = RotationNode(false)
        planetVisual?.setParent(this)
        planetVisual?.renderable = planetRenderable
        planetVisual?.localScale = Vector3(planetScale, planetScale, planetScale)
    }
}