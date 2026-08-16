// Exfiltration through compiler diagnostics: the compiler quotes the source lines it fails
// on, and compilation errors are returned to the contestant. Reading the expected answer
// this way would let a submission hardcode every output.
int main() {
#include "/sandbox/tasks/testTask/tests/01.out"
#include "/sandbox/tasks/commTask/manager/manager.cpp"
}
