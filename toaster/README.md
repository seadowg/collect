# toaster

Allows you to interact with [Android's Toast API](https://developer.android.com/guide/topics/ui/notifiers/toasts) through an interface rather than a set of static helpers:

```kotlin
class ToastActivity : Activity() {
    
    @Inject
    lateinit var toaster: Toaster

    override fun onCreate(savedInstanceState: Bundle?) {
        toaster.toast("Hello, world!")
    }
}
```

A simple implementation of `Toaster` is provided:

```kotlin
val toaster = ApplicationToaster(applicationContext)
```

`ApplicationToaster` is a basic wrapper around `Toast.makeText` and `Toast#show` but you could easily create your implementations or extend `ApplicationToaster`. For instance, here's a `Toaster` that also logs toasts using Timber:

```kotlin
class TimberToaster(application: Application) : ApplicationToaster(application) {

    override fun toast(message: String, duration: Int, beforeShow: ((Toast) -> Unit)?) {
        super.toast(message, duration, beforeShow)
        Timber.i("Toast shown: \"$message\"")
    }
}
```

Or, you could use the provided `RecordingToaster` fake to test that your components show the right toast:

```kotlin
val toaster = RecordingToaster()
val myComponent = MyComponent(toaster)

myComponent.showToast()
assertThat(toaster.popRecordedToasts(), contains("Hello, World!"))
```

The companion [`toaster-espresso`](https://github.com/getodk/collect/tree/master/toaster-espresso) library can be used to make assertions on `RecordingToast` in instrumented tests.


