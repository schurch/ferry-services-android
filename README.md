# Ferry Services Android

Android port of the existing iOS Scottish Ferries app, built with Jetpack Compose and a modern Android app structure.

## Included

- Compose UI for services, service details, map, disruption HTML, and settings.
- Retrofit + kotlinx.serialization network layer using the same API endpoints as the iOS app.
- DataStore-backed app preferences for installation ID, subscriptions, and notification registration state.
- Firebase Messaging service for installation registration and notification routing into service details.
- Bundled fallback `services.json` seed data copied from the iOS app.
