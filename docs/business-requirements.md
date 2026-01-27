# YugaStore — Business Requirements Documentation

## Document Information
- **Document Version:** 1.0
- **Status:** Current State Documentation
- **Owner:** Product Owner
- **Date Created:** 2026-01-27
- **Last Updated:** 2026-01-27
- **Purpose:** Document business rules and logic as currently implemented

---

## Overview

This document catalogs the business rules currently implemented in YugaStore. It describes what the system does, how it behaves, and the business logic enforced by the application.

**Scope:** This documentation focuses exclusively on existing, implemented capabilities. It does not address gaps, future requirements, or recommendations.

**Organization:** Rules are organized by business domain: Shopping Cart, Product Catalog, Checkout & Orders, Inventory Management, Pricing, User Management, Transaction Integrity, and Not Implemented capabilities.

---

## 1. Shopping Cart Rules

### 1.1 Cart Persistence and Identification
**The system maintains persistent shopping cart data** for each customer session using a data store.

**The system uses a fixed user identifier** for cart operations. Currently, the user ID "1" is used for all shopping sessions.

**The system maintains a separate cart for each user** with cart items identified by product identifier (ASIN) and user ID.

### 1.2 Adding Items to Cart
**The system increments quantity when a product already exists in the cart.** When adding a product that is already in the cart, the quantity increases by 1 rather than creating a duplicate entry.

**The system creates a new cart entry with quantity 1 for new products.** When adding a product not currently in the cart, a new cart item is created with an initial quantity of 1.

**The system stores minimal information in the cart.** Cart entries contain only the product identifier (ASIN), user ID, and quantity. Product details like name, price, and image are retrieved when displaying the cart.

### 1.3 Removing Items from Cart
**The system decrements quantity when removing one unit of a product.** Removing a product decreases its quantity by 1 when the current quantity is greater than 1.

**The system deletes the cart entry when quantity reaches 1 and item is removed.** When removing a product with quantity of 1, the entire cart entry is deleted.

**The system does not prevent removal of items that don't exist in the cart.** No validation occurs before attempting to remove a product.

### 1.4 Cart Retrieval and Display
**The system retrieves all cart items for a user with complete product information.** Cart display shows enriched product data including name, price, image, and quantity.

**The system calculates cart totals by multiplying price × quantity** for each item and summing the results.

**The system provides services for adding, removing, and viewing cart contents.**

### 1.5 Cart Clearing
**The system clears all cart items for a user after successful checkout.** Once an order is placed, all items are removed from the customer's cart.

---

## 2. Product Catalog Rules

### 2.1 Product Information
**The system maintains a product catalog** with comprehensive product information stored in a data store.

**The system identifies products by ASIN** (Amazon Standard Identification Number). ASINs are unique identifiers like "B00004ZCJI" or "B000067NXH".

**The system stores product attributes including** category, sales rank, title, description, image URL, and price. Each product has a single base price.

**The system maintains product review data** including number of reviews, number of stars, and average star rating.

### 2.2 Product Recommendations
**The system maintains product relationship data** showing which products are related through customer behavior. Four types of relationships are tracked:
- Products also bought together
- Products also viewed by customers
- Products bought together in past orders
- Products customers buy after viewing this item

**The system uses pre-loaded recommendation data.** Recommendation relationships are not computed dynamically from current customer behavior.

**The system provides recommendation data** but display of recommendations in the customer interface is limited.

### 2.3 Product Ranking and Sorting
**The system maintains product rankings** organized by category and sales rank in a separate data structure.

**The system retrieves products sorted by sales rank within a category.** Products are displayed in ascending sales rank order (lower rank numbers appear first).

**The system supports pagination of product lists.** Customers can browse through products using page-by-page navigation with configurable page sizes using limit and offset parameters.

**The system creates an index for efficient retrieval** of top products in a given category by sales rank.

### 2.4 Product Browsing
**The system provides detailed product information** when customers view individual product pages through a service that retrieves product metadata by ASIN.

**The system provides product catalog services** allowing customers to:
- View product details by product identifier
- Browse all products with pagination
- Browse products by category with pagination

