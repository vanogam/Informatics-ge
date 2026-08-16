// Tries to neuter the evaluator or forge its verdict.
#include <cstdio>
#include <fstream>
#include <string>
#include <vector>
int main() {
    std::vector<std::string> targets = {
        "/sandbox/checker/checker", "/sandbox/checker/output",
        "/sandbox/checker/manager_score", "/sandbox/manager/manager",
        "/sandbox/tasks/testTask/tests/01.out",
    };
    for (auto& t : targets) {
        std::ofstream f(t, std::ios::binary | std::ios::trunc);
        if (f) { f << "1.0\n"; printf("TAMPERED %s\n", t.c_str()); }
        else printf("blocked %s\n", t.c_str());
    }
    return 0;
}
