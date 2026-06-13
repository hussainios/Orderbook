#!/usr/bin/env python3
import argparse
import statistics
import subprocess
from dataclasses import dataclass
from pathlib import Path
from typing import Dict, List


ROOT_DIR = Path("/Users/hussainiqbal/orderbook/Orderbook")
WORKLOADS = [
    "balanced_1000000_42.csv",
    "cancel_heavy_1000000_42.csv",
    "large_book_1000000_42.csv",
    "iceberg_heavy_1000000_42.csv",
]


@dataclass
class RunMetrics:
    elapsed_ms: float
    commands_per_second: float
    p95_ns: int
    p99_ns: int


def parse_summary(output: str) -> Dict[str, str]:
    metrics: Dict[str, str] = {}
    for line in output.splitlines():
        if "=" not in line:
            continue
        key, value = line.split("=", 1)
        metrics[key.strip()] = value.strip()
    return metrics


def run_benchmark(workload: str) -> RunMetrics:
    args = (
        'mvn -q exec:java -Dexec.mainClass=SETSOrderBookExercise '
        f'-Dexec.args="--input workloads/{workload} --benchmark"'
    )
    completed = subprocess.run(
        args,
        cwd=ROOT_DIR,
        shell=True,
        check=True,
        capture_output=True,
        text=True,
    )
    metrics = parse_summary(completed.stdout)
    return RunMetrics(
        elapsed_ms=float(metrics["elapsedMillis"]),
        commands_per_second=float(metrics["commandsPerSecond"]),
        p95_ns=int(metrics["p95LatencyNanos"]),
        p99_ns=int(metrics["p99LatencyNanos"]),
    )


def median_float(values: List[float]) -> float:
    return statistics.median(values)


def median_int(values: List[int]) -> int:
    return int(statistics.median(values))


def throughput_spread_percent(values: List[float]) -> float:
    median = statistics.median(values)
    if median == 0:
        return 0.0
    return (max(values) - min(values)) * 100.0 / median


def format_row(date: str, workload: str, runs: List[RunMetrics]) -> str:
    elapsed_values = [run.elapsed_ms for run in runs]
    throughput_values = [run.commands_per_second for run in runs]
    p95_values = [run.p95_ns for run in runs]
    p99_values = [run.p99_ns for run in runs]

    return (
        f"| {date} | `{workload}` | {len(runs)} | "
        f"{median_float(elapsed_values):,.3f} | "
        f"{median_float(throughput_values):,.3f} | "
        f"{min(throughput_values):,.3f} | "
        f"{max(throughput_values):,.3f} | "
        f"{throughput_spread_percent(throughput_values):.2f}% | "
        f"{median_int(p95_values):,} | "
        f"{median_int(p99_values):,} |"
    )


def main() -> None:
    parser = argparse.ArgumentParser(description="Run benchmark mode multiple times and print markdown rows.")
    parser.add_argument("--runs", type=int, default=5)
    parser.add_argument("--date", required=True)
    args = parser.parse_args()

    if args.runs <= 0:
        parser.error("--runs must be greater than 0")

    for workload in WORKLOADS:
        runs = [run_benchmark(workload) for _ in range(args.runs)]
        print(format_row(args.date, workload, runs))


if __name__ == "__main__":
    main()
