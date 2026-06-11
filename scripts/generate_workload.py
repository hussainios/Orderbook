#!/usr/bin/env python3
import argparse
import random
from collections import defaultdict
from dataclasses import dataclass
from collections import deque
from pathlib import Path
from typing import Deque, Dict, List, Optional, Set


WORKLOADS_DIR = Path("/Users/hussainiqbal/orderbook/Orderbook/workloads")


@dataclass(frozen=True)
class RegimeConfig:
    cancel_ratio: float
    iceberg_ratio: float
    crossing_ratio: float
    buy_offsets: List[int]
    sell_offsets: List[int]
    quantity_min: int
    quantity_max: int
    iceberg_quantity_min: int
    iceberg_quantity_max: int
    peak_divisor_min: int
    peak_divisor_max: int
    min_live_orders_for_cancel: int


@dataclass
class SimOrder:
    order_id: int
    side: str
    price: int
    total_quantity: int
    peak: Optional[int]
    visible_quantity: int

    @property
    def is_iceberg(self) -> bool:
        return self.peak is not None

    def reduce(self, quantity: int) -> None:
        self.total_quantity -= quantity
        self.visible_quantity -= quantity

    def replenish(self) -> None:
        if self.total_quantity <= 0:
            self.visible_quantity = 0
            return
        if self.peak is None:
            self.visible_quantity = self.total_quantity
            return
        self.visible_quantity = min(self.total_quantity, self.peak)


REGIMES: Dict[str, RegimeConfig] = {
    "balanced": RegimeConfig(
        cancel_ratio=0.20,
        iceberg_ratio=0.10,
        crossing_ratio=0.25,
        buy_offsets=[0, 1, 2, 3, 4, 5],
        sell_offsets=[0, 1, 2, 3, 4, 5],
        quantity_min=10,
        quantity_max=250,
        iceberg_quantity_min=200,
        iceberg_quantity_max=2000,
        peak_divisor_min=3,
        peak_divisor_max=8,
        min_live_orders_for_cancel=20,
    ),
    "cancel_heavy": RegimeConfig(
        cancel_ratio=0.45,
        iceberg_ratio=0.05,
        crossing_ratio=0.10,
        buy_offsets=[0, 0, 1, 1, 2],
        sell_offsets=[0, 0, 1, 1, 2],
        quantity_min=10,
        quantity_max=150,
        iceberg_quantity_min=150,
        iceberg_quantity_max=1200,
        peak_divisor_min=3,
        peak_divisor_max=6,
        min_live_orders_for_cancel=50,
    ),
    "large_book": RegimeConfig(
        cancel_ratio=0.08,
        iceberg_ratio=0.05,
        crossing_ratio=0.03,
        buy_offsets=list(range(1, 16)),
        sell_offsets=list(range(1, 16)),
        quantity_min=25,
        quantity_max=300,
        iceberg_quantity_min=250,
        iceberg_quantity_max=2500,
        peak_divisor_min=4,
        peak_divisor_max=10,
        min_live_orders_for_cancel=30,
    ),
    "iceberg_heavy": RegimeConfig(
        cancel_ratio=0.15,
        iceberg_ratio=0.45,
        crossing_ratio=0.18,
        buy_offsets=[0, 1, 1, 2, 3, 5],
        sell_offsets=[0, 1, 1, 2, 3, 5],
        quantity_min=20,
        quantity_max=200,
        iceberg_quantity_min=500,
        iceberg_quantity_max=5000,
        peak_divisor_min=5,
        peak_divisor_max=12,
        min_live_orders_for_cancel=25,
    ),
}


