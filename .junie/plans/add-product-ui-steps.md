---
sessionId: session-261104-104233-h7tl
---

# Product Image Delivery Integration Plan

## Requirements

### Overview & Goals
Integrate backend product image delivery into **both** the Admin and Customer Android apps so that uploaded images are displayed as actual rendered images. The Admin app needs image rendering in the management carousel; the Customer app already renders images but needs to ensure the backend's new image delivery contract is fully supported.

### Current State — Admin App (`NearAiAdminPortal`)
- **Image upload**: Fully implemented with correct backend contract (multipart field `file`, MIME type detection, file size validation, duplicate prevention)
- **Image DTOs**: `ProductImageResponseDto` matches backend contract with fields: `id`, `product_id`, `url`, `file_name`, `mime_type`, `file_size_bytes`, `display_order`, `is_primary`, `uploaded_at`
- **Image display**: `ImageCarousel` in `ProductDetailsScreen.kt` shows icon placeholders with file names only — no actual image rendering
- **Coil**: Already a project dependency and used in `DiscoveryScreen.kt` via `coil.compose.AsyncImage`
- **Base URL**: Debug `http://10.0.2.2:8081/`, Release `https://api.nearai.dev/`. Backend serves images at `/uploads`
- **Product details response**: `ProductResponseDto.images` is `List<String>` (URLs only), not full image objects
- **Dedicated image endpoints**: Return full `ProductImageResponseDto` objects via `listProductImages(productId)`

### Current State — Customer App (`NearAiDescovery`)
- **Product DTO**: `ProductDto.images` is `List<String>` (URLs). Same as Admin — backend returns image URLs as strings in product responses
- **Domain model**: `Product.images` is `List<String>`, with `primaryImage` computed property that returns the first URL
- **Product Detail Screen**: Already uses `coil3.compose.AsyncImage` to render images in a swipeable carousel (lines 346-356). Images render from `product.images` URLs
- **Image rendering**: Already functional — uses `AsyncImage` with `ImageRequest.Builder` and `ContentScale.Crop`
- **Empty state**: Shows icon placeholder when `product.images` is empty
- **API service**: `FastApiService.getProductById()` returns `ProductDto` with `images: List<String>`
- **No dedicated image endpoints**: Customer app does not call `/products/{id}/images` — it relies on `ProductDto.images` from product details

### Scope

**In Scope — Admin App:**
- Replace icon placeholders in `ImageCarousel` with actual image rendering using Coil `AsyncImage`
- Handle image loading states (loading placeholder, error fallback)
- Ensure uploaded images appear immediately after successful upload
- Add mapper tests for image DTO serialization

**In Scope — Customer App:**
- Verify `ProductDto.images` correctly receives uploaded image URLs from backend
- Verify `ProductDetailScreen` renders uploaded images (already implemented)
- Add tests for image URL handling in DTO serialization
- Report impact of product listing not returning uploaded image metadata

**Out of Scope:**
- Image reordering UI (drag-and-drop) — backend API exists but UI not requested
- Product listing images — backend does not return image metadata in listing to avoid N+1 queries
- Image upload in customer app — customers don't upload images

### Functional Requirements
1. **Admin**: Uploaded images must render as actual images in the `ImageCarousel`, not icon placeholders
2. **Admin**: Image loading must show a placeholder while loading
3. **Admin**: Failed image loads must show a fallback icon with file name
4. **Admin**: The primary image must be visually distinguished
5. **Customer**: Product detail screen must display uploaded images from backend-provided URLs
6. **Customer**: Multiple images must support gallery navigation (already implemented)
7. **Both**: Images must load from backend-provided URLs without hardcoded localhost references

## Technical Design

