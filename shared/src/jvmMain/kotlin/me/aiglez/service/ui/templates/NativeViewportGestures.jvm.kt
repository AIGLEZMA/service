package me.aiglez.service.ui.templates

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import java.awt.AWTEvent
import java.awt.Component
import java.awt.KeyboardFocusManager
import java.awt.MouseInfo
import java.awt.Point
import java.awt.Toolkit
import java.awt.Window
import java.awt.event.AWTEventListener
import java.awt.event.MouseEvent
import java.beans.PropertyChangeListener
import java.lang.reflect.Proxy
import javax.swing.JComponent
import javax.swing.RootPaneContainer
import javax.swing.SwingUtilities
import kotlin.math.exp

private const val NativePinchSensitivity = 2.0f

@Composable
internal actual fun rememberNativeViewportGesture(): NativeViewportGesture? {
    var gesture by remember { mutableStateOf<NativeViewportGesture?>(null) }
    DisposableEffect(Unit) {
        var sequence = 0L
        var lastPointerPositionInWindow: Offset? = null
        var gestureTarget: JComponent? = null
        var gestureListener: Any? = null
        val keyboardFocusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager()

        fun emitZoom(rawMagnification: Double) {
            val position = lastPointerPositionInWindow
                ?: keyboardFocusManager.activeWindow?.currentPointerPositionInContentWindow()
                ?: return
            val zoomFactor = exp((rawMagnification.toFloat() * NativePinchSensitivity).coerceIn(-0.5f, 0.5f))
            gesture = NativeViewportGesture(
                sequence = ++sequence,
                kind = NativeViewportGestureKind.Zoom,
                positionInWindow = position,
                zoomFactor = zoomFactor,
            )
        }

        fun detachMagnificationListener() {
            val target = gestureTarget ?: return
            val listener = gestureListener ?: return
            runCatching {
                val utilities = Class.forName("com.apple.eawt.event.GestureUtilities")
                val gestureListenerClass = Class.forName("com.apple.eawt.event.GestureListener")
                utilities
                    .getMethod("removeGestureListenerFrom", JComponent::class.java, gestureListenerClass)
                    .invoke(null, target, listener)
            }
            gestureTarget = null
            gestureListener = null
        }

        fun attachMagnificationListener(component: Component?) {
            val target = component?.gestureHostJComponent()
                ?: keyboardFocusManager.activeWindow?.gestureHostJComponent()
                ?: return
            if (target == gestureTarget) return
            detachMagnificationListener()
            runCatching {
                val utilities = Class.forName("com.apple.eawt.event.GestureUtilities")
                val magnificationListenerClass = Class.forName("com.apple.eawt.event.MagnificationListener")
                val listener = Proxy.newProxyInstance(
                    magnificationListenerClass.classLoader,
                    arrayOf(magnificationListenerClass),
                ) { _, method, args ->
                    if (method.name == "magnify") {
                        val event = args?.firstOrNull()
                        val magnification = event
                            ?.javaClass
                            ?.getMethod("getMagnification")
                            ?.invoke(event) as? Double
                        if (magnification != null) emitZoom(magnification)
                        runCatching { event?.javaClass?.getMethod("consume")?.invoke(event) }
                    }
                    null
                }
                val gestureListenerClass = Class.forName("com.apple.eawt.event.GestureListener")
                utilities
                    .getMethod("addGestureListenerTo", JComponent::class.java, gestureListenerClass)
                    .invoke(null, target, listener)
                gestureTarget = target
                gestureListener = listener
            }
        }

        val mouseListener = AWTEventListener { event ->
            if (event is MouseEvent) {
                lastPointerPositionInWindow = event.positionInContentWindow()
                attachMagnificationListener(event.component)
            }
        }
        val focusListener = PropertyChangeListener { event ->
            if (event.propertyName == "focusOwner") {
                attachMagnificationListener(event.newValue as? Component)
            }
        }

        Toolkit.getDefaultToolkit().addAWTEventListener(
            mouseListener,
            AWTEvent.MOUSE_EVENT_MASK or AWTEvent.MOUSE_MOTION_EVENT_MASK,
        )
        keyboardFocusManager.addPropertyChangeListener("focusOwner", focusListener)
        attachMagnificationListener(keyboardFocusManager.focusOwner)

        onDispose {
            Toolkit.getDefaultToolkit().removeAWTEventListener(mouseListener)
            keyboardFocusManager.removePropertyChangeListener("focusOwner", focusListener)
            detachMagnificationListener()
        }
    }
    return gesture
}

private fun Component.gestureHostJComponent(): JComponent? {
    val windowRoot = SwingUtilities.getWindowAncestor(this)
        ?.let { it as? RootPaneContainer }
        ?.rootPane
    return windowRoot ?: nearestJComponent()
}

private fun Window.gestureHostJComponent(): JComponent? {
    return (this as? RootPaneContainer)?.rootPane ?: nearestJComponent()
}

private fun Component.nearestJComponent(): JComponent? {
    return when (this) {
        is JComponent -> this
        is RootPaneContainer -> rootPane
        else -> SwingUtilities.getAncestorOfClass(JComponent::class.java, this) as? JComponent
    }
}

private fun MouseEvent.positionInContentWindow(): Offset? {
    val component = component ?: return null
    val window = SwingUtilities.getWindowAncestor(component) ?: KeyboardFocusManager
        .getCurrentKeyboardFocusManager()
        .activeWindow
        ?: return null
    val origin = window.contentOriginOnScreen() ?: window.locationOnScreenOrNull() ?: return null
    return Offset((xOnScreen - origin.x).toFloat(), (yOnScreen - origin.y).toFloat())
}

private fun Window.currentPointerPositionInContentWindow(): Offset? {
    val pointer = MouseInfo.getPointerInfo()?.location ?: return null
    val origin = contentOriginOnScreen() ?: locationOnScreenOrNull() ?: return null
    return Offset((pointer.x - origin.x).toFloat(), (pointer.y - origin.y).toFloat())
}

private fun Window.contentOriginOnScreen(): Point? {
    val contentPane = (this as? RootPaneContainer)?.contentPane ?: return null
    return contentPane.locationOnScreenOrNull()
}

private fun Component.locationOnScreenOrNull(): Point? {
    return runCatching { locationOnScreen }.getOrNull()
}
