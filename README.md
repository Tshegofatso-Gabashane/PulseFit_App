# PulseFit

> A fitness tracking Android app for logging workouts, meals, and daily activity.

![Android](https://img.shields.io/badge/Android-API%2024%2B-green)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0-purple)
![Firebase](https://img.shields.io/badge/Firebase-Auth%20%7C%20Firestore-orange)
![License](https://img.shields.io/badge/License-MIT-blue)

---

## About

**PulseFit** is a native Android application that helps users track their fitness journey — from daily workouts and step counts to meals and calorie intake. It features Firebase Authentication, cloud sync via Firestore, a Google Maps integration for route tracking, and a live weather widget for outdoor workout planning.

The app is built with **Kotlin**, **Jetpack**, and **Material 3** components, targeting a clean, modern, and minimal user experience.

---

##  Features

| Feature | Description |
|---|---|
| **Authentication** | Email/password sign-up and login via Firebase Auth |
|  **Dashboard** | Daily overview of calories, active time, distance, and XP level |
|  **Exercises** | Browse and filter exercises by category (Cardio, Strength) |
|  **Diet Log** | Track meals, calories consumed/burned, daily budget |
|  **Activity Map** | Search destinations and track runs on Google Maps |
|  **Cloud Sync** | Firestore keeps user data and preferences in sync |
|  **Weather Widget** | Live weather data for outdoor workout planning |
| **Settings** | Language, units, sync preference, and profile details |

---

##  Tech Stack

**Language & Framework**
- Kotlin
- Android SDK (min API 24, target API 36)
- Jetpack (ViewModel, LiveData, Navigation)

**UI**
- Material 3 components
- ConstraintLayout, RecyclerView, ViewPager2
- Custom vector drawables

**Networking & Data**
- Retrofit 3.0 + Gson
- OkHttp (via Retrofit)
- REST API for exercises, meals, and weather

**Backend**
- Firebase Authentication
- Cloud Firestore
- Google Maps SDK for Android

**Build**
- Gradle (Kotlin DSL)
- Version catalog (`libs.versions.toml`)

---


---

##  Getting Started

### Prerequisites

- **Android Studio** Koala (2024.1.1) or newer
- **JDK 11** or higher
- **Android SDK** API 24+
- **Node.js 18+** (only if running the local API server)
- A **Firebase account**
- A **Google Maps API key**
- An **OpenWeatherMap API key**

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/PulseFit_App.git
cd PulseFit_App
MIT License

Copyright (c) 2026 [Your Name]

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
