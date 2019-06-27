package application;

import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.sql.*;
import java.util.Base64;
import java.util.Optional;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.web.HTMLEditor;

public class GnotesController {

	// GUI variables
	@FXML private MenuItem New;
	@FXML private MenuItem save;
	@FXML private MenuItem delete;
	@FXML private MenuItem lock;
	@FXML private MenuItem unlock;
	@FXML private MenuItem changename;
	@FXML private MenuItem newpass;
	@FXML private MenuItem about;
	@FXML private ListView<String> list;
	private final ObservableList<String> oblist = FXCollections.observableArrayList();
	@FXML private HTMLEditor details;
	// Database objects and variables
	final String URL = "jdbc:derby:GNotesDB;create=true;user=gnotes";
	Connection conn;
	Statement st;
	ResultSet resultset;
	ResultSetMetaData rsmd;
	// Creating necessary objects
	ListNotes listnotes = new ListNotes();
	Note newNote;
	DBQueries query = new DBQueries();
	Alert alert;
	TextInputDialog dialog;
	Dialog<String> dl;
	// Other variables
	String password = null;
	int idCounter = 0;
	String noteName = null;
	boolean locked = false;
	boolean saved = false;
	boolean newlyCreated = false;
	int ind = -1;
	int ind2 = -1;
	
	// initialize controller
	public void initialize() {
		
		// Establish connection with the database
		try {
			conn = DriverManager.getConnection(URL);
			st = conn.createStatement();
			showDialog(alert,0,"Loaded data",null,"Successfully loaded data");
		} catch (SQLException ex) {
			showDialog(alert,2,"Error",null,"Couldn't load data");
			ex.printStackTrace();
		}
		// Get data from the database's tables
		query.createTable(conn, st);
		query.createPassTable(conn, st);
		password = query.selectPassValue(conn, st);
		listnotes = query.syncData(conn, st);
		for (int i=0; i<listnotes.size(); i++) {
			idCounter = listnotes.get(i).getId();
			oblist.add(listnotes.get(i).getName());
			list.setItems(oblist);
		}
		
	}
	
