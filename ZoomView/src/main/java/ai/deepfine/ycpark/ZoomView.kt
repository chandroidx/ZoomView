package ai.deepfine.ycpark

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.github.ycpark.R
import java.io.File
import java.io.FileInputStream
import kotlin.math.*

/**
 * @Description Class설명
 * @author yc.park (DEEP.FINE)
 * @since 2021-01-29
 * @version 1.0.0
 */
class ZoomView : FrameLayout {
    //==============================================================================================
    // Constant Define
    //==============================================================================================
    private val TAG = "ZoomView"

    private val DEFAULT_GRAVITY = MINIMAP_GRAVITY_TOP or MINIMAP_GRAVITY_RIGHT

    // attrs
    var miniMapEnabled              = false
    var miniMapHeight               = 0
    var maxZoom                     = 0F
    var miniMapInBorderSize         = 0
    var miniMapInBorderColor        = 0
    var miniMapOutBorderSize        = 0
    var miniMapOutBorderColor       = 0
    private var miniMapGravity      = 0
    private var miniMapMargin       = 0
    private var miniMapMarginTop    = 0
    private var miniMapMarginBottom = 0
    private var miniMapMarginLeft   = 0
    private var miniMapMarginRight  = 0
    var imageInMiniMap              = false
    var touchable                   = false

    companion object {
        private val MINIMAP_GRAVITY_LEFT    = 2.0F.pow(0).toInt()
        private val MINIMAP_GRAVITY_RIGHT   = 2.0F.pow(1).toInt()
        private val MINIMAP_GRAVITY_TOP     = 2.0F.pow(2).toInt()
        private val MINIMAP_GRAVITY_BOTTOM  = 2.0F.pow(3).toInt()
    }
    // gravity constants


    // touching variables
    private var lastTapTime = 0L
    private var touchStartX = 0F
    private var touchStartY = 0F
    private var touchLastX  = 0F
    private var touchLastY  = 0F
    private var startd      = 0F
    private var pinching    = false
    private var lastd       = 0F
    private var lastdx1     = 0F
    private var lastdx2     = 0F
    private var lastdy1     = 0F
    private var lastdy2     = 0F
    private var scrolling   = false

    // miniMap Variables
    private val m = Matrix()
    private val p = Paint()
    private var mZoom       = 1.0F
    private var mSmoothZoom = 1.0f
    private var zoomX       = 0F
    private var zoomY       = 0F
    private var smoothZoomX = 0F
    private var smoothZoomY = 0F
    private var ch: Bitmap? = null
    private var mBitmap: Bitmap? = null
    private var minimapStartX = 0f
    private var minimapStartY = 0f


    private var tempMiniMapEnabled = false


    //==============================================================================================
    // Initialize
    //==============================================================================================
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        getAttrs(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        getAttrs(attrs, defStyleAttr)
    }

