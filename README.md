# YOUNOVA — Your AI. Your Universe.

YOUNOVA is a production-grade, privacy-first, professional AI workspace application built natively for Android using Kotlin and Jetpack Compose. Powered directly by Google Gemini API intelligence, YOUNOVA provides a minimalist pure black workspace with radiant atomic purple accents, real-time multi-turn conversational streaming, customizable models, persistent conversation management, and in-app API configuration.

---

## ⚛️ Brand Identity & Aesthetics

- **Brand Concept**: *Your AI. Your Universe.*
- **Logo**: A custom purple atomic symbol composed of three intersecting orbital elliptical paths surrounding an illuminated glowing nucleus.
- **Color Palette**:
  - Pure Black Background: `#000000`
  - Primary Purple: `#A855F7`
  - Bright Purple Accent: `#C084FC`
  - Deep Purple: `#7E22CE`
  - Subtle Purple Glow: `rgba(168, 85, 247, 0.18)`
  - Secondary Surfaces: `#0A0A0A`
  - Elevated Cards: `#18151D`
  - Borders: `#242027`
  - Primary Text: `#F5F3F7`
  - Secondary Text: `#A1A1AA`

---

## 🚀 Key Features

### 1. Real Google Gemini AI Integration
- **Direct REST API & Server-side Proxy**: Integrates with Google AI Studio's Gemini models via OkHttp with 60-second timeouts.
- **Streaming Responses**: Real-time server-sent events (`streamGenerateContent?alt=sse`) for low-latency token streaming.
- **Stop Generation**: Immediate cancellation of ongoing generation via atomic request aborting.
- **Multi-turn Context**: Full conversation turn history maintained with intelligent context window management.
- **Supported Models**:
  - `gemini-3.5-flash` (Default fast model for reasoning, coding, and chat)
  - `gemini-3.1-pro-preview` (Advanced deep reasoning and complex coding)
  - `gemini-3.1-flash-lite-preview` (Ultra-low latency model)
  - `gemini-flash-latest` (General task model)

### 2. Persistent API Configuration & Connection Testing
- Dedicated **API Configuration** section in Settings.
- Support for both AI Studio secrets (`BuildConfig.GEMINI_API_KEY`) and custom user-entered API keys.
- Masked key persistence across device restarts and app launches (`••••••••••••abcd`).
- Live **Test Connection** action that validates API credentials against Google AI Studio with explicit connection status states (`Connected`, `Checking...`, `Invalid API Key`, `Quota Exceeded`, `Rate Limit Reached`, `Connection Failed`).
- Ability to update or remove keys with confirmation dialogs.

### 3. Conversation Management (Room Database)
- **Local SQLite Persistence**: Fully offline-capable local storage using Jetpack Room with foreign-key cascade deletions.
- **Categorized Sidebar / Drawer**:
  - Pinned chats
  - Today
  - Yesterday
  - Previous 7 Days
  - Older
  - Archived chats
- **Context Actions**:
  - Rename conversation
  - Pin / Unpin
  - Archive / Unarchive
  - Delete with confirmation
  - Export full conversation as Markdown
- **Live Search**: Instant search filtering through conversation titles.

### 4. Rich Message Presentation & Formatting
- **Markdown Rendering**: Headings (`#`, `##`, `###`), bold text, bulleted & numbered lists, blockquotes.
- **Syntax-Highlighted Code Blocks**: Clean dark code containers with language tags, horizontal scrolling, and a one-tap **Copy Code** button with visual confirmation.
- **Message Action Bar**: Copy message text, Thumbs Up / Thumbs Down feedback ratings, Regenerate response, and timestamp display.
- **Error Recovery**: Actionable error states with a one-tap **Retry** button.

### 5. Multimodal & Voice Composer
- Bottom-anchored rounded composer with auto-expanding multiline input.
- **Attachment Support**: Zero-permission photo picker for sending image attachments with previews and removal controls.
- **Voice Input**: Integrated Android Speech-to-Text (`RecognizerIntent`) allowing voice review before submission.
- **Keyboard Shortcut**: Press Enter to send with Shift+Enter for newlines.

---

## 🛠️ Architecture & Tech Stack

- **Language**: Kotlin 2.2+
- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: Clean MVVM (Model-View-ViewModel) with Unidirectional Data Flow (UDF)
- **Persistence**: Room Database 2.7 (KSP symbol processing) + SharedPreferences
- **Networking**: OkHttp 4.10 + Kotlin Coroutines & Flow
- **Image Handling**: Coil Compose + Android Base64 Image Decoder

---

## ⚙️ Configuration & Secrets

API keys can be supplied through two methods:
1. **AI Studio Secrets (Recommended for Development)**:
   - Configure `GEMINI_API_KEY` in the AI Studio Secrets panel.
   - The key is automatically injected into `BuildConfig.GEMINI_API_KEY`.
2. **In-App API Configuration**:
   - Open **Settings** > **API Key**.
   - Paste your key from [Google AI Studio](https://aistudio.google.com/).
   - Tap **Save Key** & **Test Connection**.

---

## 📱 Running & Testing

To compile and verify the build locally:
```bash
gradle :app:assembleDebug
```

To run unit tests:
```bash
gradle :app:testDebugUnitTest
```
