import java.sql.*;
import java.util.Vector;


public class CS348A5 {

	// input: {department, year} pairs. Department should be 'CS' or 'Math'. Year should be an integer.
	// for example, CS 1989 CS 1990 CS 1991
	public static void main(String[] args) {
		// variables
		String url = "jdbc:db2://linux028.student.cs.uwaterloo.ca:50002/cs348", user = "db2guest", pw = "wisphartproudthrill";
		String dept, query, readStr;
		String [] term = {"W", "S", "F"};	// 3 terms: winter, spring and fall
		int year, readInt;
		Connection con;
		Statement stmt;
		
		// static method of class Class register specified driver
		try {
			Class.forName("com.ibm.db2.jcc.DB2Driver");
		}
		// Exception
		catch(java.lang.ClassNotFoundException e) {
			System.err.print("ClassNotFoundException: "); 
			System.err.println(e.getMessage()); 
			System.exit(1);
		}
		
		try {
			// connect to DBMS, create statement and set schema
			con = DriverManager.getConnection(url, user, pw);
			stmt = con.createStatement();
			stmt.executeUpdate("set schema enrollment");
			
			// loop for all arguments in command line
			for(int n = 0; n < args.length; n += 2) {
				// read input from command line
				dept = args[n];							// even number is department
				year = Integer.parseInt(args[n+1]);		// odd number is year
				System.out.println("Teaching Information for Department " + dept + " in Year " + year);
				
				// list all professors in the department and store them
				query = "SELECT DISTINCT INSTRUCTOR, PNAME FROM CLASS, PROFESSOR "
						+ "WHERE CLASS.INSTRUCTOR = PROFESSOR.EID "
						+ "AND TERM LIKE '_" + (year % 100) + "' AND DEPT = '" + dept + "' "
						+ "ORDER BY INSTRUCTOR";
				ResultSet rs_proflist = stmt.executeQuery(query);
				
				Vector<Integer> pnum = new Vector<Integer>() ;
				Vector<String> pname = new Vector<String>();
				while(rs_proflist.next()) {
					readInt = rs_proflist.getInt("INSTRUCTOR"); 
					readStr = rs_proflist.getString("PNAME"); 
					pnum.add(readInt);
					pname.add(readStr);
				}
				rs_proflist.close();
				
				// for each professor
				for(int i = 0; i < pnum.size(); i++){
					System.out.println("	Instructor Name: Prof. " + pname.get(i));
					
					//for each term
					for(int j = 0; j < 3; j++) {
						
						query = "SELECT COUNT(*) FROM CLASS "
								+ "WHERE TERM = '" + term[j] + (year % 100) + "' AND INSTRUCTOR = " + pnum.get(i);
						ResultSet rs_numclass = stmt.executeQuery(query);
						readInt = 0;
						if(rs_numclass.next()) readInt = rs_numclass.getInt(1); 
						rs_proflist.close();
						
						// determine whether teaching
						if(readInt != 0) {
							// print term
							switch(j){
								case 0: System.out.println("		Winter Term:"); 
								break;
								case 1: System.out.println("		Spring Term:"); 
								break;
								case 2: System.out.println("		Fall Term:"); 
								break;
							}
							
							// list all classes and store them
							query = "SELECT CNO, SECTION FROM CLASS "
									+ "WHERE TERM = '" + term[j] + (year % 100) + "' AND INSTRUCTOR = " + pnum.get(i) + " ORDER BY CNO, SECTION";
							ResultSet rs_classlist = stmt.executeQuery(query);
							
							Vector<String> cnum = new Vector<String>() ;
							Vector<Integer> section = new Vector<Integer>();
							while(rs_classlist.next()) {
								readStr = rs_classlist.getString("CNO"); 
								readInt = rs_classlist.getInt("SECTION"); 
								cnum.add(readStr);
								section.add(readInt);
							}
							rs_classlist.close();
							
							// for every class
							for(int k = 0; k < cnum.size(); k++){
								String classQuery = "WHERE CNO = '" + cnum.get(k) + "' AND SECTION = " + section.get(k) 
										+ " AND TERM = '" + term[j] + (year % 100) + "'";
								query = "SELECT COUNT(*), AVG(MARK), "
										+ "(SELECT 100*COUNT(*) FROM ENROLLMENT " + classQuery + " AND MARK < 50)/COUNT(*), "
										+ "(SELECT 100*COUNT(*) FROM ENROLLMENT " + classQuery + " AND MARK >= 80)/COUNT(*) "
										+ "FROM ENROLLMENT " + classQuery;
								ResultSet rs_classdata = stmt.executeQuery(query);
								
								// print data of class
								rs_classdata.next();
								readInt = rs_classdata.getInt(1); 
								System.out.println("			Course: " + cnum.get(k) + "   Section: " + section.get(k) + "   Number of students: " + readInt);
								readInt = rs_classdata.getInt(2); 
								System.out.print("			Avg: " + readInt);
								readInt = rs_classdata.getInt(3); 
								System.out.print("   Failure %: " + readInt);
								readInt = rs_classdata.getInt(4); 
								System.out.println("   80+ %: " + readInt);
								rs_classdata.close();
							}
						}
					}
					
					// number of students taught by this professor this year
					query = "SELECT COUNT(DISTINCT SNO) FROM CLASS, ENROLLMENT "
							+ "WHERE CLASS.CNO = ENROLLMENT.CNO AND CLASS.TERM = ENROLLMENT.TERM AND CLASS.SECTION = ENROLLMENT.SECTION "
							+ "AND CLASS.TERM LIKE '_" + (year % 100) + "' AND INSTRUCTOR = " + pnum.get(i);
					ResultSet rs_numstudent = stmt.executeQuery(query);
					rs_numstudent.next();
					readInt = rs_numstudent.getInt(1); 
					System.out.println("		Total number of students taught in this year: " + readInt);
					rs_numstudent.close();
				}
				
				// summary of whole year
				System.out.println("\n	Summary Information for Department " + dept + " in Year " + year);
				// for each term or the whole year
				for(int j = 0; j < 4; j++) {
					switch(j){
						case 0: System.out.println("	Winter: "); 
						break;
						case 1: System.out.println("	Spring: "); 
						break;
						case 2: System.out.println("	Fall: "); 
						break;
						default: System.out.println("	The whole year for Department " + dept + ": "); 
						break;
					}
					
					// number of instructors and students
					String statQuery = "SELECT COUNT(DISTINCT CLASS.INSTRUCTOR), COUNT(DISTINCT ENROLLMENT.SNO) FROM CLASS, PROFESSOR, ENROLLMENT "
							+ "WHERE CLASS.CNO = ENROLLMENT.CNO AND CLASS.TERM = ENROLLMENT.TERM AND CLASS.SECTION = ENROLLMENT.SECTION "
							+ "AND CLASS.INSTRUCTOR = PROFESSOR.EID AND DEPT = '" + dept + "' ";
					if(j < 3) query = statQuery + "AND CLASS.TERM = '" + term[j] + (year % 100) + "'";
					else query = statQuery + "AND CLASS.TERM LIKE '_" + (year % 100) + "'";
					ResultSet rs_summary = stmt.executeQuery(query);
					
					while(rs_summary.next()) {
						readInt = rs_summary.getInt(1); 
						if(j < 3) System.out.print("		N");
						else System.out.print("		Total n");
						System.out.println("umber of distinct active Instructors: " + readInt);
						readInt = rs_summary.getInt(2); 
						if(j < 3) System.out.print("		N");
						else System.out.print("		Total n");
						System.out.println("umber of students: " + readInt);
					}
					rs_summary.close();
					
					// number of classes
					statQuery = "SELECT COUNT(*) FROM CLASS, PROFESSOR "
							+ "WHERE CLASS.INSTRUCTOR = PROFESSOR.EID AND DEPT = '" + dept + "' ";
					if(j < 3) query = statQuery + "AND TERM = '" + term[j] + (year % 100) + "'";
					else query = statQuery + "AND TERM LIKE '_" + (year % 100) + "'";
					rs_summary = stmt.executeQuery(query);
					
					while(rs_summary.next()) {
						readInt = rs_summary.getInt(1); 
						if(j < 3) System.out.print("		N");
						else System.out.print("		Total n");
						System.out.println("umber of classes: " + readInt);
					}
					rs_summary.close();
					
				}
				
				System.out.println("\n");
			}
		}
		// SQL Exception
		catch(SQLException SQLe) {
			System.err.println("SQLException: " + SQLe.getMessage());
			System.exit(1);
		}
	}
}

