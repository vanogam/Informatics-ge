// Grader for the multi-process communication fixture: talks to the manager over stdin/stdout
// and takes its process index from argv[1], the way a CMS stub does. Refusing to run without
// that argument is the point - it is what a single-process executor gets wrong.
#include "scale.h"

#include <cstdio>
#include <cstdlib>

int main(int argc, char** argv) {
    if (argc < 2) {
        fprintf(stderr, "invalid args\n");
        return 1;
    }
    int index = atoi(argv[1]);

    int factor = 0;
    if (scanf("%d", &factor) != 1) {
        fprintf(stderr, "grader %d could not read the manager's request\n", index);
        return 1;
    }

    printf("%d\n", scale(factor, index));
    fflush(stdout);
    return 0;
}