# Ferry Services Android

Android port of the existing iOS Scottish Ferries app, built with Jetpack Compose and a modern Android app structure.

## Included

- Compose UI for services, service details, map, disruption HTML, and settings.
- Retrofit + kotlinx.serialization network layer using the same API endpoints as the iOS app.
- DataStore-backed app preferences for installation ID, subscriptions, and notification registration state.
- Firebase Messaging service for installation registration and notification routing into service details.
- Bundled fallback `services.json` seed data copied from the iOS app.

## Play Store internal deployment

Pushes to `master` can automatically build a signed release bundle and upload it to the Google Play `internal` track using [`.github/workflows/deploy-play-internal.yml`](.github/workflows/deploy-play-internal.yml).

After an internal build is uploaded, you can promote that tested build to production from Play Console when you decide to release it.

### Required GitHub secrets

- `ANDROID_KEYSTORE_BASE64`: Base64-encoded upload keystore file.
- `ANDROID_RELEASE_STORE_PASSWORD`: Upload keystore password.
- `ANDROID_RELEASE_KEY_ALIAS`: Upload key alias.
- `ANDROID_RELEASE_KEY_PASSWORD`: Upload key password.
- `GOOGLE_SERVICES_JSON`: Full contents of `app/google-services.json`.
- `MAPS_API_KEY`: Google Maps API key used for the manifest placeholder.
- `PLAY_SERVICE_ACCOUNT_JSON`: Full JSON for a Play Console service account with release permissions.

### Required Play Console setup

1. Create a service account in Google Cloud.
2. Link it in Play Console under `Users and permissions`.
3. Grant it access to the app with at least the permissions needed to manage internal testing releases.
4. Download the JSON key and store it in the `PLAY_SERVICE_ACCOUNT_JSON` GitHub secret.

### Version codes

GitHub Actions passes `CI_VERSION_CODE=$GITHUB_RUN_NUMBER` to Gradle for release builds. The app then adds a default base offset of `10000`, so uploaded Play builds use version codes like `10001`, `10002`, and so on.

Local builds keep the checked-in version code unless you override `CI_VERSION_CODE` yourself.
