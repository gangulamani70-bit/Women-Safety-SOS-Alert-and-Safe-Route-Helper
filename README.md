# Women Safety: SOS Alert and Safe Route Helper

A Java 17+ application with a Swing interface and a browser-based localhost interface. It demonstrates account registration, trusted contacts, location selection, nearby safe-place listings, route previews, and SOS history.

## Important safety notice

This is an educational demonstration, not an emergency service. SOS alerts are recorded locally and are not sent to police, hospitals, or contacts. Safe-place data and routes are examples, not live or verified information. Do not rely on this app during an emergency; contact local emergency services directly.

## Features

- Home, login, and account registration; the browser app always starts at login.
- Manage trusted contacts and personal emergency call numbers.
- Use sample locations or explicitly grant browser permission for live location. Coordinates are kept local and hidden from the interface.
- View sample police stations, hospitals, and shops, and preview illustrative routes.
- Confirm an SOS simulation and view its local alert history.
- Passwords for newly registered users are PBKDF2-hashed.
- Click-to-call links require the user to initiate the call; no phone numbers are guessed.

## Requirements

- JDK 17 or newer
- Apache Maven

## Run the Swing app

From the project directory, run `mvn clean test` to run tests, then `mvn exec:java` to launch the desktop app.

## Run the localhost browser app

On Windows, double-click `run-localhost.bat`. It starts the local server and opens `http://localhost:8080`. Keep the command window open while using the app. You can also start it with `mvn -Dexec.mainClass=womensafety.web.LocalhostServer compile exec:java`. The server is intended for local use; do not expose it to a public network.

On first local run the project creates a `data/` folder. It may seed demonstration data locally. Do not commit or upload files from that folder because it can contain account/contact data. Register an account yourself to use the app; no one is signed in automatically.

## Project layout

- `model/`: application data models
- `service/`: authentication, location, safe-place, and alert behavior
- `ui/`: Swing screens
- `web/`: localhost browser server
- `util/`: local persistence and sample data
- `src/test/`: automated service tests

## Optional Render demo

The repository contains a Dockerfile and Render Blueprint configuration for an optional public demo. The free tier has ephemeral storage, may sleep, and can lose data after restarts. Production mode does not seed demo accounts. Do not enter real passwords, phone numbers, or location data in a public deployment. Deployment is not performed automatically by this repository.
