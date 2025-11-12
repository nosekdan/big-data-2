import filecmp
import difflib

def compareResults():
    MIN_SIZE = 2
    MAX_SIZE = 512
    cppdir = "../results/cpp/"
    pydir = "../results/python/"
    javadir = "../results/java/"
    i = MIN_SIZE
    while i <= MAX_SIZE:
        file1 = cppdir + str(i) + ".txt"
        file2 = pydir + str(i) + ".txt"
        file3 = javadir + str(i) + ".txt"
        diff = compareFiles(file1, file2)
        diff2 = compareFiles(file2, file3)
        if not diff and not diff2:  # Empty diff means files match
            print(f"Results for size {i} match.")
        else:
            print(f"Fail: Results for size {i} do not match.")
            print(''.join(diff).join(diff2))  # Print the actual diff
        i *= 2

def compareFiles(file1, file2) -> list:
    try:
        with open(file1, 'r') as f1, open(file2, 'r') as f2:
            lines1 = f1.readlines()
            lines2 = f2.readlines()

        # Generate unified diff
        diff = difflib.unified_diff(lines1, lines2, fromfile=file1, tofile=file2, lineterm='')
        return list(diff)
    except FileNotFoundError as e:
        print(f"Error: Could not find file {e.filename}")
        return [f"File not found: {e.filename}"]
    except Exception as e:
        print(f"Error reading files: {e}")
        return [f"Error: {e}"]

if __name__ == "__main__":
    compareResults()