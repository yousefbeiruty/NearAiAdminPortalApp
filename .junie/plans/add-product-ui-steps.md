---
sessionId: session-261104-104233-h7tl
---

# UI Steps - How to Add a New Product

### Step-by-Step Guide

Follow these steps in the NearAi Admin Portal app to add a new product:

---

**Step 1: Open the app and navigate to Products**
1. Launch the app and sign in (if not already authenticated)
2. Tap the **More** tab in the bottom navigation bar
3. In the More menu, tap **Products**

**Step 2: Start creating a new product**
- On the Products list screen, tap the **floating action button** (bottom-right corner) labeled **"New Product"**
- This opens the Product Create screen with a form

**Step 3: Fill in the product details**
The form contains the following fields:

| Field | Required | Description | Example |
|-------|----------|-------------|---------|
| **Name** | ✓ | Product name (1–255 characters) | "Wireless Headphones" |
| **Description** | ✓ | Multi-line text description | "Bluetooth 5.0 headphones with noise cancellation" |
| **Price** | ✓ | Numeric price amount | `99.99` |
| **Currency** | — | Currency code (3 letters) | `USD` (default) |
| **Status** | — | Dropdown: Active / Inactive / Draft | Select from dropdown |
| **Category** | — | Optional category assignment | Select from dropdown |

**Step 4: Submit the form**
- Tap the **"Create Product"** button at the bottom of the form

**Step 5: Validation**
If any field is invalid, a red error banner will appear at the top with the error message:
- "Product name is required" — if Name is blank
- "Description is required" — if Description is blank
- "Valid price is required" — if Price is not a valid positive number

**Step 6: After successful creation**
- The app automatically navigates you to the **Product Details** screen for the newly created product
- You can view all product information, manage variants, images, and stock from there

---

### Navigation Flow
```
More Tab → Products → [New Product FAB] → Create Form → [Create Product] → Product Details
```

### Key Files
- `ProductsListScreen.kt` — Products list with the "New Product" button
- `ProductCreateScreen.kt` — Form UI with all input fields
- `ProductCreateViewModel.kt` — Validation and repository call
- `AppNavHost.kt` — Navigation routing between screens

# Delivery Steps

### ✓ Step 1: Provide UI steps for adding a new product
Deliver a step-by-step guide covering navigation, form filling, validation, and post-creation flow based on the existing codebase.