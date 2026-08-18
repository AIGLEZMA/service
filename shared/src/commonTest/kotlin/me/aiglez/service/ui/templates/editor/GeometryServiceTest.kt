package me.aiglez.service.ui.templates.editor

import me.aiglez.service.domain.models.TemplateElement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GeometryServiceTest {
    @Test
    fun pageRectCenterUsesBoundsNotOrigin() {
        val rect = PageRect(x = 100f, y = 80f, width = 40f, height = 30f)

        assertEquals(120f, rect.centerX)
        assertEquals(95f, rect.centerY)
    }

    @Test
    fun screenToPageAccountsForZoomAndOffsets() {
        val metrics = CanvasMetrics(
            zoom = 2f,
            viewportScrollX = 10f,
            viewportScrollY = 20f,
            workspaceOriginX = 30f,
            workspaceOriginY = 40f,
            pageOriginX = 5f,
            pageOriginY = 6f,
        )

        val page = GeometryService.screenToPage(PagePoint(130f, 220f), metrics)

        assertEquals(50f, page.x)
        assertEquals(100f - 6f, page.y)
    }

    @Test
    fun resizeFromLeftMovesXAndShrinksWidth() {
        val original = PageRect(x = 100f, y = 80f, width = 200f, height = 120f)

        val resized = GeometryService.resizeElement(
            originalBounds = original,
            handle = ResizeHandle.Left,
            dragStartPage = PagePoint(100f, 140f),
            currentPage = PagePoint(130f, 140f),
        )

        assertEquals(130f, resized.x)
        assertEquals(170f, resized.width)
        assertEquals(80f, resized.y)
        assertEquals(120f, resized.height)
    }

    @Test
    fun resizeFromTopLeftRespectsMinimumSize() {
        val original = PageRect(x = 20f, y = 20f, width = 30f, height = 30f)

        val resized = GeometryService.resizeElement(
            originalBounds = original,
            handle = ResizeHandle.TopLeft,
            dragStartPage = PagePoint(20f, 20f),
            currentPage = PagePoint(200f, 200f),
            minSize = 12f,
        )

        assertEquals(38f, resized.x)
        assertEquals(38f, resized.y)
        assertEquals(12f, resized.width)
        assertEquals(12f, resized.height)
    }

    @Test
    fun resizeFromCenterKeepsCenterFixed() {
        val original = PageRect(x = 100f, y = 80f, width = 200f, height = 120f)

        val resized = GeometryService.resizeElement(
            originalBounds = original,
            handle = ResizeHandle.Right,
            dragStartPage = PagePoint(300f, 140f),
            currentPage = PagePoint(330f, 140f),
            resizeFromCenter = true,
        )

        assertEquals(70f, resized.x)
        assertEquals(260f, resized.width)
        assertEquals(original.centerX, resized.centerX)
    }

    @Test
    fun constrainedCornerResizePreservesAspectRatio() {
        val original = PageRect(x = 20f, y = 20f, width = 100f, height = 50f)

        val resized = GeometryService.resizeElement(
            originalBounds = original,
            handle = ResizeHandle.BottomRight,
            dragStartPage = PagePoint(120f, 70f),
            currentPage = PagePoint(170f, 80f),
            constrainProportions = true,
        )

        assertEquals(150f, resized.width)
        assertEquals(75f, resized.height)
    }

    @Test
    fun snapBoundsUsesPageEdgesAndObjectSizes() {
        val other = TemplateElement.Rectangle(
            id = "other",
            x = 200f,
            y = 120f,
            width = 80f,
            height = 44f,
        )
        val guideSet = SnapGuideSet(
            pageSize = PageSize(width = 300f, height = 400f),
            margin = 20f,
            printableInset = 24f,
            bleedInset = 5f,
            trimInset = 0f,
            safeAreaInset = 10f,
            headerGuide = 54f,
            footerGuide = 346f,
            gridSize = 10f,
            columns = 2,
            rows = 2,
            baselineGrid = 12f,
        )

        val moved = GeometryService.snapBounds(
            bounds = PageRect(x = 2f, y = 118f, width = 80f, height = 40f),
            guideSet = guideSet,
            otherElements = listOf(other),
            threshold = 5f,
        )

        assertEquals(0f, moved.bounds.x)
        assertEquals(120f, moved.bounds.y)

        val resized = GeometryService.snapBounds(
            bounds = PageRect(x = 30f, y = 30f, width = 78f, height = 42f),
            guideSet = guideSet,
            otherElements = listOf(other),
            threshold = 5f,
            mode = ResizeHandle.Right,
        )

        assertEquals(80f, resized.bounds.width)
    }

    @Test
    fun snapRotationUsesStandardAnglesWithinThreshold() {
        assertEquals(45f, GeometryService.snapRotation(43.5f))
        assertEquals(42.4f, GeometryService.snapRotation(42.4f))
    }

    @Test
    fun resizeHandleHitTestIncludesEdges() {
        val bounds = PageRect(x = 100f, y = 80f, width = 200f, height = 120f)

        assertEquals(
            ResizeHandle.Top,
            GeometryService.hitTestResizeHandle(bounds, PagePoint(175f, 86f), handleSize = 20f),
        )
        assertEquals(
            ResizeHandle.Right,
            GeometryService.hitTestResizeHandle(bounds, PagePoint(294f, 130f), handleSize = 20f),
        )
        assertEquals(
            ResizeHandle.Bottom,
            GeometryService.hitTestResizeHandle(bounds, PagePoint(175f, 194f), handleSize = 20f),
        )
        assertEquals(
            ResizeHandle.Left,
            GeometryService.hitTestResizeHandle(bounds, PagePoint(106f, 130f), handleSize = 20f),
        )
    }

    @Test
    fun resizeHandleHitTestKeepsCornerPriority() {
        val bounds = PageRect(x = 100f, y = 80f, width = 200f, height = 120f)

        val hit = GeometryService.hitTestResizeHandle(bounds, PagePoint(106f, 86f), handleSize = 20f)

        assertEquals(ResizeHandle.TopLeft, hit)
    }

    @Test
    fun resizeHandleHitTestLeavesMoveAreaInsideSmallElements() {
        val bounds = PageRect(x = 100f, y = 80f, width = 20f, height = 16f)

        assertNull(
            GeometryService.hitTestResizeHandle(bounds, PagePoint(110f, 88f), handleSize = 36f),
        )
        assertEquals(
            ResizeHandle.Top,
            GeometryService.hitTestResizeHandle(bounds, PagePoint(110f, 82f), handleSize = 36f),
        )
        assertEquals(
            ResizeHandle.Top,
            GeometryService.hitTestResizeHandle(bounds, PagePoint(110f, 68f), handleSize = 36f),
        )
    }

    @Test
    fun hitTestReturnsTopMostVisibleElement() {
        val lower = TemplateElement.Rectangle(
            id = "lower",
            x = 0f,
            y = 0f,
            width = 100f,
            height = 100f,
            zIndex = 1,
        )
        val upper = TemplateElement.Text(
            id = "upper",
            x = 10f,
            y = 10f,
            width = 100f,
            height = 100f,
            zIndex = 2,
        )

        val hit = GeometryService.hitTestElement(listOf(lower, upper), PagePoint(20f, 20f))

        assertEquals("upper", hit?.id)
    }
}
