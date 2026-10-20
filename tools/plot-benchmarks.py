#!/usr/bin/env python3
"""Render the retained benchmark CSV pairs. Requires Python 3 and matplotlib."""
import csv
from pathlib import Path
from statistics import mean

import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.patches import Patch
from matplotlib.ticker import MaxNLocator

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'docs/benchmarks/charts'
CASES = [('Terrain · 16 chunks', 'terrain-rd16'),
         ('Terrain · 10 chunks', 'terrain-rd10'),
         ('Item-frame display lists', 'item-frames')]
OFF, ON, INK, MUTED = '#586575', '#79b8ff', '#e6edf3', '#9da7b3'
BACKGROUND = '#0d1117'


def load(case, mode):
    with (ROOT / 'docs/benchmarks' / case / (mode + '.csv')).open() as f:
        frames = sorted(int(row['frame_ns']) / 1e6 for row in csv.DictReader(f))
    assert frames and frames[0] > 0, 'Expected positive recorded frame times'
    average = mean(frames)
    return average, mean(frames[int(len(frames) * .99):]), 1000 / average


DATA = [(label, load(case, 'off'), load(case, 'on')) for label, case in CASES]
plt.rcParams.update({'font.family': 'DejaVu Sans', 'font.size': 11,
                     'text.color': INK, 'axes.labelcolor': MUTED,
                     'xtick.color': MUTED, 'ytick.color': INK,
                     'svg.fonttype': 'none'})


def figure(title, subtitle, panels):
    fig, axes = plt.subplots(1, panels, figsize=(14, 5.5), squeeze=False)
    fig.patch.set_facecolor(BACKGROUND)
    fig.text(.04, .90, title, size=22, weight='bold')
    fig.text(.04, .83, subtitle, color=MUTED, size=11)
    fig.legend(handles=[Patch(color=OFF, label='Feature OFF'), Patch(color=ON, label='Feature ON')],
               loc='upper right', bbox_to_anchor=(.97, .94), frameon=False, ncol=2)
    fig.text(.04, .055, 'Single-feature OFF / ON · B6 flight + B1 item frames · October 2026',
             color=MUTED, size=9)
    fig.subplots_adjust(left=.205, right=.96, top=.67, bottom=.23, wspace=.22)
    return fig, axes[0]


def bars(ax, metric, title, unit, increase=False):
    ax.set_facecolor(BACKGROUND)
    maximum = max(values[metric] for _, off, on in DATA for values in (off, on))
    for y, (_, off, on) in enumerate(DATA):
        for offset, values, color in [(-.17, off, OFF), (.17, on, ON)]:
            value = values[metric]
            ax.barh(y + offset, value, height=.28, color=color, zorder=3)
            text = f'{value:,.1f}' if increase else f'{value:.2f}'
            ax.text(value + maximum * .025, y + offset, text, va='center', fontsize=10)
        delta = (on[metric] / off[metric] - 1) * 100 if increase else (1 - on[metric] / off[metric]) * 100
        ax.text(maximum * 1.46, y, f'+{delta:.1f}%' if increase else f'−{delta:.1f}%',
                va='center', ha='right', color=ON, weight='bold', fontsize=12)
    ax.set_title(title, loc='left', fontsize=13, weight='bold', pad=18)
    ax.set_yticks(range(len(DATA)), [label for label, _, _ in DATA])
    ax.set_ylim(2.6, -.6)
    ax.set_xlim(0, maximum * 1.5)
    # Leave annotation space to the right of the numeric axis.
    ticks = MaxNLocator(nbins=4).tick_values(0, maximum)
    ax.set_xticks(ticks[ticks <= maximum * 1.1])
    ax.set_xlabel(unit, labelpad=10)
    ax.grid(axis='x', color=MUTED, alpha=.16, zorder=0)
    ax.tick_params(axis='both', length=0, pad=9)
    for spine in ax.spines.values():
        spine.set_visible(False)


def save(fig, name):
    OUT.mkdir(parents=True, exist_ok=True)
    for extension in ('png', 'svg'):
        path = OUT / (name + '.' + extension)
        fig.savefig(path, dpi=180, facecolor=fig.get_facecolor())
        if extension == 'svg':
            path.write_text('\n'.join(line.rstrip() for line in path.read_text().splitlines()) + '\n')
    plt.close(fig)


