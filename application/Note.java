package application;

public class Note {

	private int id;
	private String name;
	private String noteText;
	private boolean locked;
	
	// Constructor
	public Note(int id, String name, String noteText, boolean locked) {
		this.name = name;
		this.noteText = noteText;
		this.locked = locked;
		this.id = id;
	}
	
	// Setters & Getters
	public String getName() {
		return name;
	}
	
	public void setName(String newName) {
		name = newName;
	}
	
	public String getNoteText() {
		return noteText;
	}
	
	public void setNoteText(String newText) {
		noteText = newText;
	}
	
	public boolean isLocked() {
		return locked;
	}
	
	public void setLocked(boolean lock) {
		locked = lock;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int iD) {
		id = iD;
	}
	
	// toString()
	public String toString() {
		return (id+"#"+name+"#"+locked);
	}
	
}