	// What happens when menu items are clicked
	@FXML
	private void menuItemsClicked(ActionEvent e) {
		if (e.getSource()==New) {
		// What happens when "New note" option is selected
			if ((oblist.size()!=1)&&((!saved)&&(newlyCreated))) {
				int answer = showConfirmDialog(alert, "Warning", null, "A note is unsaved. If you create a new note, the unsaved one will be deleted.\nDo you want to proceed?");
				if (answer==0) {
					oblist.remove(noteName);
					list.setItems(oblist);
					details.setHtmlText("");
					list.getSelectionModel().clearSelection();
					ind = -1;
					ind2 = -1;
					saved = true;
					newlyCreated = false;
					locked = false;
					noteName = showInputDialog(dialog,"Input",null,"Enter the name of the note: ");
					if (noteName!=null) {
						oblist.add(noteName);
						list.setItems(oblist);
						list.getSelectionModel().select(oblist.size()-1);
						details.setHtmlText("");
						newlyCreated = true;
						saved = false;
						ind2 = list.getSelectionModel().getSelectedIndex();
					}
				}
			} else if ((oblist.size()==1)&&((!saved)&&(newlyCreated))) {
				int answer = showConfirmDialog(alert, "Warning", null, "A note is unsaved. If you create a new note, the unsaved one will be deleted.\nDo you want to proceed?");
				if (answer==0) {
					oblist.remove(noteName);
					list.setItems(oblist);
					details.setHtmlText("");
					list.getSelectionModel().clearSelection();
					ind = -1;
					ind2 = -1;
					saved = false;
					newlyCreated = false;
					locked = false;
					noteName = showInputDialog(dialog,"Input",null,"Enter the name of the note: ");
					if (noteName!=null) {
						oblist.add(noteName);
						list.setItems(oblist);
						list.getSelectionModel().select(oblist.size()-1);
						details.setHtmlText("");
						newlyCreated = true;
						saved = false;
						ind2 = list.getSelectionModel().getSelectedIndex();
					}
				}
			} else if (oblist.size()!=1) {
				noteName = showInputDialog(dialog,"Input",null,"Enter the name of the note: ");
				if (noteName!=null) {
					oblist.add(noteName);
					list.setItems(oblist);
					list.getSelectionModel().select(oblist.size()-1);
					details.setHtmlText("");
					newlyCreated = true;
					saved = false;
					locked = false;
					ind2 = list.getSelectionModel().getSelectedIndex();
				}
			}  else {
				noteName = showInputDialog(dialog,"Input",null,"Enter the name of the note: ");
				if (noteName!=null) {
					oblist.add(noteName);
					list.setItems(oblist);
					list.getSelectionModel().select(oblist.size()-1);
					details.setHtmlText("");
					newlyCreated = true;
					saved = false;
					locked = false;
					ind2 = list.getSelectionModel().getSelectedIndex();
				}
			}
		} else if (e.getSource()==save) {
		// What happens when "Save note" option is selected
			if (oblist.size()==0) {
				showDialog(alert,2,"Error",null,"The list is empty.");
			} else if (list.getSelectionModel().getSelectedIndex()==(oblist.size()-1)) {
				try {
					if ((!saved)&&(newlyCreated)) {
						if (locked) {
							String pswd = null;
							pswd = showPassDialog(dl,"Password",null,"Enter the password: ");
							if (pswd!=null) {
								while (pswd.equals(password)==false) {
									pswd = reEnterPassword(alert, dl,"Password",null,"Enter the password: ");
									if (pswd==null) break;
								}
								if ((pswd!=null)&&(pswd.equals(password))) {
									idCounter++;
									newNote = new Note(idCounter, noteName, details.getHtmlText(), locked);
									listnotes.add(newNote);
									query.insertValue(conn, st, newNote.getId(), noteName, newNote.getNoteText(), locked);
									saved = true;
									newlyCreated = false;
									locked = false;
									showDialog(alert,0,"Note saved",null,"The note was successfully saved.");
								}
							}
						} else {
							idCounter++;
							newNote = new Note(idCounter, noteName, details.getHtmlText(), locked);
							listnotes.add(newNote);
							query.insertValue(conn, st, newNote.getId(), noteName, newNote.getNoteText(), locked);
							saved = true;
							newlyCreated = false;
							locked = false;
							showDialog(alert,0,"Note saved",null,"The note was successfully saved.");
						}
					} else {
						newNote = listnotes.get(list.getSelectionModel().getSelectedIndex());
						if (newNote.isLocked()) {
							String pswd = null;
							pswd = showPassDialog(dl,"Password",null,"Enter the password: ");
							if (pswd!=null) {
								while (pswd.equals(password)==false) {
									pswd = reEnterPassword(alert, dl,"Password",null,"Enter the password: ");
									if (pswd==null) break;
								}
								if ((pswd!=null)&&(pswd.equals(password))) {
									newNote.setNoteText(details.getHtmlText());
									newNote.setLocked(true);
									query.updateValue(conn, st, newNote.getId(), newNote.getName(), newNote.getNoteText(), newNote.isLocked());
									saved = true;
									newlyCreated = false;
									locked = false;
									showDialog(alert,0,"Note saved",null,"The note was successfully saved.");
								}
							}
						} else {
							newNote.setNoteText(details.getHtmlText());
							newNote.setLocked(false);
							query.updateValue(conn, st, newNote.getId(), newNote.getName(), newNote.getNoteText(), newNote.isLocked());
							saved = true;
							newlyCreated = false;
							locked = false;
							showDialog(alert,0,"Note saved",null,"The note was successfully saved.");
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			} else if (list.getSelectionModel().getSelectedIndex()==-1) {
				showDialog(alert,2,"Error",null,"Please select a note from the list to save.");
			} else {
				try {
					newNote = listnotes.get(list.getSelectionModel().getSelectedIndex());
					if (newNote.isLocked()) {
						String pswd = null;
						pswd = showPassDialog(dl,"Password",null,"Enter the password: ");
						if (pswd!=null) {
							while (pswd.equals(password)==false) {
								pswd = reEnterPassword(alert, dl,"Password",null,"Enter the password: ");
								if (pswd==null) break;
							}
							if ((pswd!=null)&&(pswd.equals(password))) {
								newNote.setNoteText(details.getHtmlText());
								newNote.setLocked(true);
								query.updateValue(conn, st, newNote.getId(), newNote.getName(), newNote.getNoteText(), newNote.isLocked());
								saved = true;
								newlyCreated = false;
								locked = false;
								showDialog(alert,0,"Note saved",null,"The note was successfully saved.");
							}
						}
					} else {
						newNote.setNoteText(details.getHtmlText());
						newNote.setLocked(false);
						query.updateValue(conn, st, newNote.getId(), newNote.getName(), newNote.getNoteText(), newNote.isLocked());
						saved = true;
						newlyCreated = false;
						locked = false;
						showDialog(alert,0,"Note saved",null,"The note was successfully saved.");
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		} else if (e.getSource()==delete) {
		// What happens when "Delete note" option is selected
			int selected = list.getSelectionModel().getSelectedIndex();
			Note nt = listnotes.get(selected);
			if (oblist.size()==0) {
				showDialog(alert,2,"Error",null,"The list is empty.");
			} else if (selected==oblist.size()-1) {
				if ((!saved)&&(newlyCreated)) {
					showDialog(alert,0,"Info",null,"You can't delete an unsaved note.");
				} else {
					if (nt.isLocked()) {
						String pswd = null;
						pswd = showPassDialog(dl,"Password",null,"Enter the password: ");
						if (pswd!=null) {
							while (pswd.equals(password)==false) {
								pswd = reEnterPassword(alert, dl,"Password",null,"Enter the password: ");
								if (pswd==null) break;
							}
							if ((pswd!=null)&&(pswd.equals(password))) {
								nt = listnotes.get(selected);
								listnotes.remove(selected);
								query.deleteValue(conn, st, nt.getId());
								oblist.remove(selected);
								list.setItems(oblist);
								details.setHtmlText("");
							}
						}
					} else {
						nt = listnotes.get(selected);
						listnotes.remove(selected);
						query.deleteValue(conn, st, nt.getId());
						oblist.remove(selected);
						list.setItems(oblist);
						details.setHtmlText("");
					}
				}
			} else if (selected==-1) {
				showDialog(alert,2,"Error",null,"Please select a note from the list to delete.");
			} else {
				if (nt.isLocked()) {
					String pswd = null;
					pswd = showPassDialog(dl,"Password",null,"Enter the password: ");
					if (pswd!=null) {
						while (pswd.equals(password)==false) {
							pswd = reEnterPassword(alert, dl,"Password",null,"Enter the password: ");
							if (pswd==null) break;
						}
						if ((pswd!=null)&&(pswd.equals(password))) {
							nt = listnotes.get(selected);
							listnotes.remove(selected);
							query.deleteValue(conn, st, nt.getId());
							oblist.remove(selected);
							list.setItems(oblist);
							details.setHtmlText("");
							list.getSelectionModel().clearSelection();
							ind = -1;
							ind2 = -1;
						}
					}
				} else {
					nt = listnotes.get(selected);
					listnotes.remove(selected);
					query.deleteValue(conn, st, nt.getId());
					oblist.remove(selected);
					list.setItems(oblist);
					details.setHtmlText("");
					list.getSelectionModel().clearSelection();
					ind = -1;
					ind2 = -1;
				}
			}
		} else if (e.getSource()==lock) {
		// What happens when "Lock note" option is selected
			String pswd = null;
			if (oblist.size()==0) {
				showDialog(alert,2,"Error",null,"The list is empty.");
			} else {
				int sel = list.getSelectionModel().getSelectedIndex();
				if ((!saved)&&(newlyCreated)) {
					if (locked) {
						showDialog(alert,2,"Error",null,"The note is already locked.");
					} else {
						if (password==null) {
							showDialog(alert,0,"Password info",null,"This is gonna be the password you will be using in all of the notes.\nIt can't be more than 30 characters.");
							password = showPassDialog(dl,"Password",null,"Enter the password: ");
							if (password!=null) {
								locked = true;
								query.insertPassValue(conn, password);
							}
						} else {
							pswd = showPassDialog(dl,"Password",null,"Enter the password: ");
							if (pswd!=null) {
								while (pswd.equals(password)==false) {
									pswd = reEnterPassword(alert, dl,"Password",null,"Enter the password: ");
									if (pswd==null) break;
								}
								if ((pswd!=null)&&(pswd.equals(password))) {
									locked = true;
								}
							}
						}
					}
				} else {
					Note nt = listnotes.get(sel);
					if (nt.isLocked()) {
						showDialog(alert,2,"Error",null,"The note is already locked.");
					} else {
						if (password==null) {
							showDialog(alert,0,"Password info",null,"This is gonna be the password you will be using in all of the notes.\nIt can't be more than 30 characters.");
							password = showPassDialog(dl,"Password",null,"Enter the password: ");
							if (password!=null) {
								nt.setLocked(true);
								query.insertPassValue(conn, password);
								query.updateValue(conn, st, nt.getId(), nt.getName(), nt.getNoteText(), nt.isLocked());
							}
						} else {
							pswd = showPassDialog(dl,"Password",null,"Enter the password: ");
							if (pswd!=null) {
								while (pswd.equals(password)==false) {
									pswd = reEnterPassword(alert, dl,"Password",null,"Enter the password: ");
									if (pswd==null) break;
								}
								if ((pswd!=null)&&(pswd.equals(password))) {
									nt = listnotes.get(sel);
									nt.setLocked(true);
									query.updateValue(conn, st, nt.getId(), nt.getName(), nt.getNoteText(), nt.isLocked());
								}
							}
						}
					}
				}
			}
		} else if (e.getSource()==unlock) {
		// What happens when "Unlock note" option is selected
			String pswd = null;
			if (oblist.size()==0) {
				showDialog(alert,2,"Error",null,"The list is empty.");
			} else {
				int sel = list.getSelectionModel().getSelectedIndex();
				if ((!saved)&&(newlyCreated)) {
					if (password==null) {
						showDialog(alert,2,"Error",null,"The are no locked notes.");
					} else {
						if (locked) {
							pswd = showPassDialog(dl,"Password",null,"Enter the password: ");
							if (pswd!=null) {
								while (pswd.equals(password)==false) {
									pswd = reEnterPassword(alert, dl,"Password",null,"Enter the password: ");
									if (pswd==null) break;
								}
								if ((pswd!=null)&&(pswd.equals(password))) {
									locked = false;
								}
							}
						} else {
							showDialog(alert,2,"Error",null,"The note is not locked.");
						}
					}
				} else {
					if (password==null) {
						showDialog(alert,2,"Error",null,"The are no locked notes.");
					} else {
						Note nt = listnotes.get(sel);
						if (nt.isLocked()) {
							pswd = showPassDialog(dl,"Password",null,"Enter the password: ");
							if (pswd!=null) {
								while (pswd.equals(password)==false) {
									pswd = reEnterPassword(alert, dl,"Password",null,"Enter the password: ");
									if (pswd==null) break;
								}
								if ((pswd!=null)&&(pswd.equals(password))) {
									nt.setLocked(false);
									query.updateValue(conn, st, nt.getId(), nt.getName(), nt.getNoteText(), nt.isLocked());
								}
							}
						} else {
							showDialog(alert,2,"Error",null,"The note is not locked.");
						}
					}
				}
			}
		} else if (e.getSource()==changename) {
			// What happens when "Change note's name" option is selected
			String pswd = null;
			String newName = null;
			if (oblist.size()==0) {
				showDialog(alert,2,"Error",null,"The list is empty.");
			} else {
				int sel = list.getSelectionModel().getSelectedIndex();
				if ((!saved)&&(newlyCreated)) {
					if (password==null) {
						newName = showInputDialog(dialog,"New Name",null,"Enter the note's new name: ");
						if (newName!=null) {
							noteName = newName;
							oblist.set(sel, noteName);
							list.setItems(oblist);
						}
					} else {
						if (locked) {
							pswd = showPassDialog(dl,"Password",null,"Enter the password: ");
							if (pswd!=null) {
								while (pswd.equals(password)==false) {
									pswd = reEnterPassword(alert, dl,"Password",null,"Enter the password: ");
									if (pswd==null) break;
								}
								if ((pswd!=null)&&(pswd.equals(password))) {
									newName = showInputDialog(dialog,"New Name",null,"Enter the note's new name: ");
									if (newName!=null) {
										noteName = newName;
										oblist.set(sel, noteName);
										list.setItems(oblist);
									}
								}
							}
						} else {
							newName = showInputDialog(dialog,"New Name",null,"Enter the note's new name: ");
							if (newName!=null) {
								noteName = newName;
								oblist.set(sel, noteName);
								list.setItems(oblist);
							}
						}
					}
				} else {
					Note nt = listnotes.get(sel);
					if (password==null) {
						newName = showInputDialog(dialog,"New Name",null,"Enter the note's new name: ");
						if (newName!=null) {
							nt.setName(newName);
							oblist.set(sel, nt.getName());
							list.setItems(oblist);
						}
					} else {
						nt = listnotes.get(sel);
						if (nt.isLocked()) {
							pswd = showPassDialog(dl,"Password",null,"Enter the password: ");
							if (pswd!=null) {
								while (pswd.equals(password)==false) {
									pswd = reEnterPassword(alert, dl,"Password",null,"Enter the password: ");
									if (pswd==null) break;
								}
								if ((pswd!=null)&&(pswd.equals(password))) {
									newName = showInputDialog(dialog,"New Name",null,"Enter the note's new name: ");
									nt.setName(newName);
									oblist.set(sel, nt.getName());
									list.setItems(oblist);
								}
							}
						} else {
							newName = showInputDialog(dialog,"New Name",null,"Enter the note's new name: ");
							nt.setName(newName);
							oblist.set(sel, nt.getName());
							list.setItems(oblist);
						}
					}
				}
			}
		} else if (e.getSource()==newpass) {
		// What happens when "Change password" option is selected
			String pswd = null;
			String newPass = null;
			if (password==null) {
				showDialog(alert,2,"Error",null,"There is no password.");
			} else {
				if ((!saved)&&(newlyCreated)) {
					int answer = showConfirmDialog(alert, "Warning", null, "A note is unsaved. If you change your password before saving it, the note will be deleted.\nDo you want to proceed?");
					if (answer==0) {
						list.getSelectionModel().clearSelection();
						ind = -1;
						ind2 = -1;
						oblist.remove(noteName);
						details.setHtmlText("");
						pswd = showPassDialog(dl,"Current Password",null,"Enter current password: ");
						if (pswd!=null) {
							while (pswd.equals(password)==false) {
								pswd = reEnterPassword(alert, dl,"Current Password",null,"Enter current password: ");
								if (pswd==null) break;
							}
							if ((pswd!=null)&&(pswd.equals(password))) {
								newPass = showPassDialog(dl,"New Password",null,"Enter the new password: ");
								if (newPass!=null) {
									password = newPass;
									query.updatePassValue(conn, st, password);
								}
							}
						}
					}
				} else {
					list.getSelectionModel().clearSelection();
					ind = -1;
					ind2 = -1;
					details.setHtmlText("");
					pswd = showPassDialog(dl,"Current Password",null,"Enter current password: ");
					if (pswd!=null) {
						while (pswd.equals(password)==false) {
							pswd = reEnterPassword(alert, dl,"Current Password",null,"Enter current password: ");
							if (pswd==null) break;
						}
						if ((pswd!=null)&&(pswd.equals(password))) {
							newPass = showPassDialog(dl,"New Password",null,"Enter the new password: ");
							if (newPass!=null) {
								password = newPass;
								query.updatePassValue(conn, st, password);
							}
						}
					}
				}
			}
		} else if (e.getSource()==about) {
			showDialog(alert,0,"About",null,"GNotes is a simple note-keeping app with an option to safely lock your notes.\nGNotes Version: 1.0");
		}
	}
	
	@FXML
	private void listItemClicked(MouseEvent e) {
		if (e.getSource()==list) {
			if (oblist.size()!=0) {
				ind = list.getSelectionModel().getSelectedIndex();
				if (ind2!=ind) {
					ind2 = list.getSelectionModel().getSelectedIndex();
					if (ind!=(oblist.size()-1)) {
						if ((!saved)&&(newlyCreated)) {
							int answer = showConfirmDialog(alert, "Warning", null, "A note is unsaved. If you select another note, the unsaved one will be deleted.\nDo you want to proceed?");
							String curText = details.getHtmlText();
							if (answer==0) {
								locked = false;
								details.setHtmlText("");
								if (listnotes.get(ind).isLocked()) {
									String pswd = null;
									pswd = showPassDialog(dl,"Password",null,"Enter the password: ");
									if (pswd!=null) {
										while (pswd.equals(password)==false) {
											pswd = reEnterPassword(alert, dl,"Password",null,"Enter the password: ");
											if (pswd==null) {
												list.getSelectionModel().select(oblist.size()-1);
												details.setHtmlText(curText);
												ind = -1;
												ind2 = -1;
												break;
											}
										}
										if ((pswd!=null)&&(pswd.equals(password))) {
											oblist.remove(noteName);
											list.setItems(oblist);
											details.setHtmlText(listnotes.get(ind).getNoteText());
											saved = true;
											newlyCreated = false;
										}
									} else {
										list.getSelectionModel().select(oblist.size()-1);
										details.setHtmlText(curText);
										ind = -1;
										ind2 = -1;
									}
								} else {
									oblist.remove(noteName);
									list.setItems(oblist);
									details.setHtmlText(listnotes.get(ind).getNoteText());
									saved = true;
									newlyCreated = false;
								}
							} else {
								list.getSelectionModel().select(oblist.size()-1);
								details.setHtmlText(curText);
								ind = -1;
								ind2 = -1;
							}
						} else {
							if (listnotes.get(ind).isLocked()) {
								String pswd = null;
								list.getSelectionModel().clearSelection();
								details.setHtmlText("");
								pswd = showPassDialog(dl,"Password",null,"Enter the password: ");
								if (pswd!=null) {
									while (pswd.equals(password)==false) {
										pswd = reEnterPassword(alert, dl,"Password",null,"Enter the password: ");
										if (pswd==null) {
											ind = -1;
											ind2 = -1;
											break;
										}
									}
									if ((pswd!=null)&&(pswd.equals(password))) {		
										list.getSelectionModel().select(ind);
										details.setHtmlText(listnotes.get(ind).getNoteText());
									}
								} else {
									ind = -1;
									ind2 = -1;
								}
							} else {
								details.setHtmlText(listnotes.get(ind).getNoteText());
							}
						}
					} else {
						if (((!saved)&&(newlyCreated))==false) {
							if (listnotes.get(ind).isLocked()) {
								String pswd = null;
								list.getSelectionModel().clearSelection();
								details.setHtmlText("");
								pswd = showPassDialog(dl,"Password",null,"Enter the password: ");
								if (pswd!=null) {
									while (pswd.equals(password)==false) {
										pswd = reEnterPassword(alert, dl,"Password",null,"Enter the password: ");
										if (pswd==null) {
											ind = -1;
											ind2 = -1;
											break;
										}
									}
									if ((pswd!=null)&&(pswd.equals(password))) {
										list.getSelectionModel().select(ind);
										details.setHtmlText(listnotes.get(ind).getNoteText());
									}
								} else {
									ind = -1;
									ind2 = -1;
								}
							} else {
								details.setHtmlText(listnotes.get(ind).getNoteText());
							}
						}
					}
				}
			}
		}
	}
	
	// show an information, warning, or error dialog
	private void showDialog(Alert a, int i,String title, String headerText, String content) {
		if (i==0) a = new Alert(AlertType.INFORMATION);
		else if (i==1) a = new Alert(AlertType.WARNING);
		else if (i==2) a = new Alert(AlertType.ERROR);
		a.setTitle(title);
		a.setHeaderText(headerText);
		a.setContentText(content);
		a.showAndWait();
	}
	// simple confirmation dialog with "OK" and "Cancel" options
	private int showConfirmDialog(Alert a, String title, String headerText, String content) {
		a = new Alert(AlertType.CONFIRMATION);
		a.setTitle(title);
		a.setHeaderText(headerText);
		a.setContentText(content);
		Optional<ButtonType> result = a.showAndWait();
		if (result.get()==ButtonType.OK) {
			return 0;
		} else {
			return -1;
		}
	}
	// simple input dialog
	private String showInputDialog(TextInputDialog d, String title, String headerText, String content) {
		d = new TextInputDialog();
		d.setTitle(title);
		d.setHeaderText(headerText);
		d.setContentText(content);
		Optional<String> result = d.showAndWait();
		if (result.isPresent()&&(result.get()!=null)) {
			return result.get();
		} else return null;
	}
	// simple password dialog
	private String showPassDialog(Dialog<String> d, String title, String headerText, String content) {
		d = new Dialog<String>();
		d.setTitle(title);
		d.setHeaderText(headerText);
		ButtonType okButtonType = new ButtonType("OK", ButtonData.OK_DONE);
		d.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);
		GridPane grid = new GridPane();
		PasswordField password = new PasswordField();
		grid.add(new Label(content), 0, 1);
		grid.add(password, 1, 1);
		d.getDialogPane().setContent(grid);
		Platform.runLater(() -> password.requestFocus());
		d.setResultConverter(dialogButton -> {
		    if (dialogButton == okButtonType) {
		        return new String(password.getText());
		    }
		    else return null;
		});
		Optional<String> result = d.showAndWait();
		if (result.isPresent()&&(result.get()!=null)) {
			return result.get();
		} else return null;
	}
	// re-enter password function when password is wrong
	private String reEnterPassword(Alert alert, Dialog<String> d, String title, String headerText, String content) {
		String pswd = null;
		showDialog(alert,2,"Password incorrect",null,"The password is incorrect");
		int ans = showConfirmDialog(alert, "Password incorrect", null, "Do you want to try again?");
		if (ans==0) {
			pswd = showPassDialog(d,title,headerText,content);
		} else return null;
		return pswd;
	}
	
	/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 */

	// generate salt for password hashing
	public byte[] generateSalt() {
		Random r = new SecureRandom();
		byte[] salt = new byte[20];
		r.nextBytes(salt);
		return salt;
	}

	// hash the password and return it as a String in Base64 format
	public String hashPassword(String password, byte[] salt) {
		char[] charPass = password.toCharArray();
		PBEKeySpec spec = new PBEKeySpec(charPass, salt, 65536, 512);
		try {
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
			byte[] securePass = factory.generateSecret(spec).getEncoded();
			String encodedPass = Base64.getEncoder().encodeToString(securePass);
			return encodedPass;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		} finally {
			spec.clearPassword();
		}
	}

	// use AES encryption to encrypt "plainText" using "key" (the key that should be used is the hashed password)
	public byte[] encryptText(String plainText,SecretKey key) {
		try {
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			byte[] cipherText = cipher.doFinal(plainText.getBytes());
			return cipherText;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	// decrypt an AES encrypted "cipherText" using "key" (the key that should be used is the hashed password)
	public String decryptText(byte[] cipherText,SecretKey key) {
		try {
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, key);
			String plainText = new String(cipher.doFinal(cipherText), "UTF-8");
			return plainText;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	// check if the password the user entered is correct
	public boolean verifyPassword (String password, byte[] salt, String key) {
		if (password==null) return false;
		String enteredPass = hashPassword(password, salt);
		return enteredPass.equals(key);
	}
	
	
}