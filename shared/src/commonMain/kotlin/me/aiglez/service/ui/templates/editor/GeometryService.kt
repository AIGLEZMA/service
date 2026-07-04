package me.aiglez.service.ui.templates.editor

import me.aiglez.service.domain.models.TemplateElement
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.roundToInt

data class PagePoint(
    val x: Float,
    val y: Float,
)

data class PageRect(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
) {
    val right: Float get() = x + width
    val bottom: Float get() = y + height
    val centerX: Float get() = x + width / 2f
    val centerY: Float get() = y + height / 2f

    fun contains(point: PagePoint): Boolean {
        return point.x in x..right && point.y in y..bottom
    }
}

data class PageSize(
    val width: Float,
    val height: Float,
)

data class SnapGuideSet(
    val pageSize: PageSize,
    val margin: Float,
    val printableInset: Float,
    val bleedInset: Float,
    val trimInset: Float,
    val safeAreaInset: Float,
    val headerGuide: Float,
    val footerGuide: Float,
    val gridSize: Float,
    val columns: Int,
    val rows: Int,
    val baselineGrid: Float,
    val customVerticalGuides: List<Float> = emptyList(),
    val customHorizontalGuides: List<Float> = emptyList(),
    val rulerVerticalGuides: List<Float> = emptyList(),
    val rulerHorizontalGuides: List<Float> = emptyList(),
    val includePageCenter: Boolean = true,
)

data class SnapResult(
    val bounds: PageRect,
    val verticalGuide: Float? = null,
    val horizontalGuide: Float? = null,
)

data class CanvasMetrics(
    val zoom: Float,
    val viewportScrollX: Float = 0f,
    val viewportScrollY: Float = 0f,
    val workspaceOriginX: Float = 0f,
    val workspaceOriginY: Float = 0f,
    val pageOriginX: Float = 0f,
    val pageOriginY: Float = 0f,
)

enum class ResizeHandle {
    TopLeft,
    Top,
    TopRight,
    Right,
    BottomRight,
    Bottom,
    BottomLeft,
    Left,
}

object GeometryService {
    const val MinElementSize = 8f

    fun screenToWorkspace(screen: PagePoint, metrics: CanvasMetrics): PagePoint {
        val safeZoom = metrics.zoom.coerceAtLeast(0.01f)
        return PagePoint(
            x = (screen.x + metrics.viewportScrollX - metrics.workspaceOriginX) / safeZoom,
            y = (screen.y + metrics.viewportScrollY - metrics.workspaceOriginY) / safeZoom,
        )
    }

    fun workspaceToPage(workspace: PagePoint, metrics: CanvasMetrics): PagePoint {
        return PagePoint(
            x = workspace.x - metrics.pageOriginX,
            y = workspace.y - metrics.pageOriginY,
        )
    }

    fun screenToPage(screen: PagePoint, metrics: CanvasMetrics): PagePoint {
        return workspaceToPage(screenToWorkspace(screen, metrics), metrics)
    }

    fun pageToScreen(page: PagePoint, metrics: CanvasMetrics): PagePoint {
        val safeZoom = metrics.zoom.coerceAtLeast(0.01f)
        return PagePoint(
            x = (page.x + metrics.pageOriginX) * safeZoom + metrics.workspaceOriginX - metrics.viewportScrollX,
            y = (page.y + metrics.pageOriginY) * safeZoom + metrics.workspaceOriginY - metrics.viewportScrollY,
        )
    }

    fun moveElement(originalBounds: PageRect, dragStartPage: PagePoint, currentPage: PagePoint): PageRect {
        return originalBounds.copy(
            x = originalBounds.x + currentPage.x - dragStartPage.x,
            y = originalBounds.y + currentPage.y - dragStartPage.y,
        )
    }