### Admin App — Current Implementation
- **`ProductDetailsScreen.kt`**: `ImageCarousel` renders each image as a `Card` with an `Icon` placeholder and file name text. No image loading library is used.
- **`DiscoveryScreen.kt`**: Reference implementation of `AsyncImage` from Coil — loads images from URLs with `contentScale = ContentScale.Crop`
- **`CatalogApiService.kt`**: `uploadProductImage` returns `ProductImageResponseDto` with `url` field
- **`CatalogRepositoryImpl.kt`**: Maps `ProductImageResponseDto` to domain `ProductImage` via `CatalogMapper`
- **`ProductDetailsViewModel`**: Calls `listProductImages(productId)` after upload to refresh image list

### Customer App — Current Implementation
- **`ProductDetailScreen.kt`** (lines 339-422): Already renders images via `AsyncImage` in a `LazyRow` carousel. Falls back to icon placeholder when no images exist.
- **`ProductDto`**: `images: List<String>` — matches backend `ProductResponseDto.images`
- **`Product` domain model**: `images: List<String>`, `primaryImage` computed property
- **`FastApiService`**: `getProductById()` returns `ProductDto` with images from backend
- **No image upload**: Customer app is read-only for images

### Key Decisions
1. **Admin — Use Coil `AsyncImage`**: Already in dependencies, already used in `DiscoveryScreen.kt`. No new dependency needed.
2. **Admin — Use backend-provided `url` field directly**: The `ProductImage.url` field contains the full URL from the backend. No URL construction needed.
3. **Admin — Keep carousel layout unchanged**: Same 140dp width cards, horizontal scroll. Only replace the icon placeholder with `AsyncImage`.
4. **Admin — Error fallback**: If image fails to load, show the icon placeholder with file name (current behavior) as graceful degradation.
5. **Customer — No changes needed for image rendering**: The customer app already renders images correctly. The backend's `ProductResponseDto.images` field (which maps to `ProductDto.images`) contains image URLs that are already displayed.
6. **Customer — Add image DTO tests**: Verify that `ProductDto` correctly serializes image URLs from backend responses.

### Proposed Changes

#### Admin: `ProductDetailsScreen.kt` — ImageCarousel
- Add `import coil.compose.AsyncImage`
- Replace the `Icon` placeholder in each carousel card with `AsyncImage(model = image.url, ...)`
- Add `contentScale = ContentScale.Crop` to fill the card area
- Keep the file name and "Primary" badge below the image
- Add error handling: if URL is null or empty, show icon placeholder

#### Customer: `ProductDtos.kt` — No changes needed
- `ProductDto.images` is already `List<String>` matching backend contract
- `ProductDetailScreen` already renders these URLs correctly

#### Customer: Tests
- Add serialization test: `ProductDto` with image URLs deserializes correctly
- Add test: Empty images list handles gracefully
- Add test: Missing image URLs are filtered out

### File Structure

**Admin App — Modified files:**
- `app/src/main/java/com/nearai/admin/feature/catalog/presentation/ProductDetailsScreen.kt` — Replace icon placeholders with `AsyncImage`

**Admin App — New files:**
- `app/src/test/java/com/nearai/admin/feature/catalog/data/CatalogMapperTest.kt` — Image DTO mapping tests

**Customer App — New files:**
- `feature-home/src/test/java/com/yousef/nearaidescovery/feature/home/ImageDtoSerializationTest.kt` — Image URL serialization tests

### Architecture Diagram

```mermaid
graph LR
  A[Backend /uploads] -->|HTTP GET image| B[Coil AsyncImage Admin]
  A -->|HTTP GET image| C[Coil AsyncImage Customer]
  D[Admin ViewModel] -->|listProductImages| E[CatalogRepository]
  E -->|GET /products/{id}/images| F[Backend API]
  F -->|ProductImageResponseDto| E
  E -->|List<ProductImage>| D
  D -->|uiState.images| G[Admin ProductDetailsScreen]
  G -->|image.url| B
  H[Customer ViewModel] -->|getProductById| I[ProductRepository]
  I -->|GET /products/{id}| F
  F -->|ProductDto.images List<String>| I
  I -->|Product.images| H
  H -->|product.images| C
  C -->|rendered image| J[Customer Carousel]
  B -->|rendered image| K[Admin Carousel]
```

