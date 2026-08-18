// Manager for the multi-process communication fixture.
//
// It infers the process count from the number of FIFO pairs it is handed - the CMS convention,
// solution-to-manager path first - then asks every process for factor * index and scores 1.0
// only when all of them answer correctly. Deliberately free of testlib so the fixture exercises
// the executor rather than the checker library.
#include <cstdio>
#include <vector>

int main(int argc, char** argv) {
    int processes = (argc - 1) / 2;
    if (processes < 1) {
        fprintf(stderr, "FAIL manager needs at least one FIFO pair\n");
        printf("0\n");
        return 0;
    }

    int factor = 0;
    if (scanf("%d", &factor) != 1) {
        fprintf(stderr, "FAIL manager could not read the test input\n");
        printf("0\n");
        return 0;
    }

    std::vector<FILE*> fromSolution(processes), toSolution(processes);
    for (int i = 0; i < processes; ++i) {
        toSolution[i] = fopen(argv[1 + 2 * i + 1], "w");
        fromSolution[i] = fopen(argv[1 + 2 * i], "r");
        if (toSolution[i] == nullptr || fromSolution[i] == nullptr) {
            fprintf(stderr, "FAIL manager could not open the FIFO pair for process %d\n", i);
            printf("0\n");
            return 0;
        }
    }

    // Descending order on purpose: every process must already be running, so an executor that
    // starts them one at a time deadlocks here instead of quietly passing. Every process is
    // talked to even once the answer is known to be wrong - dropping out early would leave the
    // rest reading a closed pipe, and they would fail as crashes rather than as a wrong answer.
    bool correct = true;
    for (int i = processes - 1; i >= 0; --i) {
        fprintf(toSolution[i], "%d\n", factor);
        fflush(toSolution[i]);

        int answer = 0;
        if (fscanf(fromSolution[i], "%d", &answer) != 1) {
            fprintf(stderr, "process %d sent nothing\n", i);
            correct = false;
        } else if (answer != factor * i) {
            fprintf(stderr, "process %d answered %d, expected %d\n", i, answer, factor * i);
            correct = false;
        }
    }

    if (correct) {
        fprintf(stderr, "all %d processes answered correctly\n", processes);
    }
    printf(correct ? "1\n" : "0\n");
    return 0;
}
