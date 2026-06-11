# Limit/Iceberg Order Book Simulator

This project simulates a SETS-style order book with support for:

- `ADD` limit orders
- `ADD` iceberg orders
- `CANCEL` by `orderId`

It reads CSV commands from `stdin`, prints any resulting trades, and then prints the full book after each valid accepted command.

## Overview
- `InputParser` parses each input line into a `BookCommand`
- `OrderBook` processes `ADD` and `CANCEL` commands
- `OrderOutputter` prints trades and the current book snapshot

## Input Format
### Add limit order
```text
ADD,B,100322,5103,7500
```

Format: `ADD,Side,Id,Price,Quantity`

### Add iceberg order
```text
ADD,S,100345,5103,100000,10000
```

Format: `ADD,Side,Id,Price,Quantity,Peak`

### Cancel order
```text
CANCEL,100322
```

Format: `CANCEL,Id`

### Field meanings
- `Side`: `B` for buy or `S` for sell
- `Price`: whole pence
- `Quantity`: total order quantity
- `Peak`: visible quantity for an iceberg order

## Command Behavior
- Empty lines and lines starting with `#` are ignored
- Legacy implicit add syntax like `B,100322,5103,7500` is rejected
- `CANCEL` for a non-live order is invalid and ignored
- `ADD` with an `orderId` that is already live on the book is invalid and ignored
- A successful `CANCEL` prints the updated book and no trades

## Output Format
After each valid accepted command, the simulator outputs:

1. Trades, if any, in the format:
```text
buyId,sellId,price,quantity
```

2. The current order book with fixed-width columns

IDs are printed as plain numbers. Prices and volumes use comma separators.

## Matching and Iceberg Behavior
- Matching is price-time priority within the current queue structure
- When a resting iceberg’s visible quantity is exhausted and hidden quantity remains, it replenishes and moves to the back of its price level
- Incoming iceberg orders that partially match replenish their visible quantity before resting on the book
- `CANCEL` removes the remaining resting quantity for the targeted live order

## Entrypoint
`SETSOrderBookExercise` reads from `stdin` and writes to `stdout`.

## Tests
Run the test suite with:

```bash
mvn test
```

The project includes:
- unit tests for parser, orders, trades, output, and book behavior
- end-to-end limit/add/cancel tests in `VerificationTest`
- end-to-end iceberg scenarios in `TestIceberg`

## How to Run
### Prerequisites
- Java (JDK)
- Maven

### Build and test
```bash
mvn test
mvn package
```

### Run the simulator
```bash
mvn -q exec:java -Dexec.mainClass=SETSOrderBookExercise < input.csv
```

Example:

```bash
cat <<'EOF' | mvn -q exec:java -Dexec.mainClass=SETSOrderBookExercise
ADD,B,100322,5103,7500
ADD,S,100345,5103,100000,10000
CANCEL,100322
EOF
```
