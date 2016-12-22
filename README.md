# Java-database-program-with-JDBC-

This is a Java program (with JDBC) that generates the required result (Teaching Information for a Department in one Year). User inputs at least one pair of {department, year} in the command line. Department should be 'CS' or 'Math'. Year should be an integer. An example is “CS 1989 CS 1990 CS 1991”. For each pairs, the program searches for the list of professors teaching in this year. After that, for each professor, the program searches for the list of classes they taught, and prints the required result. Finally, it prints the summary.

relational schema.

Student(SNO, SNAME)

Course(CNO, CNAME)

Prerequisite(CNO, PREREQ)

Professor(EID,PNAME, OFFICE, DEPT)

Enrollment(SNO, CNO, TERM, SECTION, MARK)

Schedule(CNO, TERM, SECTION, DAY, HOUR, ROOM)

Class(CNO, TERM, SECTION, INSTRUCTOR)
