import pandas as pd
import matplotlib.pyplot as plt
import os
from datetime import datetime

# Create output directory with current date
current_date = datetime.now().strftime('%Y%m%d%H%M')
output_dir = f'../graphs/{current_date}'
os.makedirs(output_dir, exist_ok=True)

# Read the three data files
file1 = pd.read_csv('../benchmarks/cpp.txt', sep='\t')
file2 = pd.read_csv('../benchmarks/java.txt', sep='\t')
file3 = pd.read_csv('../benchmarks/python.txt', sep='\t')

# Plot 1: Time vs Size
fig1, ax1 = plt.subplots(figsize=(10, 6))
ax1.plot(file1['Size'], file1['Time(s)'], marker='o', label='C++')
ax1.plot(file2['Size'], file2['Time(s)'], marker='s', label='Java')
ax1.plot(file3['Size'], file3['Time(s)'], marker='^', label='Python')
ax1.set_xlabel('n')
ax1.set_ylabel('Time (s)')
ax1.set_title('Execution time')
ax1.set_xscale('log', base=2)
ax1.set_yscale('log')
ax1.legend()
ax1.grid(True, alpha=0.3)
plt.tight_layout()
plt.savefig(f'{output_dir}/execution_time.png', dpi=300, bbox_inches='tight')
plt.close()

# Plot 2: Memory vs Size
fig2, ax2 = plt.subplots(figsize=(10, 6))
ax2.plot(file1['Size'], file1['Memory(MB)'], marker='o', label='C++')
ax2.plot(file2['Size'], file2['Memory(MB)'], marker='s', label='Java')
ax2.plot(file3['Size'], file3['Memory(MB)'], marker='^', label='Python')
ax2.set_xlabel('n')
ax2.set_ylabel('Memory (MB)')
ax2.set_title('Memory usage')
ax2.set_xscale('log', base=2)
ax2.legend()
ax2.grid(True, alpha=0.3)
plt.tight_layout()
plt.savefig(f'{output_dir}/memory_usage.png', dpi=300, bbox_inches='tight')
plt.close()

# Plot 3: CPU vs Size
fig3, ax3 = plt.subplots(figsize=(10, 6))
ax3.plot(file1['Size'], file1['CPU(%)'], marker='o', label='C++')
ax3.plot(file2['Size'], file2['CPU(%)'], marker='s', label='Java')
ax3.plot(file3['Size'], file3['CPU(%)'], marker='^', label='Python')
ax3.set_xlabel('n')
ax3.set_ylabel('CPU (%)')
ax3.set_title('CPU usage')
ax3.set_xscale('log', base=2)
ax3.legend()
ax3.grid(True, alpha=0.3)
plt.tight_layout()
plt.savefig(f'{output_dir}/cpu_usage.png', dpi=300, bbox_inches='tight')
plt.close()

print(f"Graphs saved to {output_dir}/:")
print("  - execution_time.png")
print("  - memory_usage.png")
print("  - cpu_usage.png")