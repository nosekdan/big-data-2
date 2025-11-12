from pathlib import Path
import random
import sys

# This functions generates random square matrices of a size given by the parameter.
# Matrices will be stored in the ../input directory in subdirectories based on the size.
# The files are named 1.txt and 2.txt. 
# The numbers in a line in the matrices are separated by spaces and lines are separated
# by the new line character \n.
# These matrices are later used to benchmark matrix multiplication in different programming languages.
# The second parameter (boolean) says if it should be a sparse matrix
def generateMatrix(size, sparse):
    if not isinstance(size, int):
        raise TypeError("Size must be integer.")
    if size <= 0:
        raise ValueError("Size must be positive.")
    if not isinstance(sparse, int):
        raise TypeError("Sparse must be integer.")
    

    matrix_dir = (Path(__file__).resolve().parent / ".." / "input" / ("sparse" if (sparse) else "normal") / str(size)).resolve()
    matrix_dir.mkdir(parents=True, exist_ok=True)

    rng = random.Random()
    generated_files = []

    for i in range(1, 2 + 1):
        # Build the matrix as lines of space-separated integers
        lines = []
        for _ in range(size):
            row = []
            for _ in range(size):
                if sparse:
                    # If rand % 3 == 0, choose a number (1-9), otherwise 0
                    rand_num = rng.randint(0, 100)
                    chosen_number = rng.randint(1, 9) if rand_num % 3 == 0 else 0
                    row.append(str(chosen_number))
                else:
                    # Normal: any digit 0-9
                    row.append(str(rng.randint(0, 9)))
            lines.append(" ".join(row))

        content = "\n".join(lines) + "\n"
        file_path = matrix_dir / f"{i}.txt"
        file_path.write_text(content, encoding="utf-8")
        generated_files.append(str(file_path))

    return generated_files

if __name__ == "__main__":
    if len(sys.argv) != 2 and len(sys.argv) != 3:
        print("Usage: python generateMatrix.py <size> <sparse=0>")
        sys.exit(1)
    
    try:
        size = int(sys.argv[1])
        sparse = bool(sys.argv[2])
        files = generateMatrix(size, sparse)
        print(f"Generated {len(files)} matrices of size {size}x{size}")
    except ValueError as e:
        print(f"Error: {e}")
        sys.exit(1)