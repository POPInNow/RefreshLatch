/*
 * Copyright (C) 2019 POP Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.popinnow.android.refresh

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.lifecycle.Lifecycle.Event.ON_DESTROY
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import timber.log.Timber
import java.util.UUID

/**
 * RefreshLatch is a class which is designed to prevent UI flicker for quick refresh events.
 *
 * If a refresh event takes less time than the [delayTime], the refresh UI will not be shown at all.
 * If a refresh event takes more time than the [delayTime], the refresh UI will be shown for
 * at least the [minShowTime], or the total amount of time for the refresh operation -
 * which ever is longer.
 *
 * The uniqueId field is not meant to actually be overridden, it is always meant to be random.
 *
 * A UUID is present so that a user can register multiple RefreshLatches on the same LifecycleOwner.
 * LifecycleOwner counts its unique registrations via the equals/hashCode business, so we must
 * generate a unique UUID here or else two RefreshLatches can have the same equals.
 */
internal data class RefreshLatchImpl internal constructor(
  private val owner: LifecycleOwner,
  private val delayTime: Long,
  private val minShowTime: Long,
  private val onRefresh: (refreshing: Boolean) -> Unit,
  private val uniqueId: String = UUID.randomUUID().toString()
) : RefreshLatch {

  // Debugging enabled
  private var debug = false

  // Internal command state
  private var state = RefreshState(isRefreshing = false, timeLastShown = 0L)

  // Main thread handler for posting commands
  private val handler = Handler(Looper.getMainLooper())

  init {
    owner.lifecycle.addObserver(object : LifecycleObserver {

      @Suppress("unused")
      @OnLifecycleEvent(ON_DESTROY)
      fun onDestroy() {
        owner.lifecycle.removeObserver(this)
        clearCommands()
      }
    })
  }

  override var isRefreshing: Boolean
    get() = state.isRefreshing
    set(refresh) {
      if (state.isRefreshing == refresh) {
        debugLog { "setRefreshing() called with same state, ignore." }
        return
      }

      clearCommands()
      state = state.copy(isRefreshing = refresh)

      val timeLastShown = state.timeLastShown
      when {
        refresh -> queueShow()
        timeLastShown >= 0 -> queueHide(timeLastShown)
        else -> hide()
      }
    }

  override fun force(isRefreshing: Boolean) {
    clearCommands()
    state = state.copy(isRefreshing = isRefreshing)

    if (isRefreshing) {
      show()
    } else {
      hide()
    }
  }

  override fun enableDebugging(): RefreshLatchImpl {
    return this.apply { debug = true }
        .also { debugLog { "Enabling Debugging" } }
  }

  private inline fun debugLog(message: () -> String) {
    if (debug) {
      Timber.d(message())
    }
  }

  private fun queueShow() {
    val delay = delayTime
    debugLog { "Queueing show() to run in $delay milliseconds." }
    queueCommand(delay) { show() }
  }

  private fun queueHide(timeLastShown: Long) {
    val shownTime = SystemClock.uptimeMillis() - timeLastShown
    if (shownTime < minShowTime) {
      val delay = minShowTime - shownTime
      debugLog { "Queueing hide() to run in $delay milliseconds." }
      queueCommand(delay) { hide() }
    } else {
      hide()
    }
  }

  private inline fun queueCommand(
    delay: Long,
    crossinline command: () -> Unit
  ) {
    handler.postDelayed({ command() }, delay)
  }

  private fun clearCommands() {
    debugLog { "Clearing command buffer." }
    handler.removeCallbacksAndMessages(null)
  }

  private fun show() {
    debugLog { "show()" }
    state = state.copy(timeLastShown = SystemClock.uptimeMillis())
    onRefresh(true)
  }

  private fun hide() {
    debugLog { "hide()" }
    state = state.copy(timeLastShown = 0L)
    onRefresh(false)
  }

  private data class RefreshState(
    val isRefreshing: Boolean,
    val timeLastShown: Long
  )
}
