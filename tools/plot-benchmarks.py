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
OFF, ON, INK, MUTED = '#94a3b8', '#0d9488', '#142b42', '#53657a'


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
    fig, axes = plt.subplots(1, panels, figsize=(14, 6.5), squeeze=False)
    fig.patch.set_facecolor('#f8fafc')
    fig.text(.04, .94, 'LITEOPTIMIZER  /  MINECRAFT 1.12.2', color=ON, size=10, weight='bold')
    fig.text(.04, .865, title, size=25, weight='bold')
    fig.text(.04, .81, subtitle, color=MUTED, size=11)
    fig.legend(handles=[Patch(color=OFF, label='Feature OFF'), Patch(color=ON, label='Feature ON')],
               loc='upper right', bbox_to_anchor=(.97, .975), frameon=False, ncol=2)
    fig.text(.04, .075, 'Individual feature comparisons · other settings held constant · gains do not add together',
             color=MUTED, size=10)
    fig.text(.04, .035, 'Source: retained B6 terrain and B1 item-frame CSV pairs, Oct 13–15, 2026. Historical candidate runs; hardware inventory incomplete.',
             color=MUTED, size=8.5)
    fig.subplots_adjust(left=.205, right=.94, top=.68, bottom=.23, wspace=.8)
    return fig, axes[0]


def bars(ax, metric, title, unit, increase=False):
    ax.set_facecolor('#f8fafc')
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
    ax.grid(axis='x', alpha=.15, zorder=0)
    ax.tick_params(axis='both', length=0, pad=9)
    for spine in ax.spines.values():
        spine.set_visible(False)


def save(fig, name):
    OUT.mkdir(parents=True, exist_ok=True)
    for extension in ('png', 'svg'):
        fig.savefig(OUT / (name + '.' + extension), dpi=180, facecolor=fig.get_facecolor())
    plt.close(fig)


fig, axes = figure('Less time per frame', 'Lower is better · milliseconds · percentages show time reductions', 2)
bars(axes[0], 0, 'Average frame time', 'Milliseconds')
bars(axes[1], 1, 'Slowest 1% mean frame time', 'Milliseconds')
save(fig, 'frame-time-comparison')

fig, axes = figure('More frames per second', 'Higher is better · average FPS derived from mean frame time · percentages show FPS increases', 1)
bars(axes[0], 2, 'Average FPS', 'Frames per second', increase=True)
save(fig, 'fps-comparison')
print('Charts written to', OUT)
