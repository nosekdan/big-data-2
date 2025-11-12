package cz.cvut.fit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import com.sun.management.OperatingSystemMXBean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import cz.cvut.fit.Algorithms.Matrix;

public class Main {

    private static final int MAX_SIZE = 2048;
    private static final int MIN_SIZE = 2;
    private static final String[] ALGORITHMS = {"basic", "loop", "cache", "strassen"};
    private static final String[] MATRIX_TYPES = {"normal", "sparse"};

    // Benchmark results holder
    static class BenchmarkResult {
        int size;
        double timeSeconds;
        double memoryMB;
        double cpuPercent;

        BenchmarkResult(int size, double timeSeconds, double memoryMB, double cpuPercent) {
            this.size = size;
            this.timeSeconds = timeSeconds;
            this.memoryMB = memoryMB;
            this.cpuPercent = cpuPercent;
        }

        @Override
        public String toString() {
            return String.format("%d\t%.4f\t%.2f\t%.1f", size, timeSeconds, memoryMB, cpuPercent);
        }
    }

    // Load a matrix from a text file
    public static Matrix loadMatrixFromFile(String fileName) {
        List<List<Integer>> matrixList = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.trim().split("\\s+");
                List<Integer> row = new ArrayList<>();

                for (String value : values) {
                    if (!value.isEmpty()) {
                        row.add(Integer.parseInt(value));
                    }
                }

                if (!row.isEmpty()) {
                    matrixList.add(row);
                }
            }
        } catch (IOException e) {
            System.err.println("Error: Could not open file " + fileName);
            return null;
        }

        if (matrixList.isEmpty()) {
            return null;
        }

        int size = matrixList.size();
        int[][] matrix = new int[size][size];

        for (int i = 0; i < size; i++) {
            List<Integer> row = matrixList.get(i);
            for (int j = 0; j < size; j++) {
                matrix[i][j] = row.get(j);
            }
        }

        return new Matrix(matrix);
    }

    // Benchmark a single multiplication
    public static BenchmarkResult benchmark(Matrix A, Matrix B, int size, String algorithm) {
        OperatingSystemMXBean osBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

        // Force garbage collection before measurement
        System.gc();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Get initial metrics
        long cpuBefore = osBean.getProcessCpuTime(); // nanoseconds
        MemoryUsage memBefore = memoryBean.getHeapMemoryUsage();
        long timeBefore = System.nanoTime();

        // Get process CPU time at start
        double processCpuLoadBefore = osBean.getProcessCpuLoad();

        // Execute multiplication
        Algorithms.Matrix result = null;
        switch (algorithm) {
            case "basic":
                result = Algorithms.matrixMultiplicationBasic(A, B, size);
                break;
            case "loop":
                result = Algorithms.matrixMultiplicationLoopUnroll(A, B, size);
                break;
            case "cache":
                result = Algorithms.matrixMultiplicationCache(A, B, size);
                break;
            case "strassen":
                result = Algorithms.matrixMultiplicationStrassen(A, B, size);
                break;
        }

        // Get final metrics
        long timeAfter = System.nanoTime();
        MemoryUsage memAfter = memoryBean.getHeapMemoryUsage();
        long cpuAfter = osBean.getProcessCpuTime(); // nanoseconds

        // Calculate metrics
        double timeSeconds = (timeAfter - timeBefore) / 1_000_000_000.0;
        double timeMillis = (timeAfter - timeBefore) / 1_000_000.0;

        // Memory: heap memory used during execution
        long memUsedBefore = memBefore.getUsed();
        long memUsedAfter = memAfter.getUsed();
        double memoryMB = Math.abs((memUsedAfter - memUsedBefore) / (1024.0 * 1024.0));

        // CPU: process CPU time used / wall clock time
        long cpuTimeNanos = cpuAfter - cpuBefore;
        long wallClockNanos = timeAfter - timeBefore;

        // CPU percentage based on number of available processors
        int numProcessors = Runtime.getRuntime().availableProcessors();
        double cpuPercent = (cpuTimeNanos / (double) wallClockNanos) * 100.0;

        // Clamp CPU percent to reasonable range [0, 100*numProcessors]
        cpuPercent = Math.min(cpuPercent, 100.0 * numProcessors);

        return new BenchmarkResult(size, timeSeconds, memoryMB, cpuPercent);
    }

    // Save result matrix to file
    public static void saveMatrixResult(Matrix result, String filePath) {
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(Paths.get(filePath)))) {
            for (int i = 0; i < result.size; i++) {
                StringBuilder line = new StringBuilder();
                for (int j = 0; j < result.size; j++) {
                    if (j > 0) {
                        line.append(" ");
                    }
                    line.append(result.data[i][j]);
                }
                writer.println(line.toString());
            }
        } catch (IOException e) {
            System.err.println("Error writing result to file: " + filePath);
        }
    }

    // Save benchmark results to file
    public static void saveBenchmarkResults(List<BenchmarkResult> results, String filePath) {
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(Paths.get(filePath)))) {
            writer.println("Size\tTime(s)\tMemory(MB)\tCPU(%)");
            for (BenchmarkResult result : results) {
                writer.println(result.toString());
            }
        } catch (IOException e) {
            System.err.println("Error writing benchmark results to file: " + filePath);
        }
    }

    // Compare two matrices for equality
    public static boolean matricesAreEqual(Matrix m1, Matrix m2) {
        if (m1 == null || m2 == null) {
            return m1 == m2;
        }

        if (m1.size != m2.size) {
            return false;
        }

        for (int i = 0; i < m1.size; i++) {
            for (int j = 0; j < m1.size; j++) {
                if (m1.data[i][j] != m2.data[i][j]) {
                    return false;
                }
            }
        }

        return true;
    }

    // Verify all results by comparing files from results directory
    public static void verifyResults(Path resultsDir, String matrixType, int size) {
        System.out.println("\n--- Verifying results for " + matrixType + " matrix size " + size + " ---");

        Map<String, Matrix> results = new HashMap<>();

        // Load result for each algorithm from the results directory
        for (String algorithm : ALGORITHMS) {
            Path resultFile = resultsDir.resolve(matrixType).resolve(algorithm).resolve(size + ".txt");

            if (!Files.exists(resultFile)) {
                System.err.println("✗ " + algorithm + ": Result file not found at " + resultFile);
                continue;
            }

            Matrix result = loadMatrixFromFile(resultFile.toString());
            if (result == null) {
                System.err.println("✗ " + algorithm + ": Failed to load result from " + resultFile);
                continue;
            }

            results.put(algorithm, result);
        }

        // Compare all results against the basic algorithm (reference)
        Matrix basicResult = results.get("basic");
        if (basicResult == null) {
            System.err.println("✗ ERROR: Could not load basic algorithm result!");
            return;
        }

        boolean allMatch = true;
        for (String algorithm : ALGORITHMS) {
            if (algorithm.equals("basic")) {
                System.out.println("✓ " + algorithm + ": Reference");
            } else {
                Matrix otherResult = results.get(algorithm);
                if (otherResult == null) {
                    System.err.println("✗ " + algorithm + ": Result not loaded");
                    allMatch = false;
                } else if (matricesAreEqual(basicResult, otherResult)) {
                    System.out.println("✓ " + algorithm + ": Matches basic");
                } else {
                    System.out.println("✗ " + algorithm + ": MISMATCH! Does not match basic algorithm!");
                    allMatch = false;
                }
            }
        }

        if (allMatch) {
            System.out.println("✓ All algorithms produce identical results!");
        } else {
            System.out.println("✗ Some algorithms produced different results!");
        }
    }

    public static void main(String[] args) {
        Path projectRoot = Paths.get("").toAbsolutePath();
        Path inputDir = projectRoot.resolve("input");
        Path resultsDir = projectRoot.resolve("results");
        Path benchmarksDir = projectRoot.resolve("benchmarks");

        System.out.println("Starting matrix multiplication benchmarks...");
        System.out.println("Project root: " + projectRoot);

        // Create directories if they don't exist
        try {
            Files.createDirectories(resultsDir);
            Files.createDirectories(benchmarksDir);
        } catch (IOException e) {
            System.err.println("Error creating directories: " + e.getMessage());
            return;
        }

        // Iterate over all combinations
        for (String matrixType : MATRIX_TYPES) {
            for (String algorithm : ALGORITHMS) {
                System.out.println("\n========================================");
                System.out.println("Processing: " + matrixType + " - " + algorithm);
                System.out.println("========================================");

                List<BenchmarkResult> benchmarkResults = new ArrayList<>();

                for (int size = MIN_SIZE; size <= MAX_SIZE; size *= 2) {
                    try {
                        // Load matrices
                        Path matrixDir = inputDir.resolve(matrixType).resolve(String.valueOf(size));
                        Path matrix1Path = matrixDir.resolve("1.txt");
                        Path matrix2Path = matrixDir.resolve("2.txt");

                        if (!Files.exists(matrix1Path) || !Files.exists(matrix2Path)) {
                            System.err.println("Matrix files not found for size " + size + " (" + matrixType + ")");
                            continue;
                        }

                        Algorithms.Matrix A = loadMatrixFromFile(matrix1Path.toString());
                        Algorithms.Matrix B = loadMatrixFromFile(matrix2Path.toString());

                        if (A == null || B == null) {
                            System.err.println("Failed to load matrices for size " + size);
                            continue;
                        }

                        // Perform benchmark
                        System.out.print("Size " + size + ": ");
                        BenchmarkResult result = benchmark(A, B, size, algorithm);
                        benchmarkResults.add(result);
                        System.out.println("Time=" + String.format("%.4f", result.timeSeconds) + "s, " +
                                "Memory=" + String.format("%.2f", result.memoryMB) + "MB, " +
                                "CPU=" + String.format("%.1f", result.cpuPercent) + "%");

                        // Save result matrix
                        Algorithms.Matrix C = null;
                        switch (algorithm) {
                            case "basic":
                                C = Algorithms.matrixMultiplicationBasic(A, B, size);
                                break;
                            case "loop":
                                C = Algorithms.matrixMultiplicationLoopUnroll(A, B, size);
                                break;
                            case "cache":
                                C = Algorithms.matrixMultiplicationCache(A, B, size);
                                break;
                            case "strassen":
                                C = Algorithms.matrixMultiplicationStrassen(A, B, size);
                                break;
                        }

                        Path resultPath = resultsDir.resolve(matrixType).resolve(algorithm).resolve(size + ".txt");
                        Files.createDirectories(resultPath.getParent());
                        saveMatrixResult(C, resultPath.toString());

                    } catch (Exception e) {
                        System.err.println("Error processing size " + size + ": " + e.getMessage());
                    }
                }

                // Save benchmark results for this algorithm
                Path benchmarkPath = benchmarksDir.resolve(matrixType).resolve(algorithm + ".txt");
                try {
                    Files.createDirectories(benchmarkPath.getParent());
                    saveBenchmarkResults(benchmarkResults, benchmarkPath.toString());
                    System.out.println("Benchmark saved to: " + benchmarkPath);
                } catch (IOException e) {
                    System.err.println("Error saving benchmark: " + e.getMessage());
                }
            }
        }

        // Verify all results from the results directory
        System.out.println("\n\n========================================");
        System.out.println("VERIFICATION PHASE - Comparing Result Files");
        System.out.println("========================================");

        for (String matrixType : MATRIX_TYPES) {
            System.out.println("\n" + matrixType.toUpperCase() + " MATRICES:");
            for (int size = MIN_SIZE; size <= MAX_SIZE; size *= 2) {
                verifyResults(resultsDir, matrixType, size);
            }
        }

        System.out.println("\n\n========================================");
        System.out.println("Benchmarking and verification complete!");
        System.out.println("========================================");
    }
}