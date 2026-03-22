# BookTracker 📚

this application  allows users to search for books online , mark them as read, rate them, and keep a local record of read books. It is built using Kotlin and follows the Clean Architecture pattern, separating concerns across data, domain, and presentation layers.

## ✨ Features

- **📖 Reading Management**: Organize books into three states - To Read, Currently Reading, and Finished
- **⭐ Rating & Reviews**: Rate books with a 5-star system and write personal reviews
- **❤️ Favorites**: Mark your favorite finished books for quick access
- **🔍 Book Search**: Search for books using multiple APIs (Gutendex, Google Books)
- **📊 Reading Statistics**: Track total books read, average ratings, and reading progress
- **🎨 Modern UI**: Material Design 3 with Navigation Drawer, Bottom Navigation, and responsive layouts
- **🗑️ Smart Management**: Delete books with confirmation dialogs, swipe gestures support
- **📱 Responsive Design**: Optimized for phones and tablets with adaptive layouts


## 🛠 Tech Stack

- **Language**: Kotlin
- **Architecture**: Clean Architecture (Presentation, Domain, Data)
- **Networking**: Retrofit + Gson
- **Database**: Room (with DAOs and Entities)
- **UI**: RecyclerView, Material Components
- **MVVM**: ViewModel + LiveData
- **Dependency Management**: Gradle

## Firebase Setup

This project requires Firebase configuration.

1. Create a Firebase project
2. Add an Android app with the same package name
3. Download `google-services.json`
4. Place it inside the `/app` directory
## 📦 Structure

## 🏗️ Architecture

The app follows **Clean Architecture** principles with clear separation of concerns:
```
app/
├── data/
│   ├── local/
│   │   ├── dao/          # Room DAOs
│   │   ├── database/     # Database & Converters
│   │   └── entities/     # Room Entities
│   ├── remote/
│   │   ├── api/          # Retrofit API Services
│   │   └── dto/          # Data Transfer Objects
│   └── repository/       # Repository Implementations
├── domain/
│   ├── model/            # Domain Models (Book, ReadingStatus, UserStats)
│   ├── repository/       # Repository Interfaces
│   └── usecase/          # Business Logic Use Cases
└── presentation/
    ├── adapter/          # RecyclerView Adapters
    ├── dialogs/          # Dialog Fragments
    ├── fragments/        # UI Fragments
    └── viewmodel/        # ViewModels
```

## 🛠️ Tech Stack

### Core
- **Language**: Kotlin
- **Architecture**: Clean Architecture + MVVM
- **UI Pattern**: MVVM with LiveData

### Android Components
- **UI**: Material Design 3, BottomNavigation, NavigationDrawer, BottomSheetDialog
- **Database**: Room with TypeConverters and Migrations
- **Navigation**: Fragment-based with ViewPager2
- **Networking**: Retrofit + OkHttp + Gson
- **Image Loading**: Glide
- **Async**: Kotlin Coroutines + LiveData


Clone the repository:

   ```bash
   git clone https://github.com/Cielo882/BookTracker.git

   cd appLibros
```
## 📄 License
This project is licensed under the MIT License 
