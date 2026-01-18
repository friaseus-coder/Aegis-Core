# Aegis Core - Visual Identity & Design Guidelines

> [!IMPORTANT]
> All future UI implementations MUST adhere to these guidelines to maintain the "Premium Security" aesthetic.

## 1. Core Philosophy: "Premium Security"
The application must convey **Solidity**, **Value**, and **Privacy**.
- **Solidity**: Robust layouts, strong typography.
- **Value**: Gold accents, elegant spacing.
- **Privacy**: Strict Dark Mode (no light themes).

## 2. Color Palette
We use a custom palette. Do NOT use default Material colors.

### Primary Colors
- **Navy Blue (`#0A192F`)**: Used for Backgrounds and Surfaces.
  - *Usage*: `MaterialTheme.colorScheme.background`, `surface`.
- **Gold (`#D4AF37`)**: Used for Accents, Primary Actions, and Icons.
  - *Usage*: `MaterialTheme.colorScheme.primary`, `secondary`.

### Secondary Colors
- **Navy Blue Dark (`#050C18`)**: Used for contrast or specific container backgrounds.
- **Gold Dim (`#C5A028`)**: Used for containers or disabled states.
- **Light Gray (`#E0E0E0`)**: Used for Text/Content (`onBackground`, `onSurface`).

## 3. Theme Rules
- **Strict Dark Mode**: The app DOES NOT support a Light Theme.
  - `Theme.kt` forces specific dark colors regardless of system settings.
- **Material 3**: Use Material 3 components (`androidx.compose.material3`).
- **StatusBar**: Must match the background color (`#0A192F`).

## 4. Component Guidelines

### Icons & Imagery
- **Vector Icons**: Use `Icons.Filled` or `Icons.Outlined` from Material Design.
- **Programmatic Assets**: Prefer `Canvas` drawing for custom branding (like the Logo) over raster images to ensure scalability and "code-first" philosophy.
- **Tint**: Icons should generally be tinted **Gold** (`Color(0xFFD4AF37)`).

### Cards & Surfaces
- **Shape**: Rounded corners (`RoundedCornerShape(16.dp)`).
- **Elevation**: Moderate elevation (`4.dp`) for cards to lift them from the dark background.
- **Surface Color**: Use `SurfaceVariant` (slightly lighter than background) for cards to create depth.

### Typography
- Use standard Material 3 Typography but ensure high contrast (Light Gray on Navy Blue).
- Headings should be bold and clear.

## 5. Layout & Navigation
- **Dashboard First**: The entry point is a Dashboard Grid.
- **Navigation**: Use Jetpack Compose Navigation (`NavHost`).
  - Define routes in `Screen.kt` (Sealed Class).
  - Use `Scaffold` for top-level screen structure.

## 6. Implementation Checklist
When adding a new screen:
1. [ ] Add route to `Screen.kt`.
2. [ ] Add Composable destination to `NavigationGraph.kt`.
3. [ ] Use `Scaffold` with `containerColor = MaterialTheme.colorScheme.background`.
4. [ ] Ensure text is readable (Light Gray) and accents are Gold.

## 7. App Icon (Launcher)
- **Format**: Adaptive Icon (XML Vector).
- **Foreground**: Gold Shield Logo (`ic_launcher_foreground.xml`).
- **Background**: Navy Blue Color (`ic_launcher_background.xml`).
- **Location**: `res/mipmap-anydpi-v26/`.
