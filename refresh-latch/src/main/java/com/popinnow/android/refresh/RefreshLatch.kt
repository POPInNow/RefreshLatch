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

import androidx.annotation.CheckResult

/**
 * RefreshLatch is a class which is designed to prevent UI flicker for quick refresh events.
 *
 * If a refresh event takes less time than a pre-defined delay time, the refresh
 * UI will not be shown at all.
 *
 * If a refresh event takes more time than a pre-defined delay time, the refresh
 * UI will be shown for at least a minimum amount of time, or the total amount
 * of time taken for the refresh operation - which ever is longer.
 */
interface RefreshLatch {

  /**
   * Controls whether the latch is in a refreshing state or not
   *
   * If the latch is set to a refreshing state, it will queue a show()
   * command to fire after the delay time has elapsed.
   *
   * If the latch is set to a non-refreshing state, it will fire a hide() command.
   * How it does so depends on the total amount of time that the latch has been in
   * a refreshing state up until this point. If the latch has been in a refreshing state
   * for long enough, it will immediately fire a hide() command, otherwise it will
   * queue up a hide() command to fire after at least the minimum amount of time has elapsed.
   *
   * Setting [isRefreshing] multiple times with the same value is a no-op.
   */
  var isRefreshing: Boolean

  /**
   * Force the latch into a state.
   *
   * This will clear any queued commands and will immediately force the latch into
   * either a refreshing or non-refreshing state.
   *
   * @param isRefreshing Force the [isRefreshing] state
   */
  fun force(isRefreshing: Boolean)

  /**
   * Enable noisy debugging
   *
   * @return [RefreshLatch] This instance with debugging enabled
   */
  @CheckResult
  fun enableDebugging(): RefreshLatch
}
