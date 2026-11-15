#!/usr/bin/env python3
"""
Scheduling Algorithm Comparison Plotting Script for Procura.
Reads benchmark results exported by Java and generates comparison subplots:
1. Average Waiting Time
2. Average Turnaround Time
3. Throughput
"""

import sys
import os
import pandas as pd
import matplotlib.pyplot as plt

def plot_scheduling_comparison(csv_path="data/scheduling_comparison.csv", output_path="graphs/scheduling_comparison.png"):
    if not os.path.exists(csv_path):
        print(f"[ERROR] Data file not found: {csv_path}", file=sys.stderr)
        return False

    try:
        df = pd.read_csv(csv_path)
    except Exception as e:
        print(f"[ERROR] Failed to read CSV {csv_path}: {e}", file=sys.stderr)
        return False

    if df.empty or 'Algorithm' not in df.columns:
        print(f"[ERROR] CSV {csv_path} contains invalid or missing Algorithm data.", file=sys.stderr)
        return False

    os.makedirs(os.path.dirname(os.path.abspath(output_path)), exist_ok=True)

    plt.style.use('dark_background')
    fig, (ax1, ax2, ax3) = plt.subplots(1, 3, figsize=(14, 5), dpi=150)
    fig.patch.set_facecolor('#1E1E1E')

    algorithms = df['Algorithm']
    colors = ['#4682E6', '#FFA726', '#4CAF50'][:len(algorithms)]

    # Subplot 1: Average Waiting Time
    ax1.set_facecolor('#282828')
    bars1 = ax1.bar(algorithms, df['Avg_Waiting_Time'], color=colors, width=0.55, edgecolor='#3C3C3C')
    ax1.set_title('Avg Waiting Time (lower is better)', fontsize=11, fontweight='bold', color='#DCDCDC')
    ax1.set_ylabel('Time Units', fontsize=10, color='#A0A0A0')
    ax1.grid(axis='y', linestyle='--', alpha=0.3, color='#808080')
    ax1.tick_params(colors='#A0A0A0')
    for bar in bars1:
        yval = bar.get_height()
        ax1.text(bar.get_x() + bar.get_width()/2.0, yval + 0.05, f"{yval:.2f}", ha='center', va='bottom', color='#FFFFFF', fontsize=9, fontweight='bold')

    # Subplot 2: Average Turnaround Time
    ax2.set_facecolor('#282828')
    bars2 = ax2.bar(algorithms, df['Avg_Turnaround_Time'], color=colors, width=0.55, edgecolor='#3C3C3C')
    ax2.set_title('Avg Turnaround Time (lower is better)', fontsize=11, fontweight='bold', color='#DCDCDC')
    ax2.set_ylabel('Time Units', fontsize=10, color='#A0A0A0')
    ax2.grid(axis='y', linestyle='--', alpha=0.3, color='#808080')
    ax2.tick_params(colors='#A0A0A0')
    for bar in bars2:
        yval = bar.get_height()
        ax2.text(bar.get_x() + bar.get_width()/2.0, yval + 0.05, f"{yval:.2f}", ha='center', va='bottom', color='#FFFFFF', fontsize=9, fontweight='bold')

    # Subplot 3: Throughput
    ax3.set_facecolor('#282828')
    bars3 = ax3.bar(algorithms, df['Throughput'], color=colors, width=0.55, edgecolor='#3C3C3C')
    ax3.set_title('Throughput (jobs / unit time)', fontsize=11, fontweight='bold', color='#DCDCDC')
    ax3.set_ylabel('Jobs / Unit Time', fontsize=10, color='#A0A0A0')
    ax3.grid(axis='y', linestyle='--', alpha=0.3, color='#808080')
    ax3.tick_params(colors='#A0A0A0')
    for bar in bars3:
        yval = bar.get_height()
        ax3.text(bar.get_x() + bar.get_width()/2.0, yval + 0.005, f"{yval:.3f}", ha='center', va='bottom', color='#FFFFFF', fontsize=9, fontweight='bold')

    for ax in (ax1, ax2, ax3):
        for spine in ax.spines.values():
            spine.set_color('#3C3C3C')

    plt.suptitle('Procura — CPU Scheduling Algorithm Performance Comparison', fontsize=13, fontweight='bold', color='#DCDCDC', y=1.02)
    plt.tight_layout()

    fig.savefig(output_path, facecolor=fig.get_facecolor(), edgecolor='none', bbox_inches='tight')
    plt.close(fig)
    print(f"[SUCCESS] Scheduling comparison plot saved to: {output_path}")
    return True

if __name__ == '__main__':
    csv_file = sys.argv[1] if len(sys.argv) > 1 else "data/scheduling_comparison.csv"
    out_file = sys.argv[2] if len(sys.argv) > 2 else "graphs/scheduling_comparison.png"
    success = plot_scheduling_comparison(csv_file, out_file)
    sys.exit(0 if success else 1)
