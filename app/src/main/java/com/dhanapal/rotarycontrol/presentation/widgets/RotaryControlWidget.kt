package com.dhanapal.rotarycontrol.presentation.widgets

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.ImageView.ScaleType
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.GestureDetectorCompat
import com.dhanapal.rotarycontrol.R
import kotlin.math.*

/**
 * Custom/Configurable widget for rotary control
 * It is derived from https://github.com/o4oren/kotlin-rotary-knob/blob/master/app/src/main/java/geva/oren/rotaryknobdemo/RotaryKnobView.kt
 * Added step markers, tool tip to display selected value
 */
class RotaryControlWidget @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr), GestureDetector.OnGestureListener {

    private val gestureDetector: GestureDetectorCompat
    private var canvas: Canvas? = null
    private var maxValue = 100
    private var minValue = 1
    private var divider = 300f / (maxValue - minValue)
    private var value = 0
    private var initialValue = 0
    private var rotaryImageView: ImageView? = null
    private var rotaryTooltip: TextView? = null
    private var backgroundImage: Drawable? = null
    private var currentAngle = 0f

    private var externalRadius = 0f
    private var centerX = 0f
    private var centerY = 0f
    private var imageWidth = 0
    private var imageheight = 0

    private var markerLabels: Array<String>? = null

    var listener: RotaryControlWidgetListener? = null


    interface RotaryControlWidgetListener {
        fun onRotate(value: Int, displayText: String)
    }

    init {
        this.maxValue = maxValue + 1

        LayoutInflater.from(context)
            .inflate(R.layout.widget_rotary_control, this, true)

        setWillNotDraw(false)

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.RotaryControlWidget,
            0,
            0
        ).apply {
            try {
                minValue = getInt(R.styleable.RotaryControlWidget_minValue, 0)
                maxValue = getInt(R.styleable.RotaryControlWidget_maxValue, 100) + 1
                val makersListResId = getResourceId(R.styleable.RotaryControlWidget_markers, -1)
                if (makersListResId != -1) {
                    markerLabels = resources.getStringArray(makersListResId)
                }
                divider = 300f / (maxValue - minValue)
                initialValue = getInt(R.styleable.RotaryControlWidget_initialValue, 1)
                if (minValue < 0) {
                    initialValue -= minValue
                }
                backgroundImage = getDrawable(R.styleable.RotaryControlWidget_background)
                rotaryTooltip = findViewById(R.id.rotaryTooltip)
                rotaryImageView = findViewById(R.id.rotaryControlView)
                rotaryImageView?.setImageDrawable(backgroundImage)
            } finally {
                recycle()
            }
        }
        gestureDetector = GestureDetectorCompat(context, this)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        externalRadius = (min(width, height) * 0.45f)
        centerX = width / 2f
        centerY = height / 2f

        imageWidth = rotaryImageView?.width ?: 0
        imageheight = rotaryImageView?.height ?: 0
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        this.canvas = canvas

        paintMarkers(canvas)
        // paintToolTip(canvas)

