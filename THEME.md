# Service Console UI/UX Design Guidelines

This document outlines the design principles, visual language, and implementation standards for the **Service Console**, focusing on its professional industrial aesthetic and integrated glassmorphism.

## 1. Design Philosophy
The Service Console is designed for **industrial operations**. The goal is to provide a UI that feels authoritative, precise, and engineered. It balances modern "glassmorphic" depth with the clarity and structure required for high-stakes professional environments.

### Core Principles
- **Precision over Playfulness:** Use sharper corners and explicit borders to convey structure.
- **Clarity & Hierarchy:** Information density should be high but organized through clear typographic hierarchy.
- **Depth through Glassmorphism:** Use translucency to create a layered workspace, but ensure it never compromises legibility.

---

## 2. Visual Language

### Color Palette: "Slate & Steel"
The theme uses a professional palette based on deep navies and cool slates.

| Role | Color (Hex) | Usage |
| :--- | :--- | :--- |
| **Primary** | `#003366` | Branding, primary actions, and headers. |
| **Secondary** | `#5D6B7A` | Neutral components, secondary actions. |
| **Background** | `#F8FAFC` | Main application backdrop. |
| **Surface** | `#FFFFFF` | Main workspace and content cards. |
| **Outline** | `#94A3B8` | Borders and dividers. |

### Geometry & Shape
- **Major Panes (Workspace, Sidebar):** `12dp` rounded corners.
- **Components (Cards, List Items):** `8dp` rounded corners.
- **Small Controls (Buttons, Chips, Tags):** `4dp` rounded corners.
- **Borders:** `1dp` solid border using `outlineVariant` for cards and interactive surfaces.

---

## 3. Glassmorphism Implementation

The glassmorphic effect is controlled via `GLASSMORPHISM_INTENSITY` in `App.kt` (range `0.0f` to `1.0f`).

### Sidebar Glass
- **Surface:** `MaterialTheme.colorScheme.surface` with a dynamic alpha (base `0.28f` at max intensity).
- **Border:** Vertical gradient stroke (White `35%` to `10%` alpha) to simulate a "glass edge."
- **Effect:** At intensity `0.0f`, the sidebar reverts to a solid surface with standard Material 3 elevation.

### Background Blobs
- Background "vibrancy" is achieved through large, blurred radial gradients behind the UI.
- Use **Primary** and **Tertiary** theme colors with low alpha (`0.3f - 0.35f`).
- Apply a heavy blur (**100dp**) to maintain a professional, soft glow rather than a distracting pattern.

---

## 4. Component Guidelines

### Metric Cards
- Should feature a bold, uppercase label in `labelSmall`.
- Large, high-contrast headline for the value.
- Muted `bodySmall` text for context or detail.

### Title Bar (Undecorated Window)
- The window is **undecorated** to support transparency.
- **Window Controls:** Follow the standard macOS layout (Red/Yellow/Green circles) positioned in the top-right of the title bar area.
- **Draggable Area:** The top `44dp` of the application acts as the window handle.

---

## 5. Implementation Standards

- **Units:** Always use `dp` for dimensions and `sp` for typography.
- **Composition:** Prefer `Surface` for containers to ensure they automatically inherit theme colors and elevation logic.
- **Glass Scaling:** When adding new glassmorphic elements, always scale their `alpha` and `border` opacity based on the global `glassmorphismIntensity`.
