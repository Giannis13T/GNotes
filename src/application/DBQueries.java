package application;

import java.sql.*;
import java.util.ArrayList;

public class DBQueries {

	private String tableName = "NOTES";
	private String passTableName = "PASSWORD";
	private String isLocked = "false";
	
	// Method that creates the database table if it doesn't already exist
	public void createTable(Connection conn, Statement st) {
		try {
			DatabaseMetaData dbm = conn.getMetaData();
			ResultSet tables = dbm.getTables(null, null, tableName, null);
			if (!tables.next()) {
				st = conn.createStatement();
				st.execute("CREATE TABLE "+tableName+" (ID INTEGER, NAME VARCHAR(50), NOTE VARCHAR(32672), LOCKED VARCHAR(6))");
				st.close();
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}
	
	// Method that inserts the specified values in the last row of the table
	public void insertValue(Connection conn, Statement st, int id, String name, String note, boolean locked) {
		if (locked) {
			isLocked = "true";
		} else {
			isLocked = "false";
		}
		try {
			PreparedStatement ps = conn.prepareStatement("INSERT INTO "+tableName+" (ID, NAME, NOTE, LOCKED) VALUES (?,?,?,?)");
			ps.setInt(1, id);
			ps.setString(2, name);
			ps.setString(3, note);
			ps.setString(4, isLocked);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}
	
	// Method that returns the specified note from the table
	public String selectValue(Connection conn, Statement st, int id) {
		String notes = null;
		try {
			st = conn.createStatement();
			ResultSet results = st.executeQuery("SELECT NOTE FROM "+tableName+" WHERE ID="+id);
			while (results.next()) {
				notes = results.getString(3);
			}
			results.close();
			st.close();
			return notes;
		} catch (SQLException ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	// Method that deletes the row with the specified id value from the table
	public void deleteValue(Connection conn, Statement st, int id) {
		try {
			st = conn.createStatement();
			st.execute("DELETE FROM "+tableName+" WHERE ID="+id);
			st.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}
	
	// Method that updates the values of the table's specified row
	public void updateValue(Connection conn, Statement st, int id, String name, String note, boolean locked) {
		try {
			if (locked) {
				isLocked = "true";
			} else {
				isLocked = "false";
			}
			PreparedStatement ps = conn.prepareStatement("UPDATE "+tableName+" SET NAME=?, NOTE=?, LOCKED=? WHERE ID="+id);
			ps.setString(1, name);
			ps.setString(2, note);
			ps.setString(3, isLocked);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}
	
	/*----------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ---------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ---------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ---------------------------------------------------------------------------------------------------------------------------------------------------------
	 */
	
	// Method that creates the database table containing the notes' password and salt if it doesn't already exist
	public void createPassTable(Connection conn, Statement st) {
		try {
			DatabaseMetaData dbm = conn.getMetaData();
			ResultSet tables = dbm.getTables(null, null, passTableName, null);
			if (!tables.next()) {
				st = conn.createStatement();
				st.execute("CREATE TABLE "+passTableName+" (PSWD VARCHAR(300), SALT VARCHAR(30) FOR BIT DATA)");
				st.close();
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}
		
	// Method that inserts the password and salt in the password's table
	public void insertPassValue(Connection conn, String pswd, byte[] salt) {
		try {
			PreparedStatement ps = conn.prepareStatement("INSERT INTO "+passTableName+" (PSWD, SALT) VALUES (?,?)");
			ps.setString(1, pswd);
			ps.setBytes(2, salt);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}
	
	// Method that returns the password from the table
	public String selectPassValue(Connection conn, Statement st) {
		String pswd = null;
		try {
			st = conn.createStatement();
			ResultSet results = st.executeQuery("SELECT PSWD FROM "+passTableName);
			while (results.next()) {
				pswd = results.getString(1);
			}
			results.close();
			st.close();
			return pswd;
		} catch (SQLException ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	// Method that returns the salt from the table
	public byte[] selectSaltValue(Connection conn, Statement st) {
		byte[] salt = null;
		try {
			st = conn.createStatement();
			ResultSet results = st.executeQuery("SELECT SALT FROM "+passTableName);
			while (results.next()) {
				salt = results.getBytes(2);
			}
			results.close();
			st.close();
			return salt;
		} catch (SQLException ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	// Method that deletes the current password and salt from the table
	public void deletePassValue(Connection conn, Statement st) {
		try {
			st = conn.createStatement();
			st.execute("DELETE * FROM "+passTableName);
			st.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}
	
	// Method that updates the password and salt in the table
	public void updatePassValue(Connection conn, Statement st, String pswd, byte[] salt) {
		try {
			PreparedStatement ps = conn.prepareStatement("UPDATE "+passTableName+" SET PSWD=?, SALT=?");
			ps.setString(1, pswd);
			ps.setBytes(2, salt);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}
	
	/*----------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ---------------------------------------------------------------------------------------------------------------------------------------------------------
	 */
	
	// Method that syncs the data contained in the database with the program when the user opens it
	public ListNotes syncData(Connection c, Statement st) {
		Integer id = Integer.valueOf(-1);
		Integer id2 = Integer.valueOf(-1);
		String nm = null;
		String nt = null;
		String lck = null;
		boolean lock = false;
		Note n;
		ListNotes lnotes = new ListNotes();
		ArrayList <Integer> ids = new ArrayList<Integer>();
		try {
			st = c.createStatement();
			ResultSet results = st.executeQuery("SELECT ID FROM "+tableName);
			while (results.next()) {
				id2 = Integer.valueOf(results.getInt(1));
				ids.add(id2);
			}
			results.close();
			for (int i=0; i<ids.size(); i++) {
				results = st.executeQuery("SELECT * FROM "+tableName+" WHERE ID="+ids.get(i));
				while (results.next()) {
					id = Integer.valueOf(results.getInt(1));
					nm = results.getString(2);
					nt = results.getString(3);
					lck = results.getString(4);
					if (lck.equals("true")) {
						lock = true;
					} else {
						lock = false;
					}
					n = new Note(id, nm, nt, lock);
					lnotes.add(n);
				}
			}
			results.close();
			st.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		return lnotes;
	}
	
}