fig, axes = figure('LiteOptimizer · frame time', 'Milliseconds · lower is better', 2)
bars(axes[0], 0, 'Average', 'Milliseconds')
bars(axes[1], 1, 'Slowest 1% mean', 'Milliseconds')
axes[1].tick_params(axis='y', labelleft=False)
save(fig, 'frame-time-comparison-dark')

fig, axes = figure('LiteOptimizer · FPS', 'Average FPS · higher is better', 1)
bars(axes[0], 2, '', 'Frames per second', increase=True)
save(fig, 'fps-comparison-dark')
print('Charts written to', OUT)

# Current full-mod run: bars show per-mode medians; whiskers show run ranges.
import json

CURRENT = ROOT / 'docs/benchmarks/2026-10-20/summary.json'
current = json.loads(CURRENT.read_text())
WORKLOADS = [('B1 storage · RD10', 'B1-full'), ('B6 flight · RD16', 'B6-full')]


def draw_current(metric, title, unit, filename):
    fig, ax = plt.subplots(figsize=(10.8, 4.7))
    fig.patch.set_facecolor(BACKGROUND)
    ax.set_facecolor(BACKGROUND)
    ax.set_title(title, loc='left', color=INK, size=20, weight='bold', pad=22)
    colors = {'off': OFF, 'on': ON}
    for y, (label, key) in enumerate(WORKLOADS):
        group = current[key]
        for offset, mode in [(-.16, 'off'), (.16, 'on')]:
            stats = group[mode][metric]
            median = stats['median']
            left, right = median - stats['min'], stats['max'] - median
            ax.barh(y + offset, median, height=.27, color=colors[mode], zorder=3)
            ax.errorbar(median, y + offset, xerr=[[left], [right]], fmt='none',
                        ecolor=INK, elinewidth=1.2, capsize=4, zorder=4)
            ax.text(stats['max'] * 1.04, y + offset, f'{median:,.2f}',
                    va='center', color=INK, size=10)
        changes = group['per_pair_change_percent'][metric]
        ordered = sorted(changes)
        effect = ordered[len(ordered) // 2]
        sign = '−' if effect < 0 else '+'
        ax.text(.99, y, f'{sign}{abs(effect):.1f}%', transform=ax.get_yaxis_transform(),
                ha='right', va='center', color=ON, size=11, weight='bold')

    ax.set_yticks(range(len(WORKLOADS)), [label for label, _ in WORKLOADS])
    ax.invert_yaxis()
    maximum = max(current[key][mode][metric]['max']
                  for _, key in WORKLOADS for mode in ('off', 'on'))
    ax.set_xlim(0, maximum * 1.24)
    ax.set_xlabel(unit, color=MUTED, labelpad=10)
    ax.grid(axis='x', color=MUTED, alpha=.16, zorder=0)
    ax.tick_params(axis='both', colors=MUTED, length=0, pad=9)
    for spine in ax.spines.values():
        spine.set_visible(False)
    fig.legend(handles=[Patch(color=OFF, label='Optimizations OFF'),
                        Patch(color=ON, label='Defaults ON')],
               loc='upper right', bbox_to_anchor=(.96, .97),
               frameon=False, ncol=2, labelcolor=INK)
    fig.text(.04, .025, 'Bars: median of 3 runs · whiskers: run min–max · right labels: median paired change',
             color=MUTED, size=9)
    fig.subplots_adjust(left=.24, right=.96, top=.78, bottom=.20)
    save(fig, filename)


draw_current('frame_mean_ms', 'LiteOptimizer · average frame time',
             'Milliseconds · lower is better', 'whole-mod-frame-time-2026-10-20-dark')
draw_current('slowest_1pct_mean_ms', 'LiteOptimizer · slowest 1% mean frame time',
             'Milliseconds · lower is better', 'whole-mod-slowest-1pct-2026-10-20-dark')
draw_current('fps', 'LiteOptimizer · average FPS',
             'Frames per second · higher is better', 'whole-mod-fps-2026-10-20-dark')
draw_current('mspt_mean', 'LiteOptimizer · sampled integrated-server MSPT',
             'Milliseconds per tick · lower is better', 'whole-mod-mspt-2026-10-20-dark')
