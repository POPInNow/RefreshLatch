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

@file:JvmName("RefreshLatches")

package com.popinnow.android.refresh

import androidx.annotation.CheckResult
import androidx.lifecycle.Lifecycle.Event.ON_DESTROY
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.TimeUnit

private const val DEFAULT_DELAY_TIME = 300L
private const val DEFAULT_MIN_SHOW_TIME = 700L

/**
 * Create a new RefreshLatch with default delay time and default minimum shown time
 *
 * @param owner [LifecycleOwner] to attach to, will self clean up on [ON_DESTROY]
 * @param onRefresh Refresh callback
 * @return [RefreshLatch] New unique [RefreshLatch] instance
 */
@CheckResult
fun newRefreshLatch(
  owner: LifecycleOwner,
  onRefresh: (refreshing: Boolean) -> Unit
): RefreshLatch {
  return RefreshLatchImpl(owner, DEFAULT_DELAY_TIME, DEFAULT_MIN_SHOW_TIME, onRefresh)
}

/**
 * Create a new RefreshLatch with custom delay time and default minimum shown time
 *
 * @param owner [LifecycleOwner] to attach to, will self clean up on [ON_DESTROY]
 * @param time Delay time amount
 * @param timeUnit Delay time unit
 * @param onRefresh Refresh callback
 * @return [RefreshLatch] New unique [RefreshLatch] instance
 */
@CheckResult
fun newRefreshLatchWithDelay(
  owner: LifecycleOwner,
  time: Long,
  timeUnit: TimeUnit,
  onRefresh: (refreshing: Boolean) -> Unit
): RefreshLatch {
  return RefreshLatchImpl(owner, timeUnit.toMillis(time), DEFAULT_MIN_SHOW_TIME, onRefresh)
}

/**
 * Create a new RefreshLatch with default delay time and custom minimum shown time
 *
 * @param owner [LifecycleOwner] to attach to, will self clean up on [ON_DESTROY]
 * @param time Minimum shown time amount
 * @param timeUnit Minimum shown time unit
 * @param onRefresh Refresh callback
 * @return [RefreshLatch] New unique [RefreshLatch] instance
 */
@CheckResult
fun newRefreshLatchWithMinimumShowingTime(
  owner: LifecycleOwner,
  time: Long,
  timeUnit: TimeUnit,
  onRefresh: (refreshing: Boolean) -> Unit
): RefreshLatch {
  return RefreshLatchImpl(owner, DEFAULT_DELAY_TIME, timeUnit.toMillis(time), onRefresh)
}

/**
 * Create a new RefreshLatch with custom delay time and custom minimum shown time
 *
 * @param owner [LifecycleOwner] to attach to, will self clean up on [ON_DESTROY]
 * @param delayTime Delay time amount
 * @param delayTimeUnit Delay time unit
 * @param minShowTime Minimum shown time amount
 * @param minShowTimeUnit Minimum shown time unit
 * @param onRefresh Refresh callback
 * @return [RefreshLatch] New unique [RefreshLatch] instance
 */
@CheckResult
fun newRefreshLatch(
  owner: LifecycleOwner,
  delayTime: Long,
  delayTimeUnit: TimeUnit,
  minShowTime: Long,
  minShowTimeUnit: TimeUnit,
  onRefresh: (refreshing: Boolean) -> Unit
): RefreshLatch {
  return RefreshLatchImpl(
      owner,
      delayTimeUnit.toMillis(delayTime),
      minShowTimeUnit.toMillis(minShowTime),
      onRefresh
  )
}
