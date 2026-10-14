# EduTrack – Intelligent University Academic Platform

Java console prototype covering Modules M1–M6. Data is kept in memory, so it resets every time the program starts.

## Modules
- **M1 String Algorithms** – KMP, Z-Function, Rabin-Karp, Aho-Corasick
- **M2 Suffix Structures** – Suffix Array (prefix doubling + binary search), Kasai LCP, repeated-phrase and document-similarity analysis, SA-IS / Suffix Tree / Suffix Automaton (compact educational versions)
- **M3 Dynamic Programming** – Levenshtein, Damerau-Levenshtein, Matrix Chain, Bitmask DP, Optimal BST
- **M4 Network Flow** – Ford-Fulkerson (DFS), Edmonds-Karp (BFS), Dinic, Min-Cut, Bipartite Matching with König's theorem (the app uses the first three for allocation; Min-Cut and König are checked in `SelfTest`)
- **M5 NP-Completeness (CO5)** – SAT solver (exam timetable)
- **M6 Randomized & Parallel (CO6)** – Randomized QuickSort (merit ranking), Reservoir Sampling (random activity check)

## What is new in this version
0. **M5 and M6 are now implemented** (see below): exam planning and analytics in the Faculty Dashboard, and an exam timetable view in the Student Portal.

### CO5 – Exam planning (Faculty Dashboard, option 11)
| Menu option | What it does | Concept behind it |
|---|---|---|
| Show exam clashes | Lists course pairs that share students | clash graph from enrolments |
| Build exam timetable | Gives every exam a slot and a room so that no student has two exams at once; asks for the number of slots (Enter = 3) and uses more if needed | SAT (DPLL solver) |
| View current timetable | Shows the stored timetable | – |

### CO6 – Analytics (Faculty Dashboard, option 12)
| Menu option | What it does | Concept behind it |
|---|---|---|
| Student merit ranking | Ranks students by total marks (ties share a rank) | Randomized QuickSort |
| Random check of recent activity | Picks a fair sample from a stream of 5000 simulated events, keeping only k in memory | Reservoir Sampling |

Students can see their own exams under **Student Portal → 6. My Exam Timetable** once faculty have built a timetable.

### Earlier changes
1. **Student management in the Faculty Dashboard** – view, add, update, delete and search students.
2. **Faculty-Course Allocation report** – written in plain language: the rules, who can teach what, each assignment step ("F101 takes CS301"), the final result, and a one-line reason for every course left without a teacher. Internally it is a maximum-flow problem; three flow algorithms are run and silently checked against each other.
3. **Document Analysis** – repeated text in one document, search with surrounding text, and a similarity score for two documents. Internally it uses a suffix array and its LCP array; the report itself only uses everyday words.
4. **Readable code** – every class is formatted one statement per line, named clearly and documented with comments. Shared console input handling lives in `ui/InputHelper`, and output formatting in `util/TextUtil`.
5. **`main/SelfTest`** – checks the algorithms against brute-force answers on random inputs.

## Login
Password = ID + `@123`

| Role | ID | Password |
|---|---|---|
| Student | S101, S102, S103, S104 | S101@123 ... |
| Faculty | F101, F102, F103 | F101@123 ... |

A student added by faculty can log in immediately (for example S105 / S105@123).

## Faculty Dashboard
```
 1. My Profile
 2. View Faculty
 3. View Courses
 4. Faculty-Course Allocation
 5. Search Courses
 --- Student Management ---
 6. View All Students
 7. Add Student
 8. Update Student
 9. Delete Student
10. Search Student by Name
 --- Exams & Analytics ---
11. Exam Planning       -> clashes / build timetable / view timetable
12. Analytics           -> merit ranking / random activity check
 0. Logout
```
- **Add** – a blank ID or email uses the automatic suggestion, and every field is validated (ID format, unique ID and email, semester 1-8).
- **Update** – a blank answer keeps that field. The ID cannot change. The screen lists exactly what changed.
- **Delete** – shows the record and asks for confirmation before deleting.
- **Search** – KMP substring search; when nothing matches, edit distance suggests the closest name.

## Student Portal
```
1. My Profile
2. View Courses
3. Search Courses
4. Search Assignments
5. Document Analysis   -> analyse / search / compare / list sample documents
6. My Exam Timetable   -> exams of the courses you take (after faculty build the timetable)
0. Logout
```
In the document menu you can type a sample document ID (D101, D102, D103) or paste any text; a blank answer uses a default sample.

## Project structure
```
src/
  main/                Main (login), SelfTest (algorithm checks)
  ui/                  Menus: FacultyMenu, StudentMenu, StudentManagementMenu, DocumentMenu, ExamMenu, AnalyticsMenu, InputHelper ...
  services/            StudentService, AllocationService, DocumentService, SearchService, ExamScheduleService, AnalyticsService, AuthService ...
  data/                In-memory stores: StudentData, CourseData, FacultyData, DocumentData, EnrollmentData, MarkData, ExamData ...
  models/              Student, Faculty, Course, Assignment, Exam, Resource, Document, Mark
  util/                TextUtil (console formatting)
  stringalgorithms/    KMP, ZFunction, RabinKarp, AhoCorasick
  suffixstructures/    SuffixArray, Kasai, LCP, SuffixArrayAnalysis, SAIS, SuffixTree, SuffixAutomaton
  dynamicprogramming/  Levenshtein, DamerauLevenshtein, MatrixChain, BitmaskDP, OptimalBST
  networkflow/         FlowNetwork, FlowResult, ResidualGraph, FordFulkerson, EdmondsKarp, Dinic, MinCut, BipartiteMatching
  npcompleteness/      SATSolver, SimpleGraph
  randomizedparallel/  RandomizedQuickSort, ReservoirSampling
```

## Run in Eclipse
1. Import this folder as an existing Java project (`File > Import > General > Existing Projects into Workspace`).
2. Make sure `src` is the source folder.
3. Run `src/main/Main.java`.

## Run from the command line
Requires Java 8 or newer. From the project root:

```bash
javac -d bin $(find src -name "*.java")
java -cp bin main.Main
```

Verify the algorithms (M1–M6, 12 groups of checks against brute-force answers):

```bash
java -cp bin main.SelfTest
```

## Notes
- SA-IS, Suffix Tree and Suffix Automaton are intentionally compact educational versions (SA-IS delegates to the prefix-doubling builder; the tree/automaton answer substring queries).
- The SAT solver is exponential-time in the worst case (the problem is NP-complete); it is used only on the small exam instances here.
- Exam enrolments and marks are sample data (`EnrollmentData`, `MarkData`). A student added by faculty has no enrolments or marks until they are added to those stores.
- Parallel timings depend on the machine: on a single-core computer the parallel sum is not faster than the plain loop, but the totals always match.
- Passwords are derived from IDs for demonstration only.