    private fun getAttrs(attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.ZoomView
        )
        setTypedArray(typedArray)
    }

    private fun getAttrs(attrs: AttributeSet?, defStyleAttr: Int) {
        val typedArray =
            context.obtainStyledAttributes(
                attrs,
                R.styleable.ZoomView, defStyleAttr, 0
            )
        setTypedArray(typedArray)
    }

    private fun setTypedArray(typedArray: TypedArray) {
        with(typedArray) {
            maxZoom = getFloat(R.styleable.ZoomView_max_zoom, 5F)
            miniMapEnabled = getBoolean(R.styleable.ZoomView_minimap_enabled, false)
            tempMiniMapEnabled = miniMapEnabled

            // miniMap width is automatically set by height
            miniMapHeight = getDimensionPixelSize(
                R.styleable.ZoomView_minimap_height,
                convertDpToPixel(120)
            )

            miniMapOutBorderSize = getDimensionPixelSize(
                R.styleable.ZoomView_minimap_out_border_size,
                convertDpToPixel(3)
            )
            miniMapInBorderSize = getDimensionPixelSize(
                R.styleable.ZoomView_minimap_in_border_size,
                convertDpToPixel(3)
            )
            miniMapOutBorderColor =
                getColor(R.styleable.ZoomView_minimap_out_border_color, Color.WHITE)
            miniMapInBorderColor =
                getColor(R.styleable.ZoomView_minimap_in_border_color, Color.BLACK)

            // set image into miniMap
            imageInMiniMap = getBoolean(R.styleable.ZoomView_image_in_minimap, true)

            // set default gravity right|top
            miniMapGravity =
                getInteger(R.styleable.ZoomView_minimap_gravity, DEFAULT_GRAVITY)
            miniMapMargin = getDimensionPixelSize(R.styleable.ZoomView_minimap_margin, 0)
            miniMapMarginTop =
                getDimensionPixelSize(R.styleable.ZoomView_minimap_margin_top, 0)
            miniMapMarginBottom =
                getDimensionPixelSize(R.styleable.ZoomView_minimap_margin_bottom, 0)
            miniMapMarginLeft =
                getDimensionPixelSize(R.styleable.ZoomView_minimap_margin_left, 0)
            miniMapMarginRight =
                getDimensionPixelSize(R.styleable.ZoomView_minimap_margin_right, 0)

            touchable = getBoolean(R.styleable.ZoomView_touchable, false)
        }
    }

    //==============================================================================================
    // Control Zoom
    //==============================================================================================
    fun getZoom() = mSmoothZoom

    fun smoothZoomTo(pfZoom: Float) {
        mSmoothZoom = clamp(1.0F, pfZoom, maxZoom)
    }

    fun smoothZoomTo(pfZoom: Float, x: Float, y: Float) {
        mSmoothZoom = clamp(1.0f, pfZoom, maxZoom)
        smoothZoomX = x
        smoothZoomY = y
    }

    fun move(x: Float, y: Float) {
        smoothZoomX += x
        smoothZoomY -= y
    }

    //==============================================================================================
    // Operation
    //==============================================================================================
    private fun convertDpToPixel(piDp: Int) =
        piDp * context.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT

    private fun clamp(min: Float, value: Float, max: Float) = max(min, min(value, max))

    private fun lerp(a: Float, b: Float, k: Float) = a + (b - a) * k

    private fun bias(a: Float, b: Float, k: Float) = if (abs(b - a) >= k) a + k * sign(b - a) else b


    override fun dispatchDraw(canvas: Canvas?) {
        tempMiniMapEnabled = mZoom != 1.0f

        // do zoom
        mZoom = if (mSmoothZoom == 1.0f) mSmoothZoom else lerp(
            bias(mZoom, mSmoothZoom, 0.05F),
            mSmoothZoom,
            0.2F
        )

        smoothZoomX =
            clamp(0.5F * width / mSmoothZoom, smoothZoomX, width - 0.5f * width / mSmoothZoom)
        smoothZoomY =
            clamp(0.5F * height / mSmoothZoom, smoothZoomY, height - 0.5f * height / mSmoothZoom)

        zoomX = lerp(bias(zoomX, smoothZoomX, 0.1F), smoothZoomX, 0.35F)
        zoomY = lerp(bias(zoomY, smoothZoomY, 0.1F), smoothZoomY, 0.35F)

        val animating = abs(mZoom - mSmoothZoom) > 0.0000001F
                || abs(zoomX - smoothZoomX) > 0.0000001F || abs(zoomY - smoothZoomY) > 0.0000001F


        // nothing to draw
        if (childCount == 0)
            return

        // prepare matrix
        m.setTranslate(0.5F * width, 0.5F * height)
        m.preScale(mZoom, mZoom)
        m.preTranslate(
            -clamp(0.5F * width / mZoom, zoomX, width - 0.5F * width / mZoom),
            -clamp(0.5F * height / mZoom, zoomY, height - 0.5F * height / mZoom)
        )

        val v: View = getChildAt(0)

        // draw using cache while animating
        if (animating && isAnimationCacheEnabled && ch != null) {
            p.color = Color.WHITE
            canvas!!.drawBitmap(ch!!, m, p)
        } else {
            ch = null

            with(canvas!!) {
                save()
                concat(m)
                v.draw(this)
                restore()
            }
        }

        // draw miniMap
        if (miniMapEnabled && tempMiniMapEnabled) {
            val miniMapColor = Color.BLACK
            p.color = miniMapColor

            val w = miniMapHeight * width.toFloat() / height
            val h = miniMapHeight

            setMiniMapLocation(canvas, w, h)

            if (imageInMiniMap) {
                v.isDrawingCacheEnabled = true
                mBitmap = v.drawingCache
                mBitmap = Bitmap.createScaledBitmap(mBitmap!!, w.toInt(), miniMapHeight, true)
                canvas.drawBitmap(mBitmap!!, 0F, 0F, p)
            }


            // MiniMap out border
            p.color = miniMapOutBorderColor

            with(canvas) {
                drawRect(
                    (-miniMapOutBorderSize).toFloat(),
                    (-miniMapOutBorderSize).toFloat(), w + miniMapOutBorderSize, 0F, p
                )

                drawRect(
                    (-miniMapOutBorderSize).toFloat(),
                    h.toFloat(), w + miniMapOutBorderSize, (h + miniMapOutBorderSize).toFloat(), p
                )

                drawRect(
                    (-miniMapOutBorderSize).toFloat(),
                    (-miniMapOutBorderSize).toFloat(), 0F, (h + miniMapOutBorderSize).toFloat(), p
                )

                drawRect(
                    w,
                    (-miniMapOutBorderSize).toFloat(), w + miniMapOutBorderSize,
                    (h + miniMapOutBorderSize).toFloat(), p
                )
            }

            p.color = 0x80000000.toInt() or 0x00ffffff and miniMapColor

            val dx = w * zoomX / width
            val dy = h * zoomY / height

            // for highlight image
            with(canvas) {
                drawRect(
                    dx - 0.5F * w / mZoom,
                    0F,
                    dx + 0.5F * w / mZoom,
                    dy - 0.5F * h / mZoom,
                    p
                )

                drawRect(
                    dx - 0.5F * w / mZoom, dy + 0.5F * h / mZoom, dx + 0.5F * w / mZoom,
                    h.toFloat(), p
                )

                drawRect(
                    0F,
                    0F,
                    dx - 0.5F * w / mZoom,
                    h.toFloat(),
                    p
                )

                drawRect(dx + 0.5F * w / mZoom, 0F, w, h.toFloat(), p)
            }


            // miniMap in border
            p.color = miniMapInBorderColor

            with(canvas) {
                drawRect(
                    dx - 0.5F * w / mZoom,
                    dy - 0.5F * h / mZoom,
                    dx + 0.5F * w / mZoom,
                    dy - 0.5F * h / mZoom + miniMapInBorderSize,
                    p
                )
                drawRect(
                    dx - 0.5F * w / mZoom,
                    dy + 0.5F * h / mZoom - miniMapInBorderSize,
                    dx + 0.5F * w / mZoom,
                    dy + 0.5F * h / mZoom,
                    p
                )

                drawRect(
                    dx - 0.5F * w / mZoom,
                    dy - 0.5F * h / mZoom,
                    dx - 0.5F * w / mZoom + miniMapInBorderSize,
                    dy + 0.5F * h / mZoom,
                    p
                )

                drawRect(
                    dx + 0.5F * w / mZoom - miniMapInBorderSize,
                    dy - 0.5F * h / mZoom,
                    dx + 0.5F * w / mZoom,
                    dy + 0.5F * h / mZoom,
                    p
                )

                translate(-10.0F, -10.0F)
            }
        }

        // redraw
        rootView.invalidate()
        invalidate()
    }

    private fun setMiniMapLocation(canvas: Canvas, w: Float, h: Int) {
        minimapStartX = 0F
        minimapStartY = 0F

        if (miniMapMargin != 0) {
            miniMapMarginLeft = miniMapMargin
            miniMapMarginRight = miniMapMargin
            miniMapMarginTop = miniMapMargin
            miniMapMarginBottom = miniMapMargin
        }

        // margin horizontal of miniMap
        if (containsFlag(miniMapGravity, MINIMAP_GRAVITY_LEFT))
            minimapStartX = (miniMapMarginLeft + miniMapOutBorderSize).toFloat()
        else if (containsFlag(miniMapGravity, MINIMAP_GRAVITY_RIGHT))
            minimapStartX = width - miniMapMarginRight - w - miniMapOutBorderSize

        // margin vertical of miniMap
        if (containsFlag(miniMapGravity, MINIMAP_GRAVITY_TOP))
            minimapStartY = (miniMapMarginTop + miniMapOutBorderSize).toFloat()
        else if (containsFlag(miniMapGravity, MINIMAP_GRAVITY_BOTTOM))
            minimapStartY = (height - miniMapMarginBottom - h - miniMapOutBorderSize).toFloat()


        // translate canvas with margin
        canvas.translate(minimapStartX, minimapStartY)
    }

    private fun containsFlag(flagSet: Int, flag: Int) = (flagSet or flag) == flagSet

}