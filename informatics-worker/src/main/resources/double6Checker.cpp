#include <iostream>
#include <string>
#include <fstream>
using namespace std;
ifstream inp;
ifstream cont;
ifstream ans;
void wa() {
    cerr << "Wrong answer!" << endl;
    cout << "0.0" << endl;
    exit(0);
}

void ok() {
    cerr << "Correct!" << endl;
    cout << "1.0" << endl;
    exit(0);
}

int main(int argc, char* argv[]) {
    if (argc != 4) {
        cerr << "System error!" << endl;
        cout << "0.0" << endl;
        exit(0);
    }

    ans = ifstream(argv[2]);
    cont = ifstream(argv[3]);

    while (true) {
        string contestantToken, answerToken;
        ans >> answerToken;
        cont >> contestantToken;

        if (ans.eof()^cont.eof()) {
            wa();
        }
        double contestantValue = stod(contestantToken);
        double answerValue = stod(answerToken);
        if (abs(contestantValue - answerValue) > 1e-6) {
            wa();
        }

        if (ans.eof() && cont.eof()) {
            ok();
        }

    }

    return 0;
}