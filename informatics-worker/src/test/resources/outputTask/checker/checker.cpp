// The task's own checker, compiled inside the sandbox and run like a built-in one:
// argv[1] input, argv[2] answer, argv[3] contestant output, score on stdout.
//
// It awards half marks for an answer that is off by one, which is what makes it visible in the
// verdict that the task's checker - and not a built-in comparison - decided the score.
#include <fstream>
#include <iostream>
#include <cstdlib>
using namespace std;

int main(int argc, char* argv[]) {
    if (argc != 4) {
        cerr << "System error!" << endl;
        cout << "0.0" << endl;
        return 0;
    }

    ifstream ans(argv[2]);
    ifstream cont(argv[3]);

    long long expected, submitted;
    if (!(ans >> expected) || !(cont >> submitted)) {
        cerr << "Missing value" << endl;
        cout << "0.0" << endl;
        return 0;
    }

    if (expected == submitted) {
        cerr << "Correct!" << endl;
        cout << "1.0" << endl;
    } else if (llabs(expected - submitted) == 1) {
        cerr << "Off by one" << endl;
        cout << "0.5" << endl;
    } else {
        cerr << "Wrong answer!" << endl;
        cout << "0.0" << endl;
    }
    return 0;
}
