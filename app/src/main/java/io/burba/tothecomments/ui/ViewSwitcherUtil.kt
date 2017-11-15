package io.burba.tothecomments.ui

import android.view.View
import android.widget.ViewSwitcher

fun ViewSwitcher.show(child: View) {
    displayedChild = indexOfChild(child)
}