**The system does not implement keyword search, filtering, or faceted navigation.** Product discovery is limited to browsing by category with sales rank sorting.

---

## 3. Checkout and Order Rules

### 3.1 Checkout Process
**The system validates inventory availability before completing checkout.** Current inventory levels are checked against cart quantities to ensure products can be fulfilled.

**The system rejects checkout when inventory is insufficient.** If any cart item quantity exceeds available inventory, the entire checkout transaction fails with a "Not Enough Products In Stock" error message identifying which product and how many are available.

**The system processes checkout as an atomic transaction.** Inventory decrement, order creation, and cart clearing all succeed together or fail together—partial completion is not possible. The system uses transaction processing with BEGIN TRANSACTION and END TRANSACTION boundaries.

**The system decrements inventory for all cart items upon successful checkout.** For each product in the cart, the ordered quantity is subtracted from available inventory.

**The system creates an order record after inventory is decremented.** The order is assigned a unique identifier and records the user ID, order details, timestamp, and total amount.

**The system clears the shopping cart after order creation.** The final step of checkout removes all items from the customer's cart.

### 3.2 Order Information
**The system generates unique order identifiers** for each completed purchase using a universally unique identifier format.

**The system stores order details as descriptive text.** Order information is captured as a formatted description containing "Customer bought these Items:" followed by product titles and quantities, rather than structured line items.

**The system captures the order timestamp** for chronological tracking using the current date and time.

**The system stores order total as a single amount.** No breakdown of subtotal, tax, shipping, or discounts is maintained in order records.

**The system associates all orders with a fixed user identifier.** Currently, the user ID "1" is used for all orders regardless of which customer placed the order.

### 3.3 Order Retrieval and Confirmation
**The system returns order confirmation information after successful checkout.** Customers receive an order number (with prefix "kmp-"), order status (SUCCESS or FAILURE), and order details text.

**The system does not provide customer-facing order history.** Customers cannot view their past orders through the application beyond the immediate checkout confirmation.

**The system does not provide order lookup capability.** There is no way to retrieve a specific order by its order number after the initial checkout.

**The system does not track order status or lifecycle.** Orders have no status field indicating whether they are pending, shipped, delivered, or cancelled.

---

## 4. Inventory Management Rules

### 4.1 Inventory Tracking
**The system maintains inventory levels** for each product in the catalog in a dedicated data store.

**The system tracks inventory quantity per product (ASIN).** Each product has a single inventory count stored as an integer.

**The system does not track inventory by warehouse or location.** Inventory is a single global quantity per product.

### 4.2 Inventory Validation
**The system only validates inventory at checkout time, not when adding to cart.** Customers can add items to their cart without immediate stock verification.

**The system does not display stock levels on product pages.** No "In Stock" or "Out of Stock" indicators are shown to customers during browsing or when adding items to cart.

**The system does not prevent adding out-of-stock items to cart.** Customers discover insufficient inventory only after proceeding to checkout.

**The system refreshes inventory data for each product during checkout validation.** The system retrieves the current quantity before comparing against requested amounts to ensure accuracy.

### 4.3 Inventory Updates
**The system decrements inventory atomically during checkout.** Inventory updates are part of the checkout transaction with transactional processing enabled on the inventory data store, ensuring data consistency.

**The system updates inventory using quantity arithmetic.** The system subtracts the requested quantity directly from the current inventory level (e.g., "quantity = quantity - X").

**The system does not reserve inventory between cart and checkout.** Products can be added to multiple customer carts simultaneously and may sell out before any particular customer completes checkout.

**The system does not provide inventory replenishment capabilities.** No functionality exists for restocking products through the application.

---

## 5. Pricing Rules

### 5.1 Product Pricing
**The system maintains a single static price per product.** Each product has one base price that applies to all purchases.

**The system stores prices as decimal numbers** allowing for cent-level precision.

**The system does not support promotional pricing, discounts, or coupons.** No discount mechanisms are available.

**The system does not support tiered or volume-based pricing.** Price per unit is constant regardless of quantity purchased.

### 5.2 Tax Calculation
**The system does not calculate sales tax.** Tax is displayed as $0.00 at checkout.

