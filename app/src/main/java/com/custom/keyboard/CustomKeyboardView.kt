package com.custom.keyboard

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

data class KeyModel(
    var label: String,
    var x: Float,
    var y: Float,
    var width: Float,
    var height: Float
)

class CustomKeyboardView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val keys = mutableListOf<KeyModel>()
    private var selectedKey: KeyModel? = null
    private var isShiftActive = false
    private var isEditMode = false // لتفعيل ميزة السحب والإفراج
    
    var service: UltimateIMEService? = null
    private val paint = Paint().apply {
        textSize = 40f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    init {
        setupKeys()
    }

    private fun setupKeys() {
        keys.clear()
        val rows = listOf(
            listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"),
            listOf("A", "S", "D", "F", "G", "H", "J", "K", "L"),
            listOf("Shift", "Z", "X", "C", "V", "B", "N", "M", "Del"),
            listOf("Space")
        )

        var currentY = 50f
        val keyHeight = 110f
        val margin = 10f

        for ((rowIndex, row) in rows.withIndex()) {
            var currentX = if (rowIndex == 1) 60f else 20f
            if (rowIndex == 3) currentX = 250f
            
            for (label in row) {
                val width = when (label) {
                    "Space" -> 500f
                    "Shift", "Del" -> 160f
                    else -> 95f
                }
                keys.add(KeyModel(label, currentX, currentY, width, keyHeight))
                currentX += width + margin
            }
            currentY += keyHeight + margin
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (key in keys) {
            // رسم خلفية المفتاح
            paint.color = if (key == selectedKey) 0xFFD2E3FC.toInt() else 0xFF2F3033.toInt()
            canvas.drawRoundRect(key.x, key.y, key.x + key.width, key.y + key.height, 15f, 15f, paint)
            
            // رسم الحرف
            paint.color = 0xFFE2E2E2.toInt()
            val displayText = if (isShiftActive && key.label.length == 1) key.label.uppercase() else key.label.lowercase()
            canvas.drawText(displayText, key.x + (key.width / 2), key.y + (key.height / 1.6f), paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x
        val touchY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                selectedKey = keys.find { 
                    touchX in it.x..(it.x + it.width) && touchY in it.y..(it.y + it.height) 
                }
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                // إذا كان وضع التعديل (Drag & Drop) مفعلاً، قم بتحريك المفتاح
                if (isEditMode) {
                    selectedKey?.let {
                        it.x = touchX - (it.width / 2)
                        it.y = touchY - (it.height / 2)
                        invalidate()
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                if (!isEditMode) {
                    selectedKey?.let { key ->
                        handleKeyPress(key)
                    }
                }
                selectedKey = null
                invalidate()
            }
        }
        return true
    }

    private fun handleKeyPress(key: KeyModel) {
        when (key.label) {
            "Shift" -> { isShiftActive = !isShiftActive; invalidate() }
            "Del" -> service?.smartDelete()
            "Space" -> service?.currentInputConnection?.commitText(" ", 1)
            else -> {
                val text = if (isShiftActive) key.label.uppercase() else key.label.lowercase()
                service?.currentInputConnection?.commitText(text, 1)
            }
        }
    }

    // وظيفة لتعديل وضع التعديل برمجياً
    fun toggleEditMode() {
        this.isEditMode = !this.isEditMode
    }
}
