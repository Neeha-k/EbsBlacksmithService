## Development Setup

### IntelliJ
1. Create a workspace and check out this package
2. From terminal run `brazil-build`
3. From terminal run `brazil-build wrapper`
4. Open IntelliJ and select `File > Open` and open the workspace directory
5. Open `File > Project Structure > Project Settings > Project` and set SDK to corretto-17
6. Open `IntelliJ IDEA > Preferences > Build, Execution, Deployment > Build Tools > Gradle`, and set the Gradle JVM to corretto-11
7. Select `Brazil > Sync Dependencies` and `Brazil > Sync from Workspace`
