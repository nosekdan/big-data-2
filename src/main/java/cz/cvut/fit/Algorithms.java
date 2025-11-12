package cz.cvut.fit;

public class Algorithms {

    // Matrix class to hold 2D integer arrays
    static class Matrix {
        int[][] data;
        int size;

        Matrix(int[][] data) {
            this.data = data;
            this.size = data.length;
        }

        Matrix(int size) {
            this.data = new int[size][size];
            this.size = size;
        }
    }

    // Basic matrix multiplication
    public static Matrix matrixMultiplicationBasic(Matrix A, Matrix B, int size) {
        Matrix C = new Matrix(size);

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                for (int k = 0; k < size; k++) {
                    C.data[i][j] += A.data[i][k] * B.data[k][j];
                }
            }
        }

        return C;
    }

    // Matrix multiplication - loop unroll
    public static Matrix matrixMultiplicationLoopUnroll(Matrix A, Matrix B, int size) {
        Matrix C = new Matrix(size);
        int unrollFactor = 4; // Process 4 elements per iteration

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                // Process k in chunks of 4
                int k = 0;
                for (; k <= size - unrollFactor; k += unrollFactor) {
                    C.data[i][j] += A.data[i][k] * B.data[k][j];
                    C.data[i][j] += A.data[i][k + 1] * B.data[k + 1][j];
                    C.data[i][j] += A.data[i][k + 2] * B.data[k + 2][j];
                    C.data[i][j] += A.data[i][k + 3] * B.data[k + 3][j];
                }

                // Handle remaining elements
                for (; k < size; k++) {
                    C.data[i][j] += A.data[i][k] * B.data[k][j];
                }
            }
        }

        return C;
    }

    // Matrix multiplication - cache optimization
    public static Matrix matrixMultiplicationCache(Matrix A, Matrix B, int size) {
        Matrix C = new Matrix(size);
        int blockSize = 64; // Cache-friendly block size (based on CPU cache)

        // Process matrix in blocks
        for (int ii = 0; ii < size; ii += blockSize) {
            for (int jj = 0; jj < size; jj += blockSize) {
                for (int kk = 0; kk < size; kk += blockSize) {
                    // Multiply blocks
                    for (int i = ii; i < Math.min(ii + blockSize, size); i++) {
                        for (int j = jj; j < Math.min(jj + blockSize, size); j++) {
                            for (int k = kk; k < Math.min(kk + blockSize, size); k++) {
                                C.data[i][j] += A.data[i][k] * B.data[k][j];
                            }
                        }
                    }
                }
            }
        }

        return C;
    }


    // Matrix multiplication - Strassen Algo
    public static Matrix matrixMultiplicationStrassen(Matrix A, Matrix B, int size) {
        // Base case: use basic multiplication for small matrices
        if (size <= 64) {
            return matrixMultiplicationBasic(A, B, size);
        }

        // Ensure size is a power of 2 by padding if necessary
        int n = size;
        if ((n & (n - 1)) != 0) {
            n = Integer.highestOneBit(n) << 1;
        }

        // Divide matrices into 4 submatrices
        int newSize = n / 2;
        Matrix[][] A_sub = new Matrix[2][2];
        Matrix[][] B_sub = new Matrix[2][2];

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                A_sub[i][j] = getSubmatrix(A, i, j, newSize, size);
                B_sub[i][j] = getSubmatrix(B, i, j, newSize, size);
            }
        }

        // Calculate 7 helper matrices
        Matrix M1 = matrixMultiplicationStrassen(add(A_sub[0][0], A_sub[1][1], newSize),
                add(B_sub[0][0], B_sub[1][1], newSize), newSize);
        Matrix M2 = matrixMultiplicationStrassen(add(A_sub[1][0], A_sub[1][1], newSize), B_sub[0][0], newSize);
        Matrix M3 = matrixMultiplicationStrassen(A_sub[0][0], sub(B_sub[0][1], B_sub[1][1], newSize), newSize);
        Matrix M4 = matrixMultiplicationStrassen(A_sub[1][1], sub(B_sub[1][0], B_sub[0][0], newSize), newSize);
        Matrix M5 = matrixMultiplicationStrassen(add(A_sub[0][0], A_sub[0][1], newSize), B_sub[1][1], newSize);
        Matrix M6 = matrixMultiplicationStrassen(sub(A_sub[1][0], A_sub[0][0], newSize),
                add(B_sub[0][0], B_sub[0][1], newSize), newSize);
        Matrix M7 = matrixMultiplicationStrassen(sub(A_sub[0][1], A_sub[1][1], newSize),
                add(B_sub[1][0], B_sub[1][1], newSize), newSize);

        // Calculate result submatrices
        Matrix C11 = add(sub(add(M1, M4, newSize), M5, newSize), M7, newSize);
        Matrix C12 = add(M3, M5, newSize);
        Matrix C21 = add(M2, M4, newSize);
        Matrix C22 = add(sub(add(M1, M3, newSize), M2, newSize), M6, newSize);

        // Combine submatrices into result
        return combine(C11, C12, C21, C22, size, newSize);
    }

    // Helper method to extract submatrix
    private static Matrix getSubmatrix(Matrix M, int row, int col, int subsize, int originalSize) {
        Matrix sub = new Matrix(subsize);
        int startRow = row * subsize;
        int startCol = col * subsize;
        for (int i = 0; i < subsize; i++) {
            for (int j = 0; j < subsize; j++) {
                if (startRow + i < originalSize && startCol + j < originalSize) {
                    sub.data[i][j] = M.data[startRow + i][startCol + j];
                }
            }
        }
        return sub;
    }

    // Helper methods for matrix addition and subtraction
    private static Matrix add(Matrix A, Matrix B, int size) {
        Matrix C = new Matrix(size);
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                C.data[i][j] = A.data[i][j] + B.data[i][j];
            }
        }
        return C;
    }

    private static Matrix sub(Matrix A, Matrix B, int size) {
        Matrix C = new Matrix(size);
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                C.data[i][j] = A.data[i][j] - B.data[i][j];
            }
        }
        return C;
    }

    private static Matrix combine(Matrix C11, Matrix C12, Matrix C21, Matrix C22, int size, int subsize) {
        Matrix C = new Matrix(size);
        for (int i = 0; i < subsize; i++) {
            for (int j = 0; j < subsize; j++) {
                C.data[i][j] = C11.data[i][j];
                C.data[i][j + subsize] = C12.data[i][j];
                C.data[i + subsize][j] = C21.data[i][j];
                C.data[i + subsize][j + subsize] = C22.data[i][j];
            }
        }
        return C;
    }

}
