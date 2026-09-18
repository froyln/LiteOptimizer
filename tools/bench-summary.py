#!/usr/bin/env python3
"""Summarize one or two BenchRecorder CSVs: frame time avg / 1% low / max, MSPT avg / max.

    tools/bench-summary.py bench/a.csv [bench/b.csv]     (b relative to a → deltas)
"""
import csv, statistics, sys

def load(path):
    frames, mspt = [], []
    for row in csv.DictReader(open(path)):
        frames.append(int(row["frame_ns"]) / 1e6)
        if row["mspt_ms"]:
            mspt.append(float(row["mspt_ms"]))
    frames.sort()
    worst = frames[int(len(frames) * 0.99):] or frames
    return {
        "frames": len(frames),
        "frame_avg_ms": statistics.mean(frames),
        "frame_1pct_low_ms": statistics.mean(worst),
        "frame_max_ms": frames[-1],
        "fps": 1000.0 / statistics.mean(frames),
        "mspt_avg": statistics.mean(mspt) if mspt else float("nan"),
        "mspt_max": max(mspt) if mspt else float("nan"),
    }

def show(name, s):
    print(f"{name}: {s['frames']} frames, {s['fps']:.1f} fps, avg {s['frame_avg_ms']:.2f} ms, "
          f"1% low {s['frame_1pct_low_ms']:.2f} ms, max {s['frame_max_ms']:.1f} ms, "
          f"mspt avg {s['mspt_avg']:.2f} max {s['mspt_max']:.2f}")

a = load(sys.argv[1]); show(sys.argv[1], a)
if len(sys.argv) > 2:
    b = load(sys.argv[2]); show(sys.argv[2], b)
    for k in ("frame_avg_ms", "frame_1pct_low_ms", "mspt_avg"):
        if a[k] == a[k] and a[k]:
            print(f"  {k}: {100.0 * (b[k] - a[k]) / a[k]:+.1f}%")
