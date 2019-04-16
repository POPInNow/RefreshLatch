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
 */
internal data class RefreshLatchImpl internal constructor(
  // Lifecycle to bind to
  private val owner: LifecycleOwner,

  // Number of milliseconds to wait before "posting" the show command
  private val delayTime: Long,

  // Minimum number of milliseconds to show before "posting" the hide command
  private val minShowTime: Long,

  // When a command is posted, fire this callback
  private val onRefresh: (refreshing: Boolean) -> Unit,

  // Random unique UUID so that a user can register multiple RefreshLatches on the same LifecycleOwner
  // LifecycleOwner counts its unique registrations via the equals/hashCode business, so we must
  // generate a unique UUID here or else two RefreshLatches can have the same equals.
  private val uniqueId: String = UUID.randomUUID().toString()
) : RefreshLatch {

  // Debugging enabled
  private var debug = false

  // Internal command state
  private var _refreshing = false

  // Time that the latch was last shown
  private var timeLastShown = 0L

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
    get() = _refreshing
    set(refresh) {
      if (_refreshing == refresh) {
        debugLog { "setRefreshing() called with same state, ignore." }
        return
      }

      clearCommands()
      _refreshing = refresh

      when {
        refresh -> queueShow()
        timeLastShown >= 0 -> queueHide()
        else -> hide()
      }
    }

  override fun force(isRefreshing: Boolean) {
    clearCommands()
    _refreshing = isRefreshing
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

  private fun queueHide() {
    val shownTime = SystemClock.uptimeMillis() - timeLastShown
    if (shownTime < minShowTime) {
      val delay = minShowTime - shownTime
      debugLog { "Queueing hide() to run in $delay milliseconds." }
      queueCommand(delay) { hide() }
    } else {
      hide()
    }
  }

  private inline fun queueCommand(delay: Long, crossinline command: () -> Unit) {
    handler.postDelayed({ command() }, delay)
  }

  private fun clearCommands() {
    debugLog { "Clearing command buffer." }
    handler.removeCallbacksAndMessages(null)
  }

  private fun show() {
    debugLog { "show()" }
    timeLastShown = SystemClock.uptimeMillis()
    onRefresh(true)
  }

  private fun hide() {
    debugLog { "hide()" }
    timeLastShown = 0L
    onRefresh(false)
  }
}
