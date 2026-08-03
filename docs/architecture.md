# LiftLog Architecture

The app starts as a single Android module with feature-first packages. This keeps setup simple while leaving room for future module extraction.

## Flow

```text
Compose UI
ViewModel
Use Case
Repository
Room / DataStore
```

## Responsibilities

- Compose UI displays state and sends user actions.
- ViewModel owns screen state and converts user actions into use case calls.
- Use cases represent app actions, such as observing exercises or seeding starter data.
- Repositories hide data sources from the rest of the app.
- Room stores workout data locally.
- DataStore stores small preferences such as the preferred weight unit.

## First features

- Exercise search
- Exercise detail history
- Active workout logging
- Set editing
- Workout history
- Basic progress charts

## Future sync

The local repository contracts should stay stable. Firebase, Supabase or a custom API can later be added as a remote data source behind the same repository layer.