    fun resizeElement(
        originalBounds: PageRect,
        handle: ResizeHandle,
        dragStartPage: PagePoint,
        currentPage: PagePoint,
        minSize: Float = MinElementSize,
        constrainProportions: Boolean = false,
        resizeFromCenter: Boolean = false,
    ): PageRect {
        val dx = currentPage.x - dragStartPage.x
        val dy = currentPage.y - dragStartPage.y
        var left = originalBounds.x
        var top = originalBounds.y
        var right = originalBounds.right
        var bottom = originalBounds.bottom

        when (handle) {
            ResizeHandle.TopLeft -> {
                left += dx
                top += dy
                if (resizeFromCenter) {
                    right -= dx
                    bottom -= dy
                }
            }
            ResizeHandle.Top -> {
                top += dy
                if (resizeFromCenter) bottom -= dy
            }
            ResizeHandle.TopRight -> {
                right += dx
                top += dy
                if (resizeFromCenter) {
                    left -= dx
                    bottom -= dy
                }
            }
            ResizeHandle.Right -> {
                right += dx
                if (resizeFromCenter) left -= dx
            }
            ResizeHandle.BottomRight -> {
                right += dx
                bottom += dy
                if (resizeFromCenter) {
                    left -= dx
                    top -= dy
                }
            }
            ResizeHandle.Bottom -> {
                bottom += dy
                if (resizeFromCenter) top -= dy
            }
            ResizeHandle.BottomLeft -> {
                left += dx
                bottom += dy
                if (resizeFromCenter) {
                    right -= dx
                    top -= dy
                }
            }
            ResizeHandle.Left -> {
                left += dx
                if (resizeFromCenter) right -= dx
            }
        }

        if (constrainProportions && originalBounds.height > 0f) {
            val ratio = originalBounds.width / originalBounds.height
            val changesWidth = handle.changesWidth
            val changesHeight = handle.changesHeight
            val widthDelta = abs(right - left - originalBounds.width)
            val heightDelta = abs(bottom - top - originalBounds.height)

            if (changesWidth && (!changesHeight || widthDelta >= heightDelta)) {
                val targetHeight = ((right - left) / ratio).coerceAtLeast(minSize)
                val adjusted = setHeight(
                    top = top,
                    bottom = bottom,
                    targetHeight = targetHeight,
                    handle = handle,
                    fromCenter = resizeFromCenter,
                )
                top = adjusted.first
                bottom = adjusted.second
            } else if (changesHeight) {
                val targetWidth = ((bottom - top) * ratio).coerceAtLeast(minSize)
                val adjusted = setWidth(
                    left = left,
                    right = right,
                    targetWidth = targetWidth,
                    handle = handle,
                    fromCenter = resizeFromCenter,
                )
                left = adjusted.first
                right = adjusted.second
            }
        }

        if (right - left < minSize) {
            if (resizeFromCenter) {
                val center = (left + right) / 2f
                left = center - minSize / 2f
                right = center + minSize / 2f
            } else if (handle == ResizeHandle.Left || handle == ResizeHandle.TopLeft || handle == ResizeHandle.BottomLeft) {
                left = right - minSize
            } else {
                right = left + minSize
            }
        }
        if (bottom - top < minSize) {
            if (resizeFromCenter) {
                val center = (top + bottom) / 2f
                top = center - minSize / 2f
                bottom = center + minSize / 2f
            } else if (handle == ResizeHandle.Top || handle == ResizeHandle.TopLeft || handle == ResizeHandle.TopRight) {
                top = bottom - minSize
            } else {
                bottom = top + minSize
            }
        }

        return PageRect(left, top, right - left, bottom - top)
    }

    fun moveElementConstrained(originalBounds: PageRect, dragStartPage: PagePoint, currentPage: PagePoint): PageRect {
        val dx = currentPage.x - dragStartPage.x
        val dy = currentPage.y - dragStartPage.y
        return if (abs(dx) >= abs(dy)) {
            originalBounds.copy(x = originalBounds.x + dx)
        } else {
            originalBounds.copy(y = originalBounds.y + dy)
        }
    }

