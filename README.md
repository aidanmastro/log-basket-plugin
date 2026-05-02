# Log Basket

A [RuneLite](https://runelite.net) plugin that tracks and displays the number of logs stored in your log basket (or forestry basket) without you needing to manually check it.

## Features

- **Inventory overlay**: draws the current log count directly on the basket item in your inventory (enabled by default)
- **InfoBox overlay**: optional corner InfoBox showing the same count (disabled by default; opt in via settings)
- Automatically increments the count as you cut logs
- Detects fills, empties, and partial empties via inventory changes and game messages
- Shows `?` when the count is not yet known; use **Check** on the basket to confirm the exact amount

## InfoBox colours

| Colour | Meaning                        |
|--------|--------------------------------|
| White  | Count is known                 |
| Yellow | Count is unknown : use "Check" |
| Red    | Basket is full (28 logs)       |

## Configuration

| Option | Default | Description |
|--------|---------|-------------|
| Show inventory overlay | On  | Draw the count on the basket item in your inventory |
| Show InfoBox           | Off | Show a separate corner InfoBox with the count (opt in) |

## Getting an accurate count

The plugin infers the basket count from game events and is accurate in most cases. To guarantee an exact count at any time, right-click the basket and select **Check**, the plugin will read the result automatically.

## Installation

Install via the RuneLite Plugin Hub: "**Log Basket**".

For manual installation during development, build the shadow JAR and load it as an external plugin:

```
./gradlew shadowJar
```

Then run RuneLite with the generated JAR from `build/libs/`.
