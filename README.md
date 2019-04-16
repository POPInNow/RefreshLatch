# refresh-latch

[RefreshLatch](https://github.com/POPinNow/RefreshLatch) is a simple class
which is designed to prevent flicker on quick refresh events.

## Install

In your `build.gradle`

```gradle
dependencies {
  def latestVersion = "0.0.1"

  implementation "com.popinnow.android.refresh:refresh-latch:$latestVersion"

}
```

## Why

UI flickering is ugly, but fast efficient data refreshes are good design.  

An application should be smooth and deliver data to users quickly via a local  
cache via a repository pattern. When a local cache is "too quick" it can  
deliver data back before the UI has properly shown its refreshing state. This  
quick delivery can cause "UI flickering" which can be disruptive to the user.  
RefreshLatch seeks to fix this problem.

If a refresh event takes less time than a pre-defined delay time, the refresh  
UI will not be shown at all.

If a refresh event takes more time than a pre-defined delay time, the refresh  
UI will be shown for at least a minimum amount of time, or the total amount  
of time taken for the refresh operation - which ever is longer.

## Quick Start

Applying `RefreshLatch` to your existing project is simple.

Before:
```kotlin
class MyActivity {

  private lateinit var swipeRefresh: SwipeRefreshLayout

  override fun onCreate(savedInstanceState: Bundle?) {
    swipeRefresh = findSwipeRefreshView()
  }

  fun onRefreshBegin() {
    swipeRefresh.isRefreshing = true
    showRefreshingUI()
  }

  fun onRefreshComplete() {
    swipeRefresh.isRefreshing = false
    hideRefreshingUI()
  }
}
```

After:
```kotlin
class MyActivity {

  private lateinit var refreshLatch: RefreshLatch

  private lateinit var swipeRefresh: SwipeRefreshLayout

  override fun onCreate(savedInstanceState: Bundle?) {
    swipeRefresh = findSwipeRefreshView()
    refreshLatch = newRefreshLatch(this) { refreshing ->
      if (refreshing) {
        swipeRefresh.isRefreshing = true
        showRefreshingUI()
      } else {
        swipeRefresh.isRefreshing = false
        hideRefreshingUI()
      }
    }
  }

  fun onRefreshBegin() {
    refreshLatch.isRefreshing = true
  }

  fun onRefreshComplete() {
    refreshLatch.isRefreshing = false
  }
}
```

## Community

The `RefreshLatch` library welcomes contributions of all kinds - it does not claim to be perfect code.  
Any improvements that can be made to the usability or the efficiency of the project will be greatly  
appreciated.

## Credits

This library is primarily built and maintained by [Peter Yamanaka](https://github.com/pyamsoft)
at [POPin](https://github.com/POPinNow).  
The RefreshLatch library is used internally in the
[POPin Android application.](https://play.google.com/store/apps/details?id=com.popinnow.gandalf)

# Support

Please feel free to make an issue on GitHub, leave as much detail as possible regarding  
the question or the problem you may be experiencing.

# Contributions

Contributions are welcome and encouraged. The project is written entirely in Kotlin and  
follows the [Square Code Style](https://github.com/square/java-code-styles) for `SquareAndroid`.

## License

Apache 2

```
Copyright (C) 2019 POP Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
