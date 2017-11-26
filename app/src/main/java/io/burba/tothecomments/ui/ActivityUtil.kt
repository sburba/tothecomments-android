package io.burba.tothecomments.ui

import android.app.Activity
import android.transition.Transition
import io.reactivex.disposables.Disposable

fun Activity.afterSharedElementTransition(fn: () -> Unit): Disposable {
    val listener = object : Transition.TransitionListener {
        override fun onTransitionResume(p0: Transition?) {}
        override fun onTransitionPause(p0: Transition?) {}
        override fun onTransitionCancel(p0: Transition?) {}
        override fun onTransitionStart(p0: Transition?) {}

        override fun onTransitionEnd(p0: Transition?) {
            fn()
        }
    }

    window.sharedElementEnterTransition.addListener(listener)

    return object : Disposable {
        private var isDisposed = false

        override fun isDisposed() = isDisposed

        override fun dispose() {
            isDisposed = true
            window.sharedElementEnterTransition.removeListener(listener)
        }
    }
}
