# The Comfy Co – Android App (PROG7314 POE)

**PROG7314 – Programming 3D – Final Portfolio of Evidence**

---

## Table of Contents
1. [Overview](#overview)
2. [Core Features](#core-features)
3. [User Defined Features](#user-defined-features)
4. [Features Not Included](#features-not-included)
5. [Innovative Features](#innovative-features)
6. [Technology Stack](#technology-stack)
7. [System Architecture](#system-architecture)
8. [Getting Started](#getting-started)
9. [REST API Design](#rest-api-design)
10. [Data Storage & Offline Sync](#data-storage--offline-sync)
11. [Authentication, Security & POPIA](#authentication-security--popia)
12. [Multi-Language Support](#multi-language-support)
13. [Notifications](#notifications)
14. [Testing & GitHub Actions](#testing--github-actions)
15. [Release Notes](#release-notes)
16. [Play Store Preparation](#play-store-preparation)
17. [Demo Video](#demo-video)
18. [AI Tool Usage](#ai-tool-usage)
19. [Credits](#credits)

---

## Overview
The Comfy Co Android App is a mobile e-commerce application built for the PROG7314 POE. It allows users to browse The Comfy Co’s luxury sleepwear, manage their profile, place orders securely, and receive real-time updates.

The project implements the full POE requirements:
- Custom REST API connected to a database
- Android client consuming that API
- Single Sign-On (SSO) and Biometric Authentication
- Settings and preferences
- Offline mode with sync (RoomDB)
- Real-time push notifications
- Multi-language support (2+ South African languages)
- GitHub for version control with GitHub Actions for automated builds and tests
- Preparation for Google Play Store publication

The design and features are based on Part 1 research, planning, and design documents, where three similar Android apps were analysed and the final feature set and UI/UX were defined.

---

## Core Features

### 1. User Authentication
- **Single Sign-On (SSO)**
  - Sign in with a federated provider (e.g., Google)
  - Secure token handling on device
- **Biometric Authentication (POE only)**
  - Fingerprint / face unlock for fast re-entry
  - Uses AndroidX Biometric APIs tied to the user’s SSO session

### 2. Profile & Settings
- Toggle dark mode / light mode
- Select preferred language

### 3. Product & Shopping Flow
- Browse products (e.g., luxury sleepwear and loungewear)
- Product detail view with images, description, price, and sizes
- Add to cart, update quantities, remove items
- Checkout flow integrated with the API (order creation)

### 4. REST API Integration
- Custom API providing:
  - Products
  - Authentication
  - Orders
- Android app communicates via Retrofit over HTTPS

### 5. Offline Mode with Sync (POE only)
- Selected features work fully offline
  - Admin can upload a product offline; it syncs when online again

### 6. Real-Time Notifications (POE only)
- Push notifications via Firebase Cloud Messaging (FCM) for:
  - Order status updates (confirmed, shipped, delivered)
  - Promotional campaigns or restocks

### 7. Multi-Language Support (POE only)
- Supports at least two South African languages in addition to English:
  - English
  - isiZulu

---

## User Defined Features
- Add to cart
- Checkout
- Wishlists
- Dark and light mode

---

## Features Not Included
- Account Management and Order Tracking: Users cannot manage multiple accounts or view order history
- Payment Integration via Stripe/PayPal: Orders cannot be paid through external gateways; order confirmation is simulated
- Track Order Status via Profile: Real-time order updates are not available
- Real-Time Inventory Updates: No inventory updates
- Filtering Options: Users cannot filter

> These features are planned for future development to provide a fully functional e-commerce experience.

---

## Innovative Features
1. Wishlist Integration – Users can save products for later  
2. Dark Mode – Reduced eye strain and user preference  
3. Language Toggle (English/isiZulu/Afrikaans) – Inclusivity and accessibility  
4. SSO Login (Google) – Simplifies onboarding  

---

## Technology Stack

**Android App**
- Language: Kotlin
- IDE: Android Studio
- Architecture: MVVM + Repository pattern
- UI: XML layouts / Material 3 components
- Networking: Retrofit
- Local Storage: RoomDB
- Authentication: Firebase Auth / custom SSO backend
- Biometrics: AndroidX Biometric
- Notifications: Firebase Cloud Messaging (FCM)

**Backend & Infrastructure**
- API: Custom REST API (ASP.NET Core / Node / other)
- Database: MongoDB
- Hosting: Cloud provider (e.g., Render)

**DevOps**
- Version Control: Git + GitHub
- Issue Tracking: GitHub Issues / Projects

---

## System Architecture
High-level flow:

```
Android App (UI + ViewModel)
        |
        | Retrofit (HTTPS)
        v
   REST API Layer
        |
        v
     Database
```

1. User opens the app  
2. SSO handles login; tokens stored securely  
3. App calls the REST API (products, orders, profile)  
4. Data cached locally via Room for offline use  
5. Firebase delivers push notifications  
6. GitHub Actions runs tests and builds on push/pull requests  

---

## Getting Started

### Prerequisites
- Android Studio  
- JDK 17  
- Git  
- Access to the hosted REST API  
- Firebase project (for Auth/FCM)  

### Clone the Repository
```bash
git clone https://github.com/ST10166813/TheComfyCo.git
cd TheComfyCo
```

### Configure API & Firebase
Update local.properties, `.env`, or config files:

```kotlin
const val BASE_URL = "https://thecomfycoapi-1.onrender.com/"
```

API Repo: https://github.com/ST10166813/thecomfycoAPI.git

### Open in Android Studio
- File > Open… and select the project folder  
- Let Gradle sync  

### Run the App
- Connect a physical Android device  
- Enable USB debugging  
- Click Run and select your device  

---

## REST API Design
**Base URL**: `https://thecomfycoapi-1.onrender.com/`

**Endpoints**
- `POST /auth/sso-login` – Accepts SSO tokens / credentials and returns app token  
- `GET /products` – Returns product list  
- `GET /products/{id}` – Product detail  
- `POST /orders` – Creates order from cart  
- `GET /orders/user` – User orders  
- `GET /profile` – Fetch profile  
- `PUT /profile` – Update profile  

Android app uses Retrofit interfaces to map these endpoints to Kotlin suspend functions.

---

## Data Storage & Offline Sync
**Local Data (RoomDB)**
- Products (for browsing offline)

**Offline Behaviour**
- Network unavailable: read from Room, store pending operations  
- Network returns: pending operations synced; conflicts resolved via last-write wins  

---

## Authentication, Security & POPIA
- SSO via trusted provider  
- Biometrics for device-level security  
- Tokens stored securely  
- Minimal PII collected (compliant with POPIA)  
- HTTPS communication enforced  

---

## Multi-Language Support
Managed via Android `strings.xml` resource system.

Example:
- `values/strings.xml` – English  
- `values-zu/strings.xml` – isiZulu
- `values-af/strings.xml` – Afrikaans

Language selector in Settings persists selection across app.

---

## Notifications
Implemented via Firebase Cloud Messaging (FCM).

Use cases:
- New product added  
- Order updates  

Permission requested on Android 13+.

---

## Testing & GitHub Actions
**Local Testing**
- Unit tests for ViewModels, Repositories, utilities  
- Instrumentation tests for login, cart, checkout  

```bash
./gradlew test
./gradlew connectedAndroidTest
```

---

## Release Notes

### Prototype (Part 2)
- Initial SSO login and registration  
- Product listing and detail screens  
- Cart and checkout flow  
- REST API integration  
- Settings screen  
- Admin dashboard  

### Final POE (Submission)
- Biometric Authentication added  
- Offline mode with Room sync  
- Real-time push notifications  
- Multi-language support (English + at least two SA languages)  
- Improved UI/UX  
- Extended REST API  
- GitHub Actions workflow strengthened  
- Signed APK / App Bundle prepared  
- Bugs fixed from prototype feedback  

---

## Play Store Preparation
- Signed APK / App Bundle generated  
- App icon and assets included  

---

## Demo Video
Shows:
- Login and SSO flow  
- Biometric Authentication  
- Settings (language, theme)  
- REST API integration  
- Offline behaviour and sync  
- Real-time notifications  
- General navigation  

YouTube (Unlisted): [https://youtu.be/<your-demo-video-id>](https://youtu.be/y-WOfeP3oSQ)

---

## AI Tool Usage
AI tools were used to:
- Clarify Android/Kotlin concepts  
- Generate Retrofit interfaces and data classes  
- Explain errors  
- Suggest UI layout improvements  
- Help structure README  

All AI-generated content was reviewed, modified, and tested by the developer.

---
Credits

Authors

JN. Pillay – niskajpillay@gmail.com

M. Naidoo – megannaidoo@gmail.com
