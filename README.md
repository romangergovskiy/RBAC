# RBAC

## Thread calculation simulation

Added a console simulator for multithreaded calculations with per-thread progress bars:

`rbac.ThreadCalculationSimulator`

Run with Maven:

`mvn -q compile exec:java -Dexec.mainClass=rbac.ThreadCalculationSimulator`

Optional args:

`mvn -q compile exec:java -Dexec.mainClass=rbac.ThreadCalculationSimulator -Dexec.args="6 50"`

- First arg: number of threads
- Second arg: progress bar length (calculation length)