class WorkloadGenerator:
    def __init__(self, regime_name: str, commands: int, seed: int, mid_price: int = 10000):
        self.regime_name = regime_name
        self.config = REGIMES[regime_name]
        self.commands = commands
        self.seed = seed
        self.rng = random.Random(seed)
        self.mid_price = mid_price
        self.next_order_id = 1
        self.live_orders: Dict[int, SimOrder] = {}
        self.live_ids: List[int] = []
        self.live_by_price: Dict[int, Set[int]] = defaultdict(set)
        self.buy_book: Dict[int, Deque[SimOrder]] = defaultdict(deque)
        self.sell_book: Dict[int, Deque[SimOrder]] = defaultdict(deque)

    def generate(self) -> List[str]:
        lines = [
            f"# regime={self.regime_name}",
            f"# commands={self.commands}",
            f"# seed={self.seed}",
        ]

        for _ in range(self.commands):
            if self._should_cancel():
                lines.append(self._generate_cancel())
            else:
                lines.append(self._generate_add())

        return lines

    def _should_cancel(self) -> bool:
        return (
            len(self.live_ids) >= self.config.min_live_orders_for_cancel
            and self.rng.random() < self.config.cancel_ratio
        )

    def _generate_add(self) -> str:
        side = self.rng.choice(["B", "S"])
        is_crossing = self._should_cross(side)
        price = self._choose_price(side, is_crossing)
        order_id = self.next_order_id
        self.next_order_id += 1

        is_iceberg = self.rng.random() < self.config.iceberg_ratio
        if is_iceberg:
            total_quantity = self.rng.randint(
                self.config.iceberg_quantity_min, self.config.iceberg_quantity_max
            )
            divisor = self.rng.randint(
                self.config.peak_divisor_min, self.config.peak_divisor_max
            )
            peak = max(1, total_quantity // divisor)
            if peak >= total_quantity:
                peak = total_quantity - 1
            line = f"ADD,{side},{order_id},{price},{total_quantity},{peak}"
            order = SimOrder(order_id, side, price, total_quantity, peak, min(total_quantity, peak))
        else:
            quantity = self.rng.randint(self.config.quantity_min, self.config.quantity_max)
            line = f"ADD,{side},{order_id},{price},{quantity}"
            order = SimOrder(order_id, side, price, quantity, None, quantity)

        self._simulate_add(order)
        return line

    def _generate_cancel(self) -> str:
        order_id = self._pick_cancel_order_id()
        self._remove_live_order(order_id)
        return f"CANCEL,{order_id}"

    def _should_cross(self, side: str) -> bool:
        opposite_book = self.sell_book if side == "B" else self.buy_book
        return bool(opposite_book) and self.rng.random() < self.config.crossing_ratio

    def _choose_price(self, side: str, is_crossing: bool) -> int:
        if is_crossing:
            best_opposite = self._best_price("S" if side == "B" else "B")
            if best_opposite is not None:
                if side == "B":
                    return best_opposite + self.rng.choice([0, 1, 2])
                return max(1, best_opposite - self.rng.choice([0, 1, 2]))

        if side == "B":
            return self.mid_price - self.rng.choice(self.config.buy_offsets)
        return self.mid_price + self.rng.choice(self.config.sell_offsets)

    def _best_price(self, side: str) -> Optional[int]:
        book = self.buy_book if side == "B" else self.sell_book
        if not book:
            return None
        prices = book.keys()
        return max(prices) if side == "B" else min(prices)

    def _pick_cancel_order_id(self) -> int:
        dense_prices = [price for price, ids in self.live_by_price.items() if len(ids) >= 3]
        if dense_prices and self.regime_name in {"cancel_heavy", "iceberg_heavy"}:
            target_price = self.rng.choice(dense_prices)
            candidates = sorted(self.live_by_price[target_price])
            return candidates[0] if self.rng.random() < 0.7 else self.rng.choice(candidates)
        return self.rng.choice(self.live_ids)

    def _register_live_order(self, order: SimOrder) -> None:
        self.live_orders[order.order_id] = order
        self.live_ids.append(order.order_id)
        self.live_by_price[order.price].add(order.order_id)
        book = self.buy_book if order.side == "B" else self.sell_book
        book[order.price].append(order)

    def _remove_live_order(self, order_id: int) -> None:
        order = self.live_orders.pop(order_id)
        self.live_by_price[order.price].remove(order_id)
        if not self.live_by_price[order.price]:
            del self.live_by_price[order.price]

        self.live_ids.remove(order_id)

        book = self.buy_book if order.side == "B" else self.sell_book
        queue = book[order.price]
        queue.remove(order)
        if not queue:
            del book[order.price]

    def _simulate_add(self, incoming: SimOrder) -> None:
        opposite_book = self.sell_book if incoming.side == "B" else self.buy_book

        while incoming.total_quantity > 0 and opposite_book:
            best_price = self._best_price("S" if incoming.side == "B" else "B")
            if best_price is None or not self._prices_cross(incoming.side, incoming.price, best_price):
                break

            queue = opposite_book[best_price]
            resting = queue[0]
            match_quantity = min(incoming.total_quantity, resting.visible_quantity)
            incoming.reduce(match_quantity)
            resting.reduce(match_quantity)

            if resting.visible_quantity == 0:
                queue.popleft()
                if resting.total_quantity > 0:
                    resting.replenish()
                    queue.append(resting)
                else:
                    self._remove_fully_matched_order(resting)

            if not queue:
                del opposite_book[best_price]

        if incoming.total_quantity > 0:
            incoming.replenish()
            self._register_live_order(incoming)

    def _remove_fully_matched_order(self, order: SimOrder) -> None:
        if order.order_id not in self.live_orders:
            return
        del self.live_orders[order.order_id]
        self.live_ids.remove(order.order_id)
        self.live_by_price[order.price].remove(order.order_id)
        if not self.live_by_price[order.price]:
            del self.live_by_price[order.price]

    @staticmethod
    def _prices_cross(side: str, incoming_price: int, best_opposite_price: int) -> bool:
        if side == "B":
            return incoming_price >= best_opposite_price
        return incoming_price <= best_opposite_price


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Generate deterministic order book replay workloads.")
    parser.add_argument("--regime", choices=sorted(REGIMES.keys()), required=True)
    parser.add_argument("--commands", type=int, required=True)
    parser.add_argument("--seed", type=int, required=True)
    args = parser.parse_args()

    if args.commands <= 0:
        parser.error("--commands must be greater than 0")

    return args


def main() -> None:
    args = parse_args()
    output_path = WORKLOADS_DIR / f"{args.regime}_{args.commands}_{args.seed}.csv"

    generator = WorkloadGenerator(args.regime, args.commands, args.seed)
    lines = generator.generate()

    WORKLOADS_DIR.mkdir(parents=True, exist_ok=True)
    output_path.write_text("\n".join(lines) + "\n", encoding="utf-8")

    print(output_path)


if __name__ == "__main__":
    main()
