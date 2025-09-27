# Beezle Firebase Auth + Profile Integration

This project now includes:
- Google Sign-In (Firebase Authentication)
- Firestore-backed user profile documents (collection: `users`)
- Wallet linking to a profile (Solana public key)
- Username editing & duel stats placeholder

## 1. Enable Google Sign-In in Firebase Console
1. Open Firebase Console > Your Project > Build > Authentication > Sign-in method.
2. Enable the **Google** provider.
3. Add your Android app SHA-1 / SHA-256 fingerprints (Project Settings > Your Apps). If you haven't generated them:
   ```bash
   keytool -list -v -alias androiddebugkey -keystore "$HOME/.android/debug.keystore" -storepass android -keypass android | grep -E "SHA1|SHA-256"
   ```
4. After saving, re-download the updated `google-services.json` (it will now include `oauth_client` entries).
5. Replace the existing `app/google-services.json` with the new one.
6. The Google Services Gradle plugin will auto-generate a `default_web_client_id` string resource. If so, you can remove the manual placeholder in `res/values/strings.xml`. Otherwise, set the value manually to the **Web client (auto created)** OAuth 2.0 client ID you see in the Google Cloud console (NOT the Android client one).

## 2. Firestore Setup
1. Create (or confirm) Firestore in **Native Mode**.
2. Create `users` collection upon first sign-in automatically (code writes the document).
3. Recommended security rules (basic):
   ```
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       match /users/{userId} {
         allow read: if request.auth != null && request.auth.uid == userId; // user can read own profile
         allow create: if request.auth != null && request.auth.uid == userId;
         allow update, delete: if request.auth != null && request.auth.uid == userId;
       }
     }
   }
   ```
   Adjust if you plan public leaderboards (then consider a restricted aggregate or a Cloud Function).

## 3. Data Model
`UserProfile` (Firestore document id = Firebase UID):
```kotlin
data class UserProfile(
  val uid: String = "",
  val walletPublicKey: String? = null,
  val username: String? = null,
  val createdAt: Long = System.currentTimeMillis(),
  val mathLevel: Int = 1,
  val csLevel: Int = 1,
  val duelStats: DuelStats = DuelStats()
)
```
`DuelStats` is embedded, updated via atomic field increments when you implement match outcomes.

## 4. Flow Overview
1. User taps "Sign in with Google" (Profile screen when signed out).
2. Google Sign-In returns an ID token; Firebase Auth signs the user in.
3. `ProfileViewModel.refresh()` fetches or creates the `users/{uid}` document.
4. If a Solana wallet is already connected and no wallet is linked, it sets `walletPublicKey`.
5. User can edit username (writes to Firestore).
6. User can then link a wallet if not yet linked (button shows when both conditions satisfied).

## 5. Where Things Live
| Concern | File |
|---------|------|
| Auth UI state | `profile/AuthViewModel.kt` |
| Profile data / Firestore access | `profile/UserProfileRepository.kt` |
| Profile screen (Compose) | `profile/ProfileScreen.kt` |
| Profile view model | `profile/ProfileViewModel.kt` |
| DI (Auth/Firestore providers) | `di/AppModule.kt` |

## 6. Why Firestore (vs Realtime DB / Postgres)?
- Firestore gives you: offline caching, scalable doc/collection model, atomic updates (`FieldValue.increment`), security rules tied to Firebase Auth.
- You do **not** need Postgres for early-stage profile + stats. Add a dedicated backend later only if you need complex server logic or multi-collection transactions beyond Firestore's capabilities.
- For deterministic, trustless interactions (on-chain), keep that logic on Solana + signable payloads; store only mirrored/profile metadata in Firestore.

## 7. Performance Notes
- User profile loads once on entering screen; you can cache it in memory or use a snapshot listener for real-time updates (optional improvement).
- Use batched writes or transactions if you introduce multiple stat increments at once.

## 8. Next Suggested Improvements
- Add snapshot listener to auto-refresh profile when changed from another device.
- Add unique username reservation (Cloud Function or Firestore query + security rules).
- Add avatar upload (Firebase Storage).
- Implement duel result writer: `incrementWin(uid, won)` in `UserProfileRepository` already scaffolded.
- Add offline indicator / retry logic.

## 9. Troubleshooting
| Issue | Fix |
|-------|-----|
| `default_web_client_id` not found | Re-download `google-services.json` after enabling Google sign-in. Clean/rebuild. |
| Sign-In loops / returns null user | Ensure SHA-1 added and correct OAuth client selected. |
| Permission denied (Firestore) | Update rules; ensure you are authenticated; check document path. |
| Username not persisting | Check Firestore console for write errors / security rules. |

## 10. Build Commands
```bash
./gradlew :app:clean :app:assembleDebug
```

## 11. Linking Wallet Later
If user signs in first and links wallet later, just connect the wallet; the Profile screen automatically offers a "Link Connected Wallet" button if `walletPublicKey` is still null.

---
Feel free to extend this with leaderboard aggregation using a Cloud Function that writes to a `leaderboards` collection with sanitized public fields.

