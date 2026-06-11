# Limit/Iceberg Order Book Simulator

This project is an in-memory SETS-style order book simulator. It supports:

- `ADD` limit orders
- `ADD` iceberg orders
- `CANCEL` by `orderId`
- replay from CSV
- summary-only benchmark runs

The simulator reads CSV commands, applies them in order, prints trades plus book snapshots in normal mode, and prints summary metrics in benchmark mode.

## Command Format
### Supported commands
```text
ADD,B,100322,5103,7500
ADD,S,100345,5103,100000,10000
CANCEL,100322
```

### Formats
- `ADD,Side,Id,Price,Quantity`
- `ADD,Side,Id,Price,Quantity,Peak`
- `CANCEL,Id`

### Notes
- `Side` is `B` or `S`
- `Price` is in whole pence
- `Peak` is the visible quantity for an iceberg order
- empty lines and lines starting with `#` are ignored
- duplicate live `orderId`s are ignored
- cancelling a non-live order is ignored
- legacy syntax like `B,100322,5103,7500` is rejected

## Matching Behavior
- matching uses price-time priority
- resting iceberg orders replenish when visible quantity is exhausted
- replenished icebergs move to the back of their price level
- incoming iceberg orders replenish before resting if quantity remains
- successful cancels remove the remaining resting quantity for that order

## Running The Project
### Prerequisites
- Java (JDK)
- Maven

### Recommended commands
From the project root:

```bash
make test
make package
make replay
make benchmark
```

To use a different workload:

```bash
make replay WORKLOAD=workloads/your_file.csv
make benchmark WORKLOAD=workloads/your_file.csv
```

### Direct Maven commands
```bash
mvn test
mvn package
mvn -q exec:java -Dexec.mainClass=SETSOrderBookExercise < input.csv
mvn -q exec:java -Dexec.mainClass=SETSOrderBookExercise -Dexec.args="--input workloads/canonical_replay.csv"
mvn -q exec:java -Dexec.mainClass=SETSOrderBookExercise -Dexec.args="--input workloads/canonical_replay.csv --benchmark"
```

## CLI Options
- `--input <path>` reads commands from a file instead of `stdin`
- `--benchmark` suppresses per-event output and prints summary metrics only

Benchmark output includes:
- `totalLinesRead`
- `acceptedCommands`
- `acceptedAdds`
- `acceptedCancels`
- `tradeCount`
- `elapsedMillis`
- `commandsPerSecond`

## Output
In normal mode the simulator prints:

1. any resulting trades as `buyId,sellId,price,quantity`
2. the full book snapshot after each accepted command

IDs are printed plainly, and prices and quantities use comma separators.

## Tests
The test suite covers:

- parser behavior
- order and trade types
- order book matching and cancellation
- output formatting
- end-to-end replay scenarios

Run all tests with:

```bash
mvn test
```

## Included Workload
The repo includes one sample replay file:

- [canonical_replay.csv](/Users/hussainiqbal/orderbook/Orderbook/workloads/canonical_replay.csv)

It exercises:

- both sides of the book
- a trade
- a successful cancel
- ignored commands for benchmark accounting
