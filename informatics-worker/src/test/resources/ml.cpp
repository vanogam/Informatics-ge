#include <bits/stdc++.h>
using namespace std;
vector<int> v;

main() {
    ios::sync_with_stdio(false);
    int a, b, c = 1000000000;
    cin >> a >> b;
    v.push_back(0);
    v.push_back(1);
    int i = 2;
    while (c > 0) {
        v.push_back(v[i - 1] + v[i - 2]);
        i ++;
        c --;
    }
    cout << v[i - 1];
}