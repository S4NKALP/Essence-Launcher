/*
 * Copyright (c) 2025 Sankalp Tharu
 *
 * Licensed under the MIT License.
 * See the LICENSE file in the project root for license information.
 */
 
package com.github.essencelauncher.utils.listener

/**
 * GestureAdapter provides empty implementations for all GestureListener methods.
 * Override only what you need.
 */
abstract class GestureAdapter : GestureManager.GestureListener {
    override fun onSwipeLeft() {}
    override fun onSwipeRight() {}
    override fun onSwipeUp() {}
    override fun onSwipeDown() {}
    override fun onDoubleTap() {}
    override fun onLongPress() {}
}