    fun snapBounds(
        bounds: PageRect,
        guideSet: SnapGuideSet,
        otherElements: List<TemplateElement>,
        threshold: Float,
        mode: ResizeHandle? = null,
        resizeFromCenter: Boolean = false,
    ): SnapResult {
        if (threshold <= 0f) return SnapResult(bounds)

        val otherBounds = otherElements
            .filter { it.visible }
            .map(::getElementBounds)

        val xTargets = verticalTargets(guideSet, otherBounds)
        val yTargets = horizontalTargets(guideSet, otherBounds)
        val widthTargets = otherBounds.map { it.width }.distinctNear()
        val heightTargets = otherBounds.map { it.height }.distinctNear()

        var snapped = bounds
        var verticalGuide: Float? = null
        var horizontalGuide: Float? = null

        if (mode == null) {
            nearestAlignment(
                listOf(bounds.x, bounds.centerX, bounds.right),
                xTargets + equalSpacingXTargets(bounds, otherBounds),
                threshold,
            )?.let { match ->
                snapped = snapped.copy(x = snapped.x + match.delta)
                verticalGuide = match.target
            }
            nearestAlignment(
                listOf(bounds.y, bounds.centerY, bounds.bottom),
                yTargets + equalSpacingYTargets(bounds, otherBounds),
                threshold,
            )?.let { match ->
                snapped = snapped.copy(y = snapped.y + match.delta)
                horizontalGuide = match.target
            }
            return SnapResult(snapped, verticalGuide, horizontalGuide)
        }

        if (mode.changesWidth) {
            resizeAxisSnap(
                start = snapped.x,
                end = snapped.right,
                movingStart = mode == ResizeHandle.Left || mode == ResizeHandle.TopLeft || mode == ResizeHandle.BottomLeft,
                movingEnd = mode == ResizeHandle.Right || mode == ResizeHandle.TopRight || mode == ResizeHandle.BottomRight,
                targets = xTargets,
                sizeTargets = widthTargets,
                threshold = threshold,
                fromCenter = resizeFromCenter,
            )?.let { axis ->
                snapped = snapped.copy(x = axis.start, width = (axis.end - axis.start).coerceAtLeast(MinElementSize))
                verticalGuide = axis.guide
            }
        }
        if (mode.changesHeight) {
            resizeAxisSnap(
                start = snapped.y,
                end = snapped.bottom,
                movingStart = mode == ResizeHandle.Top || mode == ResizeHandle.TopLeft || mode == ResizeHandle.TopRight,
                movingEnd = mode == ResizeHandle.Bottom || mode == ResizeHandle.BottomLeft || mode == ResizeHandle.BottomRight,
                targets = yTargets,
                sizeTargets = heightTargets,
                threshold = threshold,
                fromCenter = resizeFromCenter,
            )?.let { axis ->
                snapped = snapped.copy(y = axis.start, height = (axis.end - axis.start).coerceAtLeast(MinElementSize))
                horizontalGuide = axis.guide
            }
        }

        return SnapResult(snapped, verticalGuide, horizontalGuide)
    }

    fun snapRotation(raw: Float, increment: Float = 45f, threshold: Float = 2f): Float {
        if (increment <= 0f) return raw
        val snapped = (raw / increment).roundToInt() * increment
        return if (abs(snapped - raw) <= threshold) snapped else raw
    }

    fun getElementBounds(element: TemplateElement): PageRect {
        return PageRect(element.x, element.y, element.width, element.height)
    }

    fun hitTestElement(elements: List<TemplateElement>, point: PagePoint): TemplateElement? {
        var bestElement: TemplateElement? = null
        var bestIndex = -1
        elements.forEachIndexed { index, element ->
            if (!element.visible) return@forEachIndexed
            val hit = when (element) {
                is TemplateElement.Line -> hitTestLine(element, point, tolerance = 8f)
                else -> getElementBounds(element).contains(point)
            }
            if (!hit) return@forEachIndexed
            val currentBest = bestElement
            if (
                currentBest == null ||
                element.zIndex > currentBest.zIndex ||
                (element.zIndex == currentBest.zIndex && index > bestIndex)
            ) {
                bestElement = element
                bestIndex = index
            }
        }
        return bestElement
    }

