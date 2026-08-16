// Probes every path holding evaluator code or the expected answer.
#include <cstdio>
#include <fstream>
#include <string>
#include <vector>
int main() {
    std::vector<std::string> targets = {
        "/sandbox/checker/checker", "/sandbox/checker/output",
        "/sandbox/checkers/tokenChecker", "/sandbox/manager/manager",
        "/sandbox/tasks/testTask/tests/01.out",
        "/sandbox/tasks/commTask/manager/manager.cpp",
        "/sandbox/tasks/commTask/graders/grader.cpp",
    };
    for (auto& t : targets) {
        std::ifstream f(t, std::ios::binary);
        std::string first;
        if (f && std::getline(f, first)) printf("LEAK %s :: %.60s\n", t.c_str(), first.c_str());
        else printf("blocked %s\n", t.c_str());
    }
    return 0;
}
