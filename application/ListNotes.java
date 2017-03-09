package application;

import java.util.ArrayList;

public class ListNotes {

private ArrayList <Note> notes = new ArrayList<Note>();
	
	public void add(Note note) {
		notes.add(note);
	}
	
	public void remove(int i) {
		notes.remove(i);
	}
	
	public int size() {
		return notes.size();
	}
	
	public Note get(int i) {
		return notes.get(i);
	}
	
	public void listNotes() {
		for (Note n: notes) {
			System.out.println(n.toString());
		}
	}
	
}