    private fun hitTestLine(line: TemplateElement.Line, point: PagePoint, tolerance: Float): Boolean {
        val ax = line.x1
        val ay = line.y1
        val bx = line.x2
        val by = line.y2
        val abx = bx - ax
        val aby = by - ay
        val lengthSquared = abx * abx + aby * aby
        if (lengthSquared == 0f) {
            return hypot(point.x - ax, point.y - ay) <= tolerance
        }
        val t = (((point.x - ax) * abx + (point.y - ay) * aby) / lengthSquared).coerceIn(0f, 1f)
        val closestX = ax + t * abx
        val closestY = ay + t * aby
        return hypot(point.x - closestX, point.y - closestY) <= tolerance.coerceAtLeast(line.thickness / 2f)
    }

    fun hitTestResizeHandle(
        bounds: PageRect,
        point: PagePoint,
        handleSize: Float,
    ): ResizeHandle? {
        val half = handleSize / 2f
        val insideBounds = bounds.contains(point)
        val inwardHalf = if (insideBounds) constrainedInwardResizeHitHalf(half, bounds) else half

        var closestHandle: ResizeHandle? = null
        var closestHandleDistance = Float.MAX_VALUE
        handleCenters(bounds).forEach { (handle, center) ->
            if (
                point.x in (center.x - inwardHalf)..(center.x + inwardHalf) &&
                point.y in (center.y - inwardHalf)..(center.y + inwardHalf)
            ) {
                val dx = point.x - center.x
                val dy = point.y - center.y
                val distance = dx * dx + dy * dy
                if (distance < closestHandleDistance) {
                    closestHandle = handle
                    closestHandleDistance = distance
                }
            }
        }
        closestHandle?.let { return it }

        val topRange = (bounds.y - half)..(bounds.y + inwardHalf)
        val rightRange = (bounds.right - inwardHalf)..(bounds.right + half)
        val bottomRange = (bounds.bottom - inwardHalf)..(bounds.bottom + half)
        val leftRange = (bounds.x - half)..(bounds.x + inwardHalf)

        return when {
            point.y in topRange && point.x in bounds.x..bounds.right -> ResizeHandle.Top
            point.x in rightRange && point.y in bounds.y..bounds.bottom -> ResizeHandle.Right
            point.y in bottomRange && point.x in bounds.x..bounds.right -> ResizeHandle.Bottom
            point.x in leftRange && point.y in bounds.y..bounds.bottom -> ResizeHandle.Left
            else -> null
        }
    }

    fun handleCenters(bounds: PageRect): List<Pair<ResizeHandle, PagePoint>> {
        val midX = bounds.x + bounds.width / 2f
        val midY = bounds.y + bounds.height / 2f
        return listOf(
            ResizeHandle.TopLeft to PagePoint(bounds.x, bounds.y),
            ResizeHandle.Top to PagePoint(midX, bounds.y),
            ResizeHandle.TopRight to PagePoint(bounds.right, bounds.y),
            ResizeHandle.Right to PagePoint(bounds.right, midY),
            ResizeHandle.BottomRight to PagePoint(bounds.right, bounds.bottom),
            ResizeHandle.Bottom to PagePoint(midX, bounds.bottom),
            ResizeHandle.BottomLeft to PagePoint(bounds.x, bounds.bottom),
            ResizeHandle.Left to PagePoint(bounds.x, midY),
        )
    }

    private fun constrainedInwardResizeHitHalf(half: Float, bounds: PageRect): Float {
        val smallestDimension = minOf(bounds.width, bounds.height)
        return minOf(half, (smallestDimension * 0.22f).coerceAtLeast(3f))
    }

    fun verticalGuidePositions(guideSet: SnapGuideSet): List<Float> {
        return verticalTargets(guideSet, emptyList()).distinctNear()
    }

    fun horizontalGuidePositions(guideSet: SnapGuideSet): List<Float> {
        return horizontalTargets(guideSet, emptyList()).distinctNear()
    }