        // To set initial position
        setRotaryPosition(calculateAngleByValue(initialValue), false)
    }

    override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float)
            : Boolean {
        val rotationDegrees = calculateAngle(e2.x, e2.y)
        // use only -150 to 150 range (knob min/max points)
        if (rotationDegrees >= -150 && rotationDegrees <= 150) {
            val clockWiseRotation =
                rotationDegrees >= (currentAngle + divider * 0.9f) // && rotationDegrees <= (currentAngle + (divider * 2))
            val antiClockWiseRotation =
                rotationDegrees <= (currentAngle - divider * 0.9f) // && rotationDegrees <= (currentAngle - (divider * 2))
            if (clockWiseRotation || antiClockWiseRotation) {
                setRotaryPosition(rotationDegrees)
            }
        }
        return true
    }

    fun setInitialValue(initialValue: Int) {
        var value = initialValue
        if (minValue < 0) {
            value = initialValue - minValue
        }
        this.initialValue = value
    }

    /**
     * Calculate the angle from x,y coordinates of the touch event
     * The 0,0 coordinates in android are the top left corner of the view.
     * Dividing x and y by height and width we normalize them to the range of 0 - 1 values:
     * (0,0) top left, (1,1) bottom right.
     * While x's direction is correct - going up from left to right, y's isn't - it's
     * lowest value is at the top. W
     * So we reverse it by subtracting y from 1.
     * Now x is going from 0 (most left) to 1 (most right),
     * and Y is going from 0 (most downwards) to 1 (most upwards).
     * We now need to bring 0,0 to the middle - so subtract 0.5 from both x and y.
     * now 0,0 is in the middle, 0, 0.5 is at 12 o'clock and 0.5, 0 is at 3 o'clock.
     * Now that we have the coordinates in proper cartesian coordinate system - and we can calculate
     * "theta" - the angle between the x axis and the point by calculating atan2(y,x).
     * However, theta is the angle between the x axis and the point, and it rises as we turn
     * counter-clockwise. In addition, atan2 returns (in radians) angles in the range of -180
     * through 180 degrees (-PI through PI). And we want 0 to be at 12 o'clock.
     * So we reverse the direction of the angle by prefixing it with a minus sign,
     * and add 90 to move the "zero degrees" point north (taking care to handling the range between
     * 180 and 270 degrees, bringing them to their proper values of -180 .. -90 by adding 360 to the
     * value.
     *
     * @param x - x coordinate of the touch event
     * @param y - y coordinate of the touch event
     * @return
     */
    private fun calculateAngle(x: Float, y: Float): Float {
        val px = (x / imageWidth.toFloat()) - 0.5
        val py = (1 - y / imageheight.toFloat()) - 0.5
        var angle = -(Math.toDegrees(atan2(py, px)))
            .toFloat() + 90
        if (angle > 180) angle -= 360
        return angle
    }

    private fun calculateAngleByValue(value: Int): Float {
        if (value <= 0) {
            return -150f
        }
        return (divider * value) - 150
    }

    private fun calculateAngleByValueForMarkers(value: Int): Float {
        if (value <= 0) {
            return 330f
        }
        return 330f - (330f / markerLabels!!.size * value)
    }

    private fun setRotaryPosition(rotationDegrees: Float, notifyListener: Boolean = true) {
        currentAngle = rotationDegrees
        val matrix = Matrix()
        rotaryImageView?.scaleType = ScaleType.MATRIX
        matrix.postRotate(rotationDegrees, imageWidth.toFloat() / 2, imageheight.toFloat() / 2)
        rotaryImageView?.imageMatrix = matrix


        // Calculate rotary value
        // The range is the 300 degrees between -150 and 150, so we'll add 150 to adjust the
        // range to 0 - 300
        val valueRangeDegrees = rotationDegrees + 150
        value = ((valueRangeDegrees / divider) + minValue).toInt()

        val displayText = if (markerLabels != null) {
            markerLabels!![value]
        } else {
            value.toString()
        }

        rotaryTooltip?.text = displayText

        if (notifyListener) {
            listener?.onRotate(value, displayText)
        }
    }

    private fun paintMarkers(canvas: Canvas?) {
        val paint = Paint()
        paint.color = Color.YELLOW
        paint.textSize = 30f
        if (markerLabels != null) {
            for (index in markerLabels!!.indices) {
                drawText(
                    canvas,
                    paint,
                    markerLabels!![index],
                    calculateAngleByValueForMarkers(index).toDouble()
                )
            }
        } else {
            drawText(canvas, paint, "0", 180.0)
            drawText(canvas, paint, "-", 330.0)
            drawText(canvas, paint, "+", 30.0)
        }
    }

    private fun drawText(canvas: Canvas?, paint: Paint, text: String, angle: Double) {
        val radians = angle * Math.PI / 180
        val startX = centerX + (externalRadius * sin(radians))
        val startY = centerY + (externalRadius * cos(radians))
        canvas?.drawText(text, startX.toFloat(), startY.toFloat(), paint)
    }

    private fun paintToolTip(canvas: Canvas?) {
        clearToolTip(canvas)
        paintArrowHead(canvas, 125f, centerY * 2 - 170)
    }

    private fun clearToolTip(canvas: Canvas?) {
        val paint = Paint()
        paint.color = Color.LTGRAY
        paint.strokeWidth = 1f
        paint.style = Paint.Style.FILL
        canvas?.drawRect(0f, centerY * 2 - 170, 125f, centerY * 2 - 120, paint)
    }

    private fun paintArrowHead(canvas: Canvas?, offsetX: Float, offsetY: Float) {
        val width = 25f
        val height = 50f

        val path = Path()
        path.fillType = Path.FillType.EVEN_ODD
        path.moveTo(0f, 0f)
        path.lineTo(0f, height)
        path.lineTo(height, width)
        path.close()
        path.offset(offsetX, offsetY)

        val paint = Paint()
        paint.style = Paint.Style.FILL
        paint.color = Color.LTGRAY
        canvas?.drawPath(path, paint)
    }

    private fun paintToolTipText(canvas: Canvas?, text: String) {
        clearToolTip(canvas)

        val textPaint = Paint()
        textPaint.textSize = 40f
        textPaint.color = Color.BLACK
        textPaint.textAlign = Paint.Align.CENTER
        canvas?.drawText(text, 70f, centerY * 2 - 130, textPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (gestureDetector.onTouchEvent(event))
            true
        else
            super.onTouchEvent(event)
    }

    override fun onDown(event: MotionEvent): Boolean {
        return true
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return false
    }

    override fun onFling(arg0: MotionEvent, arg1: MotionEvent, arg2: Float, arg3: Float)
            : Boolean {
        return false
    }

    override fun onLongPress(e: MotionEvent) {}

    override fun onShowPress(e: MotionEvent) {}
}