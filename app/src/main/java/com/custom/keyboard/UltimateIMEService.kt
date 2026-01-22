package com.custom.keyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo

class UltimateIMEService : InputMethodService() {

    override fun onCreateInputView(): View {
        val keyboardView = layoutInflater.inflate(R.layout.keyboard_layout, null).findViewById<CustomKeyboardView>(R.id.keyboard_view)
        keyboardView.service = this
        return keyboardView
    }

    fun moveCursor(direction: Int) {
        val ic = currentInputConnection
        if (direction > 0) {
            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT))
        } else {
            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT))
        }
    }

    fun smartDelete() {
        val ic = currentInputConnection
        ic.deleteSurroundingText(1, 0)
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        val isPrivate = (info?.imeOptions ?: 0) and EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING != 0
        if (isPrivate) {
            applyPrivateTheme()
        }
    }

    private fun applyPrivateTheme() {
        // Theme switching logic for incognito
    }
}