    private val ResizeHandle.changesWidth: Boolean
        get() = this != ResizeHandle.Top && this != ResizeHandle.Bottom

    private val ResizeHandle.changesHeight: Boolean
        get() = this != ResizeHandle.Left && this != ResizeHandle.Right

    private data class AlignmentMatch(val delta: Float, val target: Float)

    private data class AxisSnap(val start: Float, val end: Float, val guide: Float?)

    private fun nearestAlignment(values: List<Float>, targets: List<Float>, threshold: Float): AlignmentMatch? {
        var best: AlignmentMatch? = null
        var bestDistance = Float.MAX_VALUE
        values.forEach { value ->
            targets.forEach { target ->
                val distance = abs(target - value)
                if (distance <= threshold && distance < bestDistance) {
                    bestDistance = distance
                    best = AlignmentMatch(delta = target - value, target = target)
                }
            }
        }
        return best
    }

    private fun resizeAxisSnap(
        start: Float,
        end: Float,
        movingStart: Boolean,
        movingEnd: Boolean,
        targets: List<Float>,
        sizeTargets: List<Float>,
        threshold: Float,
        fromCenter: Boolean,
    ): AxisSnap? {
        val center = (start + end) / 2f
        var best: AxisSnap? = null
        var bestDistance = Float.MAX_VALUE

        fun consider(nextStart: Float, nextEnd: Float, guide: Float?, distance: Float) {
            if (nextEnd - nextStart < MinElementSize) return
            if (distance <= threshold && distance < bestDistance) {
                bestDistance = distance
                best = AxisSnap(nextStart, nextEnd, guide)
            }
        }

        if (fromCenter) {
            targets.forEach { target ->
                val size = abs(target - center) * 2f
                consider(center - size / 2f, center + size / 2f, target, abs(target - start).coerceAtMost(abs(target - end)))
            }
            sizeTargets.forEach { targetSize ->
                consider(center - targetSize / 2f, center + targetSize / 2f, null, abs(targetSize - (end - start)))
            }
            return best
        }

        if (movingStart) {
            nearestAlignment(listOf(start), targets, threshold)?.let {
                consider(it.target, end, it.target, abs(it.delta))
            }
            sizeTargets.forEach { targetSize ->
                consider(end - targetSize, end, null, abs(targetSize - (end - start)))
            }
        }
        if (movingEnd) {
            nearestAlignment(listOf(end), targets, threshold)?.let {
                consider(start, it.target, it.target, abs(it.delta))
            }
            sizeTargets.forEach { targetSize ->
                consider(start, start + targetSize, null, abs(targetSize - (end - start)))
            }
        }
        return best
    }

    private fun verticalTargets(guideSet: SnapGuideSet, objectBounds: List<PageRect>): List<Float> {
        val page = guideSet.pageSize
        return buildList {
            addAll(listOf(0f, page.width))
            if (guideSet.includePageCenter) add(page.width / 2f)
            addAll(edgePair(guideSet.margin, page.width))
            addAll(edgePair(guideSet.printableInset, page.width))
            addAll(edgePair(guideSet.bleedInset, page.width))
            addAll(edgePair(guideSet.trimInset, page.width))
            addAll(edgePair(guideSet.safeAreaInset, page.width))
            addAll(guideSet.customVerticalGuides)
            addAll(guideSet.rulerVerticalGuides)
            addAll(gridPositions(page.width, guideSet.gridSize))
            addAll(documentDivisions(page.width, guideSet.margin, guideSet.columns))
            objectBounds.forEach { addAll(listOf(it.x, it.centerX, it.right)) }
        }.filter { it in 0f..page.width }.distinctNear()
    }