**The system does not collect customer location for tax purposes.** No address or jurisdiction data is captured during the shopping or checkout process.

### 5.3 Shipping Costs
**The system does not calculate shipping costs.** Shipping is not displayed in the checkout interface.

**The system does not collect shipping address information.** No address capture exists in the checkout flow.

### 5.4 Order Total Calculation
**The system calculates order total as sum of (price × quantity) for all cart items.** The total is the sum of extended prices for each cart item.

**The system rounds calculated prices to 2 decimal places** for currency accuracy using standard rounding operations.

**The system recalculates totals during checkout.** The checkout process retrieves current product prices and multiplies by quantities to ensure accurate totals.

---

## 6. User Management Rules

### 6.1 Authentication
**The system has login functionality that is not currently integrated** with the shopping experience.

**The system uses fixed user identifiers.** User IDs "1" and "u1001" are used for cart and checkout operations without actual user authentication.

**The system does not require login to browse products or add items to cart.** All shopping operations are accessible without authentication.

### 6.2 User Accounts
**The system does not maintain user profile information.** No customer account data is stored beyond the fixed user identifiers.

**The system does not track user purchase history by customer.** Orders cannot be retrieved for a specific customer through the application interface.

**The system does not support user registration or account creation.** No capability exists for customers to create accounts.

---

## 7. Transaction Integrity

**The system ensures data consistency for critical operations.** Checkout transactions are atomic—inventory updates and order creation either both succeed or both fail, preventing data inconsistencies.

**The system prevents partial order completion.** If any step of the checkout process fails (inventory validation, order creation, cart clearing), no changes are committed to persistent storage.

**The system uses transaction boundaries for checkout operations.** The checkout process wraps multiple data modifications within explicit transaction markers to ensure atomicity.

**The system enables transaction support on critical data stores.** Both orders and product inventory have transactional processing enabled to support atomic operations.

---

## 8. Not Implemented

The following capabilities mentioned in project documentation or expected in typical e-commerce systems are **not currently implemented:**

### Customer-Facing Capabilities
- Customer authentication and login integration
- User account registration and profile management
- Order history viewing for customers
- Order tracking and status updates
- Returns and cancellations processing
- Saved payment methods or addresses
- Wishlist or saved-for-later functionality
- Product reviews and ratings (data exists but customer submission not enabled)
- Keyword search and filtering
- Email notifications (order confirmation, shipping updates)
- Real-time stock availability display on product pages

### Pricing and Promotions
- Promotional discount codes or coupons
- Time-based sales or promotional pricing
- Volume discounts or tiered pricing
- Tax calculation based on location
- Shipping cost calculation
- Multiple pricing tiers or customer-specific pricing

### Inventory Features
- Stock level display on product pages ("In Stock", "Low Stock", "Out of Stock")
- Out-of-stock indicators or "notify me" options
- Inventory reservation during checkout
- Low stock warnings for customers or administrators
- Multi-location inventory tracking
- Back-order support

### Support and Operations
- Customer support tools for order management
- Order search and customer lookup for support teams
- Refund processing workflows
- Returns management system
- Inventory replenishment interfaces
- Bulk product catalog updates
- Order modification or cancellation after placement

### Analytics and Reporting
- Business metrics dashboards
- Customer behavior tracking
- Conversion funnel analytics
- Financial reporting and reconciliation
- Promotional campaign performance measurement
- Inventory turnover reporting
- Customer lifetime value tracking

### Payment Processing
- Payment method capture and processing
- Payment gateway integration
- Multiple payment method support
- Secure payment information handling
- Payment confirmation and receipts

---

## Document Control

### Change Log
| Date | Version | Summary of Change | Owner |
|------|---------|-------------------|-------|
| 2026-01-27 | 1.0 | Initial business requirements document - current state only | Product Owner |

### Related Documents
- [Business Purpose](businesspurpose.md) - Strategic context and business outcomes
- [Project Charter](projectcharter.md) - Governance and stakeholders
- [Product Owner Persona](personas/product-owner.md) - Role responsibilities

---

*This document reflects the current business requirements of YugaStore as of 2026-01-27. It will be updated as business rules evolve.*
