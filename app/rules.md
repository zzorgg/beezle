🏗 Solana Duel Android App – Full Build Plan
Phase 1: Project Setup

Create Android Studio Kotlin project with Jetpack Compose.

Add dependencies:

Jetpack Compose (UI)

Solana Mobile Wallet Adapter SDK (wallets, signing)

Solana Web3 SDK (transactions)

Firebase/Supabase (for backend, matchmaking, storing user profiles).

Phase 2: Onboarding Flow

Goal: Make a polished UI so judges instantly “get it.”

Screens (Jetpack Compose):

Splash Screen → logo + tagline (e.g. “Compete. Win. Earn SOL.”).

Onboarding Carousel (3 slides):

Slide 1: “What is Duel?”

Slide 2: “Compete in math, Solana, web3 challenges”

Slide 3: “Win prizes instantly powered by Solana.”

Get Started Button → moves to wallet connect.

(Use Pager in Jetpack Compose for smooth swipes + Lottie animations for visuals.)

Phase 3: Wallet Connection

Connect Wallet Screen

Integrate SMS Seed Vault.

“Connect Wallet” button.

Show public key once connected.

Wallet Funding (Devnet)

Auto-airdrop some SOL to users for demo (via backend service).

Phase 4: Profile Creation

Profile Screen → user sets:

Name

Avatar (use Coil to pick an image)

Save profile in local DB + Firebase/Supabase so it syncs for matchmaking.

Phase 5: Duel Setup

Choose Duel Options:

Select category: Math, Solana, Web3, Web2

Select timer: 15s or 30s

Select bet: 0.01 SOL or 0.05 SOL (fixed for MVP).

Once chosen → stake SOL into escrow (via smart contract).

Phase 6: Matchmaking

Use Firebase Realtime Database (simplest) or Supabase realtime channels.

Workflow:

Player selects duel → sends “waiting for opponent” state to backend.

Opponent with same category + timer joins.

Backend pairs them → generates game room.

Both players receive same set of questions.

Phase 7: Duel Gameplay

UI:

Question at top.

4 answer buttons.

Timer countdown.

Score updates instantly.

Logic:

Players answer within 15/30s.

Server ensures both got same questions.

After time ends → scores are compared.

Phase 8: Winner Determination + Payout

Backend compares scores.

If Player A wins: send payout transaction from escrow → Player A’s wallet.

If tie: refund both players.

Transaction signature shown in UI + link to Solana Explorer.

Phase 9: Results & History

Show Result Screen:

Winner: “You Won! 🎉 +0.01 SOL”

Loser: “Better luck next time 💔”

Tie: “It’s a Draw 🤝”

Store game history in backend:

Opponent name

Category & time

Outcome

Transaction link

Phase 10: Polishing for Hackathon

Add Lottie animations for onboarding + victory screens.

Use nice color theme (dark mode, neon accent for “duel vibe”).

Make it feel fun + competitive.

Add “Share Result” button → user can share win screenshot.

⚡️ Tech Stack Summary

Frontend (Android) → Kotlin + Jetpack Compose + Coil + Lottie

Wallet & Blockchain → Solana Mobile Wallet Adapter SDK + Solana Web3 SDK

Backend → Firebase (Realtime DB, Auth, Storage) or Supabase

Smart Contract → Rust (Anchor), manages escrow for bets

✅ Hackathon Demo Flow (Judge Experience)

Judge opens APK.

Onboarding screens → wallet connect → profile setup.

Picks category (e.g. Math, 15s, 0.01 SOL).

Gets matched with another player (or demo bot if no player).

Plays quiz → timer runs → scores calculated.

Winner’s wallet shows +0.01 SOL on Devnet.

Judge clicks “View on Solana Explorer” → sees transaction.