### Risks
1. **Image URL accessibility**: Backend returns URLs like `http://10.0.2.2:8081/uploads/...` in debug. These must be accessible from the Android emulator. If the backend returns relative paths, they need to be resolved against the base URL.
2. **CORS / network issues**: If images are served on a different origin, emulator may have network restrictions. Mitigation: test with actual backend.
3. **Large image payloads**: Backend should serve resized/thumbnail images for the carousel. If full-size images are served, loading may be slow. Mitigation: rely on Coil's built-in downsampling.
4. **Product listing limitation**: Backend does not return image metadata in product listing responses to avoid N+1 queries. Product cards in home, search, and category screens will not show uploaded images until the backend adds image support to listing.

### Product Listing Impact Report
The backend explicitly does not return uploaded image metadata in product listing responses (`GET /products/`) to avoid N+1 database queries. This means:
- **Home screen product cards**: Will not display uploaded images — only products with legacy image URLs (if any) will show images
- **Search results**: Same limitation — no uploaded images in cards
- **Category product cards**: Same limitation
- **Product detail screen**: **Works correctly** — both apps fetch product details individually, and the backend includes `images` in the single-product response
- **Recommendation**: Accept this limitation until the backend adds a batch image endpoint or includes a thumbnail URL in listing responses

## Testing

### Validation Approach
Run unit tests and debug builds for both apps to verify image rendering and error handling.

### Key Scenarios — Admin
1. **Valid image upload and display**: Upload an image, verify it renders in the carousel (not icon placeholder)
2. **Multiple images**: Upload multiple images, verify all render correctly
3. **Primary image distinction**: Set an image as primary, verify visual badge
4. **Missing image URL**: Image with null/empty URL shows icon placeholder
5. **Image load failure**: Network error shows fallback icon
6. **Empty image state**: No images shows `EmptyImagesState`

### Key Scenarios — Customer
1. **Product with images**: Product detail renders images from backend URLs
2. **Product without images**: Shows "No image available" placeholder
3. **Multiple images**: Carousel pagination works correctly
4. **Image URL error**: Failed image load shows placeholder

### Test Changes
- **Admin — New**: `CatalogMapperTest.kt` — Image DTO mapping, URL preservation, multiple images
- **Admin — Existing**: `ProductDetailsViewModelTest.kt` — 13 upload tests already pass
- **Customer — New**: `ImageDtoSerializationTest.kt` — ProductDto image URL serialization

# Delivery Steps

### ✓ Step 1: Provide UI steps for adding a new product
Deliver a step-by-step guide covering navigation, form filling, validation, and post-creation flow based on the existing codebase.

### ✓ Step 2: Replace Admin ImageCarousel icon placeholders with Coil AsyncImage
Replace `Icon` placeholders in `ProductDetailsScreen.kt` `ImageCarousel` with `coil.compose.AsyncImage` so uploaded images render as actual images with loading and error states.

### ✓ Step 3: Add Admin CatalogMapper image DTO mapping tests
Create `CatalogMapperTest.kt` verifying `ProductImageResponseDto` to domain `ProductImage` mapping preserves URL, file name, MIME type, and all fields.

### ✓ Step 4: Verify Customer app ProductDto image URL serialization
Inspect `ProductDto`, `ProductDetailScreen`, and related files in `NearAiDescovery` to confirm images render correctly from backend URLs.

### ✓ Step 5: Add Customer app image DTO serialization tests
Add image URL handling tests to `ProductDtoSerializationTest.kt` in the Customer app verifying `ProductDto` image URL deserialization, empty images, and uploaded image URLs.

### ✓ Step 6: Run tests and build verification
Run unit tests and debug builds for both apps to validate all changes compile and tests pass.

### ✓ Step 7: Finalize plan and submit summary
Update plan statuses, produce the final summary with exact backend response structures, files changed, screens displaying images, remaining limitations, and test results.