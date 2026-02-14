# SETSmm Iceberg Order Book Simulator

This is a simulator for a SETS-style order book. It supports **limit** and
**iceberg** orders, reads input from `stdin` as CSV, prints any resulting trades, and then prints
the full book after each valid order.

## Overview
- `InputParser` turns each input line into an `Order`.
- `OrderBook` adds the order to the orderbook and returns any trades that are created as a result
- `OrderOutputter` prints trades and the book snapshot.

## Input Format (CSV)
**Limit order**
```
B,100322,5103,7500
```
Format: `Side,Id,Price,Quantity`

**Iceberg order**
```
S,100345,5103,100000,10000
```
Format: `Side,Id,Price,Quantity,Peak`

- `Side`: `B` (buy) or `S` (sell)
- `Price`: whole pence
- `Peak`: visible quantity (<= total quantity)

Empty lines or lines starting with `#` are ignored

## Output Format
After each line is processed, Creates trades and the current Orderbook state are outputted.

1. **Trades** :
```
buyId,sellId,price,quantity
```

2. **OrderBook** formatted with fixed column widths:
- IDs are plain numbers (no commas)
- Prices and volumes use comma separators

## Assumptions and scope
1) **Iceberg Execution**:
   - When an existing iceberg’s visible quantity is fully traded, it is replenished from its remaining total quantity.
   - Upon replenishment, the order **loses time priority** and is re-queued at the back of its price level.
   - Incoming iceberg orders that match partially will also replenish their visible quantity before entering the book.
2) Inputs are valid, so `InputParser` does not include defensive error handling.
3) Price, ID, and quantity values fit within the required output widths.
4) I assume that Iceberg orders not have PeakSize = 0

## Entrypoint
`SETSOrderBookExercise` reads from `stdin` and writes to `stdout`.

## Tests
JUnit 5 tests are used for tests. They can be run with:

```
mvn test
```

This projects contains unit tests labeled by file, End-to-end tests for iceberg orders in TestIceberg.java and for 
limit orders in VerificationTest.java 



## How to run

### Prerequisites
- Java (JDK) installed
- Maven installed

### Build and run tests
```bash
mvn test
Build the project
mvn package
Run the simulator
Reads CSV from stdin and writes results to stdout.
mvn -q exec:java -Dexec.mainClass=SETSOrderBookExercise < input.csv
Example (inline input)
cat << 'EOF' | mvn -q exec:java -Dexec.mainClass=SETSOrderBookExercise
B,100322,5103,7500
S,100345,5103,100000,10000
