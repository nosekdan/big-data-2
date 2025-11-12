#!/bin/bash

# Configuration
MAX_SIZE=512  # Maximum matrix size to benchmark
MATRIX_SIZE_LIMIT=10  # Generate matrices up to 2^(SIZE_LIMIT-1)

echo "==============================="
echo "Matrix Multiplication Benchmark"
echo "==============================="
echo ""


# Step 1: Generate matrices
echo "Step 1: Generating matrices..."
echo "-------------------------------------"
for i in $(seq 1 $((MATRIX_SIZE_LIMIT - 1))); do
    size=$((2 ** i))
    echo "Generating matrices of size ${size}x${size}..."
    python3 generateMatrix.py $size $MATRIX_AMOUNT
done
echo ""

# Step 2: Compile C++ program
echo "Step 2: Compiling C++ program..."
echo "-------------------------------------"
g++ -std=c++17 -O2 matrix.cpp -o ../exec/matrix_cpp
if [ $? -ne 0 ]; then
    echo "Error: C++ compilation failed"
    exit 1
fi
echo "C++ compilation successful"
echo ""

# Step 3: Compile Java program
echo "Step 3: Compiling Java program..."
echo "-------------------------------------"
javac -d ../exec matrix.java
if [ $? -ne 0 ]; then
    echo "Error: Java compilation failed"
    exit 1
fi
echo "Java compilation successful"
echo ""

# Step 4: Run C++ benchmark
echo "Step 4: Running C++ benchmark..."
echo "-------------------------------------"
../exec/matrix_cpp $MAX_SIZE
if [ $? -ne 0 ]; then
    echo "Error: C++ benchmark failed"
    exit 1
fi
echo ""

# Step 5: Run Java benchmark
echo "Step 5: Running Java benchmark..."
echo "-------------------------------------"
java -cp ../exec matrix $MAX_SIZE
if [ $? -ne 0 ]; then
    echo "Error: Java benchmark failed"
    exit 1
fi
echo ""

# Step 6: Run Python benchmark
echo "Step 6: Running Python benchmark..."
echo "-------------------------------------"
python3 matrix.py $MAX_SIZE
if [ $? -ne 0 ]; then
    echo "Error: Python benchmark failed"
    exit 1
fi
echo ""

# Step 7: Generate graphs
echo "Step 7: Generating graphs..."
echo "-------------------------------------"
python3 generateGraphs.py
if [ $? -ne 0 ]; then
    echo "Error: Graph generation failed"
    exit 1
fi
echo ""

echo "====================================="
echo "All benchmarks completed successfully!"
echo "====================================="
echo "Results:"
echo "  - Benchmark data: ../benchmarks/"
echo "  - Result matrices: ../results/"
echo "  - Graphs: ../graphs/"