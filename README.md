# FlexShare

A full-stack marketplace app where people list tools and items to **sell or rent by the day**. Built end to end by one developer: a native Android client in Kotlin, a custom REST backend in Ktor, and a containerized deployment on Render.

## What it does

- List an item for sale or for daily rent
- Browse listings shared by other users
- Create and manage bookings for rented items
- Load listing images efficiently from the network

## Screenshots



| Home | Listing details | Booking |
|------|-----------------|---------|
| ![Home](<img width="344" height="715" alt="image" src="https://github.com/user-attachments/assets/61ffba0b-3c23-476e-931b-e7c3511ed842" />) | ![Details](<img width="336" height="723" alt="image" src="https://github.com/user-attachments/assets/378502a7-afca-4f9c-9778-02766ba689b5" />) | ![Booking](<img width="326" height="715" alt="image" src="https://github.com/user-attachments/assets/276fa0e7-c8cc-4efe-a66c-00ad6ea90197" />) |

## Tech stack

| Layer | Technology |
|-------|------------|
| Android UI | Kotlin, Jetpack Compose (fully declarative UI) |
| Architecture | MVVM with unidirectional data flow (UDF) |
| State management | `MutableStateFlow` and `combine` in `HomeViewModel` |
| Networking | Retrofit 2, OkHttp, GsonConverterFactory |
| Image loading | Coil |
| Backend | Ktor (Kotlin asynchronous server framework) |
| Data layer | Embedded, in-memory database layer served through Ktor routing |
| Deployment | Docker (multi-stage `Dockerfile`), hosted on Render |

## Architecture

```
┌──────────────────────────┐        HTTPS / JSON        ┌───────────────────────────┐
│  Android app (Kotlin)    │  ───────────────────────►  │  Ktor backend (Docker)    │
│  Jetpack Compose UI      │   Retrofit 2 + OkHttp      │  /v1/listings             │
│  ViewModel + StateFlow   │  ◄───────────────────────  │  /v1/bookings             │
└──────────────────────────┘                            │  hosted on Render         │
                                                        └───────────────────────────┘
```

**Client.** The UI is written in Jetpack Compose and follows MVVM with unidirectional data flow. Screen state lives in a ViewModel as `MutableStateFlow`, and `combine` merges several data streams into one reactive UI state, so the UI only renders state and sends events back up.

**Backend.** A custom Ktor server exposes REST endpoints under `/v1/listings` and `/v1/bookings`, handling create, read, update, and delete operations for marketplace listings and booking transactions.

**Deployment.** The backend is packaged with a multi-stage Docker build, which keeps the final image small, and deployed to Render so the app can talk to a live server.

## API overview

| Resource | Endpoint | Purpose |
|----------|----------|---------|
| Listings | `/v1/listings` | CRUD for items listed for sale or rent |
| Bookings | `/v1/bookings` | CRUD for booking transactions |

## Running locally

**Android app**

1. Open the Android project in Android Studio.
2. Set the backend base URL to your local or deployed server.
3. Run on an emulator or a physical device.

**Backend**

```bash
cd [backend-folder]
docker build -t flexshare-backend .
docker run -p [port]:[port] flexshare-backend
```

## Known limitations

- The data layer is in-memory, so data resets when the server restarts. This was a deliberate choice to keep the project focused on API design and the client-server flow.
- There is no user authentication yet.

## Possible improvements

- Replace the in-memory layer with a persistent database such as PostgreSQL
- Add user accounts and authentication
- Add automated tests for the ViewModels and API routes

## What I learned

- Structuring a Compose app around unidirectional data flow so state stays predictable
- Designing a small REST API and keeping the client and server contracts in sync
- Containerizing a Kotlin service with a multi-stage Docker build and deploying it to the cloud
- Debugging across the whole stack, from UI state to network calls to deployment configuration

## Author

**Turki Abdo**, Computer Engineering graduate (University of Georgia, 2026), Jeddah, Saudi Arabia
[LinkedIn](https://www.linkedin.com/in/turki-abdo) · mr.turki.salah@gmail.com
