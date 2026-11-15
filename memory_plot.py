#!/usr/bin/env python3
"""
Memory Utilization Plotting Script for Procura.
Reads time-series memory usage exported by Java and generates a professional dark-themed line chart.
"""

import sys
import os
import pandas as pd
import matplotlib.pyplot as plt

def plot_memory_utilization(csv_path="data/memory_history.csv", output_path="graphs/memory_utilization.png"):
    if not os.path.exists(csv_path):
        print(f"[ERROR] Data file not found: {csv_path}", file=sys.stderr)
        return False

    try:
        df = pd.read_csv(csv_path)
    except Exception as e:
        print(f"[ERROR] Failed to read CSV {csv_path}: {e}", file=sys.stderr)
        return False

    if df.empty or 'Memory_Usage' not in df.columns:
        print(f"[ERROR] CSV {csv_path} contains invalid or empty columns.", file=sys.stderr)
        return False

    os.makedirs(os.path.dirname(os.path.abspath(output_path)), exist_ok=True)

    plt.style.use('dark_background')
    fig, ax = plt.subplots(figsize=(10, 5), dpi=150)
    fig.patch.set_facecolor('#1E1E1E')
    ax.set_facecolor('#282828')

    x_vals = range(len(df)) if 'Timestamp' not in df.columns else df['Timestamp']
    y_vals = df['Memory_Usage']

    ax.plot(x_vals, y_vals, color='#4CAF50', linewidth=2, marker='s', markersize=4, label='RAM Usage (%)')
    ax.fill_between(x_vals, y_vals, color='#4CAF50', alpha=0.25)

    ax.set_title('Real-Time Memory Utilization History', fontsize=14, fontweight='bold', color='#DCDCDC', pad=15)
    ax.set_xlabel('Sample Timeline (Seconds / Intervals)', fontsize=11, color='#DCDCDC')
    ax.set_ylabel('Memory Usage (%)', fontsize=11, color='#DCDCDC')
    ax.set_ylim(0, 105)

    ax.grid(True, linestyle='--', alpha=0.3, color='#808080')
    ax.tick_params(colors='#A0A0A0')
    for spine in ax.spines.values():
        spine.set_color('#3C3C3C')

    ax.legend(loc='upper right', facecolor='#323232', edgecolor='#3C3C3C', labelcolor='#DCDCDC')
    plt.tight_layout()

    fig.savefig(output_path, facecolor=fig.get_facecolor(), edgecolor='none')
    plt.close(fig)
    print(f"[SUCCESS] Memory utilization plot saved to: {output_path}")
    return True

if __name__ == '__main__':
    csv_file = sys.argv[1] if len(sys.argv) > 1 else "data/memory_history.csv"
    out_file = sys.argv[2] if len(sys.argv) > 2 else "graphs/memory_utilization.png"
    success = plot_memory_utilization(csv_file, out_file)
    sys.exit(0 if success else 1)
