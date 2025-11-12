import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import pandas as pd
import os
from datetime import datetime
from pathlib import Path

# Create output directory with current date
current_date = datetime.now().strftime('%Y%m%d%H%M')
output_dir = f'graphs/{current_date}'
os.makedirs(output_dir, exist_ok=True)

# Algorithm types and matrix types
algorithms = ["basic", "loop", "cache", "strassen"]
matrix_types = ["normal", "sparse"]

# Read all benchmark files into a dictionary
data = {}
for matrix_type in matrix_types:
    data[matrix_type] = {}
    for algorithm in algorithms:
        filepath = f'benchmarks/{matrix_type}/{algorithm}.txt'
        try:
            data[matrix_type][algorithm] = pd.read_csv(filepath, sep='\t')
        except Exception as e:
            print(f"Error reading {filepath}: {e}")

# ============================================================
# Plot 1: Time Comparison - All Algorithms (Normal Matrices)
# ============================================================
fig1, ax1 = plt.subplots(figsize=(12, 6))
for algorithm in algorithms:
    df = data["normal"][algorithm]
    ax1.plot(df['Size'], df['Time(s)'], marker='o', linewidth=2, label=algorithm.capitalize(), markersize=8)

ax1.set_xlabel('Matrix Size', fontsize=12, fontweight='bold')
ax1.set_ylabel('Time (seconds)', fontsize=12, fontweight='bold')
ax1.set_title('Execution Time Comparison - All Algorithms (Normal Matrices)', fontsize=14, fontweight='bold')
ax1.set_xscale('log', base=2)
ax1.set_yscale('log')
ax1.legend(fontsize=11)
ax1.grid(True, alpha=0.3)
plt.tight_layout()
plt.savefig(f'{output_dir}/01_time_comparison_normal.png', dpi=300, bbox_inches='tight')
plt.close()

# ============================================================
# Plot 2: Time Comparison - All Algorithms (Sparse Matrices)
# ============================================================
fig2, ax2 = plt.subplots(figsize=(12, 6))
for algorithm in algorithms:
    df = data["sparse"][algorithm]
    ax2.plot(df['Size'], df['Time(s)'], marker='s', linewidth=2, label=algorithm.capitalize(), markersize=8)

ax2.set_xlabel('Matrix Size', fontsize=12, fontweight='bold')
ax2.set_ylabel('Time (seconds)', fontsize=12, fontweight='bold')
ax2.set_title('Execution Time Comparison - All Algorithms (Sparse Matrices)', fontsize=14, fontweight='bold')
ax2.set_xscale('log', base=2)
ax2.set_yscale('log')
ax2.legend(fontsize=11)
ax2.grid(True, alpha=0.3)
plt.tight_layout()
plt.savefig(f'{output_dir}/02_time_comparison_sparse.png', dpi=300, bbox_inches='tight')
plt.close()

# ============================================================
# Plot 3: Normal vs Sparse - Time Comparison (All Algorithms)
# ============================================================
fig3, axes = plt.subplots(2, 2, figsize=(14, 10))
axes = axes.flatten()

for idx, algorithm in enumerate(algorithms):
    ax = axes[idx]
    
    df_normal = data["normal"][algorithm]
    df_sparse = data["sparse"][algorithm]
    
    x = range(len(df_normal))
    width = 0.35
    
    ax.bar([i - width/2 for i in x], df_normal['Time(s)'], width, label='Normal', alpha=0.8, color='skyblue')
    ax.bar([i + width/2 for i in x], df_sparse['Time(s)'], width, label='Sparse', alpha=0.8, color='coral')
    
    ax.set_xlabel('Matrix Size', fontsize=11, fontweight='bold')
    ax.set_ylabel('Time (seconds)', fontsize=11, fontweight='bold')
    ax.set_title(f'{algorithm.capitalize()} - Normal vs Sparse', fontsize=12, fontweight='bold')
    ax.set_xticks(x)
    ax.set_xticklabels(df_normal['Size'], rotation=45)
    ax.legend(fontsize=10)
    ax.grid(True, alpha=0.3, axis='y')

plt.tight_layout()
plt.savefig(f'{output_dir}/03_normal_vs_sparse_all_algorithms.png', dpi=300, bbox_inches='tight')
plt.close()

# ============================================================
# Plot 4: Average Time Comparison (Normal vs Sparse)
# ============================================================
fig4, ax4 = plt.subplots(figsize=(12, 6))

x = range(len(algorithms))
width = 0.35

avg_times_normal = []
avg_times_sparse = []

for algorithm in algorithms:
    avg_normal = data["normal"][algorithm]['Time(s)'].mean()
    avg_sparse = data["sparse"][algorithm]['Time(s)'].mean()
    avg_times_normal.append(avg_normal)
    avg_times_sparse.append(avg_sparse)

bars1 = ax4.bar([i - width/2 for i in x], avg_times_normal, width, label='Normal Matrices', alpha=0.8, color='steelblue')
bars2 = ax4.bar([i + width/2 for i in x], avg_times_sparse, width, label='Sparse Matrices', alpha=0.8, color='darkorange')

# Add value labels on bars
for bars in [bars1, bars2]:
    for bar in bars:
        height = bar.get_height()
        ax4.text(bar.get_x() + bar.get_width()/2., height,
                f'{height:.4f}s',
                ha='center', va='bottom', fontsize=9)

