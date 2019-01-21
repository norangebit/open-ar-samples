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

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class CloudAnchorsHelper {
    private val fireStoreDb = FirebaseFirestore.getInstance()
    private var nextCode = 0

    init {
        fireStoreDb.collection(COLLECTION)
            .document(DOCUMENT)
            .get()
            .addOnSuccessListener {
                val nCode = it.data?.get(NEXT_CODE) ?: return@addOnSuccessListener
                nextCode = nCode.toString().toInt()
            }
    }

    fun getShortCode(cloudAnchorId: String): Int {
        fireStoreDb.collection(COLLECTION)
            .document(DOCUMENT)
            .set(
                mapOf(Pair(nextCode.toString(), cloudAnchorId)),
                SetOptions.merge())
        uploadNextCode(nextCode+1)
        return nextCode++
    }

    private fun uploadNextCode(nextCode: Int) {
        fireStoreDb.collection(COLLECTION)
            .document(DOCUMENT)
            .set(
                mapOf(Pair(NEXT_CODE, nextCode)),
                SetOptions.merge()
            )
    }

    fun getCloudAnchorId(
        shortCode: Int,
        onSuccess: (String) -> Unit
    ) {
        fireStoreDb.collection(COLLECTION)
            .document(DOCUMENT)
            .get()
            .addOnSuccessListener {
                val x = it.data?.get(shortCode.toString()) as String
                onSuccess(x)
            }
    }

    companion object {
        private const val COLLECTION = "short_codes"
        private const val DOCUMENT = "short_codes_doc"
        private const val NEXT_CODE = "next_short_code"
    }
}