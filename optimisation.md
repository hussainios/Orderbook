# Optimisation Benchmarks

Use this file to track benchmark results after each optimisation pass.

## Benchmark Setup

- Date: 2026-06-12
- Command: `python3 scripts/run_benchmarks.py --date YYYY-MM-DD --runs 5`
- Workload size: `1,000,000` commands per file
- Seed: `42`
- Runs per workload: `5`
- Machine note: benchmark timings and latencies are machine-dependent, so compare runs taken on the same setup where possible
- Table note: use medians across runs, and prefer throughput plus `p95`/`p99` over `p50` because single-command timer resolution bottoms out on this machine

## Baseline

| Date | Workload | Runs | Median Elapsed ms | Median Cmds/s | Slowest Cmds/s | Fastest Cmds/s | Cmds/s Spread | Median P95 ns | Median P99 ns |
| --- | --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| 2026-06-12 | `balanced_1000000_42.csv` | 5 | 2,686.040 | 372,295.246 | 343,168.814 | 485,388.933 | 38.20% | 13,542 | 35,667 |
| 2026-06-12 | `cancel_heavy_1000000_42.csv` | 5 | 456.162 | 2,192,203.248 | 2,113,552.363 | 2,329,171.526 | 9.84% | 250 | 917 |
| 2026-06-12 | `large_book_1000000_42.csv` | 5 | 2,889.976 | 346,023.665 | 309,508.835 | 352,306.357 | 12.37% | 10,958 | 68,834 |
| 2026-06-12 | `iceberg_heavy_1000000_42.csv` | 5 | 3,180.708 | 314,395.428 | 223,993.731 | 328,877.964 | 33.36% | 6,708 | 79,167 |


## First optimisation

Summary: replaced per-price `ArrayDeque` cancel scans with a linked-list price level and stored node references in `liveOrders`, so `cancelOrder` can remove resting orders directly.

| Date | Workload | Runs | Elapsed ms | Cmds/s | P95 ns | P99 ns |
| --- | --- | ---: | ---: | ---: | ---: | ---: |
| 2026-06-12 | `balanced_1000000_42.csv` | 1 | 555.046 | 1,801,652.206 | 500 | 1,291 |
| 2026-06-12 | `cancel_heavy_1000000_42.csv` | 1 | 457.943 | 2,183,680.266 | 250 | 833 |
| 2026-06-12 | `large_book_1000000_42.csv` | 1 | 583.898 | 1,712,627.767 | 417 | 1,042 |
| 2026-06-12 | `iceberg_heavy_1000000_42.csv` | 1 | 710.172 | 1,408,110.080 | 958 | 2,958 |


## Template For Next Optimisation

Copy this section and replace the placeholder values after each change:

```md
## Optimisation N: <short change name>

Summary: <one-line description of what changed>

| Date | Workload | Runs | Median Elapsed ms | Median Cmds/s | Slowest Cmds/s | Fastest Cmds/s | Cmds/s Spread | Median P95 ns | Median P99 ns |
| --- | --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| YYYY-MM-DD | `balanced_1000000_42.csv` | 5 | | | | | | | |
| YYYY-MM-DD | `cancel_heavy_1000000_42.csv` | 5 | | | | | | | |
| YYYY-MM-DD | `large_book_1000000_42.csv` | 5 | | | | | | | |
| YYYY-MM-DD | `iceberg_heavy_1000000_42.csv` | 5 | | | | | | | |
```