    private fun horizontalTargets(guideSet: SnapGuideSet, objectBounds: List<PageRect>): List<Float> {
        val page = guideSet.pageSize
        return buildList {
            addAll(listOf(0f, page.height))
            if (guideSet.includePageCenter) add(page.height / 2f)
            addAll(edgePair(guideSet.margin, page.height))
            addAll(edgePair(guideSet.printableInset, page.height))
            addAll(edgePair(guideSet.bleedInset, page.height))
            addAll(edgePair(guideSet.trimInset, page.height))
            addAll(edgePair(guideSet.safeAreaInset, page.height))
            addAll(listOf(guideSet.headerGuide, guideSet.footerGuide))
            addAll(guideSet.customHorizontalGuides)
            addAll(guideSet.rulerHorizontalGuides)
            addAll(gridPositions(page.height, guideSet.gridSize))
            addAll(documentDivisions(page.height, guideSet.margin, guideSet.rows))
            addAll(gridPositions(page.height, guideSet.baselineGrid))
            objectBounds.forEach { addAll(listOf(it.y, it.centerY, it.bottom)) }
        }.filter { it in 0f..page.height }.distinctNear()
    }

    private fun edgePair(inset: Float, length: Float): List<Float> {
        return if (inset <= 0f) emptyList() else listOf(inset, length - inset)
    }

    private fun gridPositions(length: Float, step: Float): List<Float> {
        if (step <= 0f) return emptyList()
        val positions = mutableListOf<Float>()
        var value = 0f
        while (value <= length) {
            positions += value
            value += step
        }
        return positions
    }

    private fun documentDivisions(length: Float, margin: Float, count: Int): List<Float> {
        if (count <= 1) return emptyList()
        val start = margin.coerceIn(0f, length / 2f)
        val end = (length - margin).coerceAtLeast(start)
        val step = (end - start) / count
        return buildList {
            add(start)
            add(end)
            for (index in 1 until count) {
                add(start + step * index)
            }
        }
    }

    private fun equalSpacingXTargets(bounds: PageRect, otherBounds: List<PageRect>): List<Float> {
        val sorted = otherBounds.sortedBy { it.x }
        return buildList {
            sorted.zipWithNext().forEach { (left, right) ->
                val gap = right.x - left.right
                if (gap >= 0f) {
                    add(right.right + gap)
                    add(left.x - gap - bounds.width)
                    val between = right.x - left.right
                    if (between >= bounds.width) add(left.right + (between - bounds.width) / 2f)
                }
            }
        }
    }

    private fun equalSpacingYTargets(bounds: PageRect, otherBounds: List<PageRect>): List<Float> {
        val sorted = otherBounds.sortedBy { it.y }
        return buildList {
            sorted.zipWithNext().forEach { (top, bottom) ->
                val gap = bottom.y - top.bottom
                if (gap >= 0f) {
                    add(bottom.bottom + gap)
                    add(top.y - gap - bounds.height)
                    val between = bottom.y - top.bottom
                    if (between >= bounds.height) add(top.bottom + (between - bounds.height) / 2f)
                }
            }
        }
    }

    private fun List<Float>.distinctNear(): List<Float> {
        return sorted().fold(emptyList()) { acc, value ->
            if (acc.lastOrNull()?.let { abs(it - value) < 0.01f } == true) acc else acc + value
        }
    }

    private fun setWidth(
        left: Float,
        right: Float,
        targetWidth: Float,
        handle: ResizeHandle,
        fromCenter: Boolean,
    ): Pair<Float, Float> {
        if (fromCenter) {
            val center = (left + right) / 2f
            return center - targetWidth / 2f to center + targetWidth / 2f
        }
        return if (handle == ResizeHandle.Left || handle == ResizeHandle.TopLeft || handle == ResizeHandle.BottomLeft) {
            right - targetWidth to right
        } else {
            left to left + targetWidth
        }
    }

    private fun setHeight(
        top: Float,
        bottom: Float,
        targetHeight: Float,
        handle: ResizeHandle,
        fromCenter: Boolean,
    ): Pair<Float, Float> {
        if (fromCenter) {
            val center = (top + bottom) / 2f
            return center - targetHeight / 2f to center + targetHeight / 2f
        }
        return if (handle == ResizeHandle.Top || handle == ResizeHandle.TopLeft || handle == ResizeHandle.TopRight) {
            bottom - targetHeight to bottom
        } else {
            top to top + targetHeight
        }
    }
}



