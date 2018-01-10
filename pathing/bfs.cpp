
#include <iostream>
#include <queue>
using namespace std;
int r, c;
int path[250][250][250][250];
int graph[250][250];

/*
 1 2 3
 4 5 6
 7 8 9
 */

bool check(int a, int b, int curr, int curc){
    return a>=0 && a<r && b>=0 && b<c && path[curr][curc][a][b]==0 && graph[a][b] == true;
}

void bfs(int curr, int curc){
    queue<pair<int, int>> q;
    q.push(make_pair(curr, curc));
    path[curr][curc][curr][curc] = -1;
    while(!q.empty()){
        int t1 = q.front().first, t2 = q.front().second; q.pop();
        for(int x = -1; x<=1; x++){
            for(int y = -1; y<=1; y++){
                if(!(x == 0 && y == 0) && check(t1+x, t2+y, curr, curc)){
                    q.push(make_pair(t1+x, t2+y));
                    path[curr][curc][t1+x][t2+y] = 3*(x+1)+(y+1)+1;
                }
            }
        }
    }
}

int main() {
    cin>>r>>c;
    for(int x = 0; x<r; x++){
        for(int y = 0; y<c; y++){
            cin>>graph[x][y];
        }
    }
    for(int x = 0; x<r; x++){
        for(int y = 0; y<c; y++){
            bfs(x, y);
        }
    }
    cout<<endl;
    for(int x = 0; x<r; x++){
        for(int y = 0; y<c; y++){
            for(int x1 = 0; x1<r; x1++){
                for(int y1 = 0; y1<c; y1++){
                    cout<<path[x][y][x1][y1]<<" ";
                }
                cout<<endl;
            }
            cout<<endl;
        }
    }
}
