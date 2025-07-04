# Notes Organizer Android App

## Overview
Notes Organizer is a feature-rich Android application developed in Java that helps users manage their notes efficiently. The app provides a modern, Material Design 3 compliant interface with full support for both light and dark themes.

## Features
- **Note Management**
  - Create, edit, and delete notes
  - Categorize notes for better organization
  - Mark notes as favorites
  - Move notes to trash (with recovery option)
  - PDF attachment support with format preservation

- **User Interface**
  - Material Design 3 implementation
  - Responsive drawer navigation
  - Floating Action Button (FAB) for quick actions
  - Custom background images for enhanced visual appeal
  - Proper back navigation handling

- **Theme Support**
  - Dynamic theme switching between light and dark modes
  - Consistent color scheme across both themes
  - Custom styled input fields and text components
  - Proper contrast and visibility in both modes

- **Security**
  - User authentication system
  - Secure login functionality

## Technical Specifications

### Architecture
- Language: Java
- Minimum SDK Version: [Your min SDK version]
- Target SDK Version: [Your target SDK version]

### Dependencies
```gradle
// Add your main dependencies here
```

### Data Persistence
The app uses Room Database for data persistence, providing:
- Structured data storage for notes
- Efficient querying and data management
- Type-safe database access

### Key Components
1. **Activities**
   - MainActivity: Main interface with navigation drawer
   - LoginActivity: User authentication
   - [Other activities]

2. **Data Models**
   - Note: Core data structure for notes
   - User: User authentication data

3. **Database**
   - Room Database implementation
   - DAOs for data access
   - Repository pattern for data management

4. **UI Components**
   - Custom TextInputLayout styles
   - Material Design components
   - Responsive layouts

## Setup and Installation
1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Run on an emulator or physical device

## Theme Implementation
The app implements a comprehensive theming system:
- Light theme with appropriate color schemes
- Dark theme with proper contrast
- Custom styles for input fields
- Background images for specific activities

## Future Enhancements
- Cloud synchronization
- Note sharing capabilities
- Rich text formatting
- Tags and advanced categorization
- Search functionality
- Export options

## Project Report

### Technical Architecture
The project follows the MVVM (Model-View-ViewModel) architecture pattern:
- **Model**: Room Database with entities and DAOs
- **View**: XML layouts with Material Design components
- **ViewModel**: Manages UI data and business logic

### Data Flow
1. User interactions trigger UI events
2. ViewModels process these events
3. Repository handles data operations
4. Room Database manages persistent storage
5. LiveData updates UI with changes

### Design Decisions
1. **Java Implementation**: Chosen for broader developer accessibility
2. **Material Design 3**: Ensures modern, consistent UI/UX
3. **Room Database**: Provides robust data persistence
4. **Custom Theming**: Ensures proper visibility in both themes

### Challenges Addressed
1. PDF format preservation in attachments
2. Theme-based visibility issues
3. Navigation flow optimization
4. Input field styling across themes

### Performance Considerations
- Efficient database queries
- Optimized layout hierarchies
- Proper background thread usage
- Memory management for PDF handling

## Data Persistence Analysis
This project uses **Dynamic Storage** through Room Database, not SharedPreferences. Here's why:

1. **Room Database (Current Implementation)**
   - Structured data storage
   - Complex queries support
   - Type safety
   - Large data handling
   - Relationship management
   - CRUD operations
   - Background thread support

2. **Why Not SharedPreferences**
   SharedPreferences would be insufficient because:
   - Limited to key-value pairs
   - No complex data structure support
   - No query capabilities
   - Not suitable for large datasets
   - No relationship management

The choice of Room Database makes this a dynamic storage solution, which is more appropriate for a notes application that needs to handle:
- Multiple notes with various attributes
- PDF attachments
- Categories and relationships
- Complex queries for filtering and sorting
- Large datasets 