ax4.set_xlabel('Algorithm', fontsize=12, fontweight='bold')
ax4.set_ylabel('Average Time (seconds)', fontsize=12, fontweight='bold')
ax4.set_title('Average Execution Time - Normal vs Sparse Matrices', fontsize=14, fontweight='bold')
ax4.set_xticks(x)
ax4.set_xticklabels([algo.capitalize() for algo in algorithms])
ax4.legend(fontsize=11)
ax4.grid(True, alpha=0.3, axis='y')

plt.tight_layout()
plt.savefig(f'{output_dir}/04_average_time_comparison.png', dpi=300, bbox_inches='tight')
plt.close()

# ============================================================
# Plot 5: Memory Usage Comparison
# ============================================================
fig5, axes = plt.subplots(1, 2, figsize=(14, 6))

for idx, matrix_type in enumerate(matrix_types):
    ax = axes[idx]
    
    for algorithm in algorithms:
        df = data[matrix_type][algorithm]
        ax.plot(df['Size'], df['Memory(MB)'], marker='o', linewidth=2, label=algorithm.capitalize(), markersize=8)
    
    ax.set_xlabel('Matrix Size', fontsize=12, fontweight='bold')
    ax.set_ylabel('Memory (MB)', fontsize=12, fontweight='bold')
    ax.set_title(f'Memory Usage - {matrix_type.capitalize()} Matrices', fontsize=12, fontweight='bold')
    ax.set_xscale('log', base=2)
    ax.legend(fontsize=10)
    ax.grid(True, alpha=0.3)

plt.tight_layout()
plt.savefig(f'{output_dir}/05_memory_usage_comparison.png', dpi=300, bbox_inches='tight')
plt.close()

# ============================================================
# Plot 6: Speedup Analysis (Relative to Basic Algorithm)
# ============================================================
fig6, axes = plt.subplots(1, 2, figsize=(14, 6))

for idx, matrix_type in enumerate(matrix_types):
    ax = axes[idx]
    
    times_basic = data[matrix_type]["basic"]['Time(s)'].values
    sizes = data[matrix_type]["basic"]['Size'].values
    
    for algorithm in algorithms[1:]:  # Skip basic
        times_algo = data[matrix_type][algorithm]['Time(s)'].values
        speedup = times_basic / times_algo
        ax.plot(sizes, speedup, marker='o', linewidth=2, label=algorithm.capitalize(), markersize=8)
    
    ax.axhline(y=1, color='gray', linestyle='--', linewidth=1, label='Basic (baseline)')
    ax.set_xlabel('Matrix Size', fontsize=12, fontweight='bold')
    ax.set_ylabel('Speedup (vs Basic)', fontsize=12, fontweight='bold')
    ax.set_title(f'Speedup Analysis - {matrix_type.capitalize()} Matrices', fontsize=12, fontweight='bold')
    ax.set_xscale('log', base=2)
    ax.legend(fontsize=10)
    ax.grid(True, alpha=0.3)

plt.tight_layout()
plt.savefig(f'{output_dir}/06_speedup_analysis.png', dpi=300, bbox_inches='tight')
plt.close()

# ============================================================
# Plot 7: CPU Usage Comparison
# ============================================================
fig7, axes = plt.subplots(1, 2, figsize=(14, 6))

for idx, matrix_type in enumerate(matrix_types):
    ax = axes[idx]
    
    for algorithm in algorithms:
        df = data[matrix_type][algorithm]
        ax.plot(df['Size'], df['CPU(%)'], marker='o', linewidth=2, label=algorithm.capitalize(), markersize=8)
    
    ax.set_xlabel('Matrix Size', fontsize=12, fontweight='bold')
    ax.set_ylabel('CPU Usage (%)', fontsize=12, fontweight='bold')
    ax.set_title(f'CPU Usage - {matrix_type.capitalize()} Matrices', fontsize=12, fontweight='bold')
    ax.set_xscale('log', base=2)
    ax.legend(fontsize=10)
    ax.grid(True, alpha=0.3)

plt.tight_layout()
plt.savefig(f'{output_dir}/07_cpu_usage_comparison.png', dpi=300, bbox_inches='tight')
plt.close()

# ============================================================
# Print Summary Statistics
# ============================================================
print("\n" + "="*80)
print("SUMMARY STATISTICS")
print("="*80)

for matrix_type in matrix_types:
    print(f"\n{matrix_type.upper()} MATRICES:")
    print("-" * 80)
    for algorithm in algorithms:
        df = data[matrix_type][algorithm]
        avg_time = df['Time(s)'].mean()
        max_time = df['Time(s)'].max()
        max_memory = df['Memory(MB)'].max()
        print(f"  {algorithm.upper():10s}: Avg Time={avg_time:8.4f}s | Max Time={max_time:8.4f}s | Max Mem={max_memory:8.2f}MB")

print("\n" + "="*80)
print(f"Graphs saved to {output_dir}/:")
print("  - 01_time_comparison_normal.png")
print("  - 02_time_comparison_sparse.png")
print("  - 03_normal_vs_sparse_all_algorithms.png")
print("  - 04_average_time_comparison.png")
print("  - 05_memory_usage_comparison.png")
print("  - 06_speedup_analysis.png")
print("  - 07_cpu_usage_comparison.png")
print("="*80)