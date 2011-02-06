/*

    This file is part of BroadChat.

    BroadChat is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    BroadChat is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with BroadChat.  If not, see <http://www.gnu.org/licenses/>.

 */

package de.starletp9.BroadChat;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdom.JDOMException;

public class SimpleGUI extends UI {
	public static JTabbedPane tabbedPane;

	public static boolean debug = true;

	public static Backend b;

	public static String oldNickname = "Nickname";

	public static JFrame f;

	public static HashMap<String, Room> rooms = new HashMap<String, Room>();

	public static JTextField nickname;

	public static void main(String[] args) throws IOException, JDOMException {
		b = new Backend(new SimpleGUI());
		tabbedPane = new JTabbedPane();
		tabbedPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				String title = tabbedPane.getTitleAt(tabbedPane.getSelectedIndex());
				if (title.length() >= 2) {
					Room r = rooms.get(title.substring(2));
					if (r != null) {
						r.messageTextField.grabFocus();
						if (!r.red) {
							r.red = true;
							tabbedPane.setTitleAt(tabbedPane.indexOfComponent(r.splitPane), r.name);
						}
					} else if ((r = rooms.get(title)) != null)
						r.messageTextField.grabFocus();
				}
			}
		});
		f = new JFrame("BroadChat");
		f.setSize(500, 600);
		f.addWindowListener(new WindowListener() {

			@Override
			public void windowOpened(WindowEvent arg0) {
			}

			@Override
			public void windowIconified(WindowEvent arg0) {
			}

			@Override
			public void windowDeiconified(WindowEvent arg0) {
			}

			@Override
			public void windowDeactivated(WindowEvent arg0) {
			}

			@Override
			public void windowClosing(WindowEvent arg0) {
				b.shutdown(nickname.getText());
				System.exit(0);
			}

			@Override
			public void windowClosed(WindowEvent arg0) {
			}

			@Override
			public void windowActivated(WindowEvent arg0) {
			}
		});
		nickname = new JTextField(oldNickname);
		nickname.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent arg0) {
				String newNickname = nickname.getText();
				if (!oldNickname.equals(newNickname)) {
					b.nicknameChanged(oldNickname, newNickname);
					oldNickname = newNickname;
				}
			}

			@Override
			public void focusGained(FocusEvent arg0) {
			}
		});
		Box newRoom = new Box(BoxLayout.Y_AXIS);
		JPanel newRoomPanel = new JPanel();
		newRoomPanel.add(newRoom);
		tabbedPane.addTab("+", newRoomPanel);
		newRoom.add(new JLabel("              Name:              "));
		final JTextField roomName = new JTextField();
		newRoom.add(roomName);
		roomName.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				createNewRoomTab(roomName.getText());
				tabbedPane.setSelectedIndex(tabbedPane.indexOfTab(roomName.getText()));
				roomName.setText("");
			}
		});
		createNewRoomTab(BackendXMLStrings.defaultRoomName);
		tabbedPane.setSelectedIndex(1);
		JSplitPane p = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		p.add(tabbedPane);
		p.add(nickname);
		p.setResizeWeight(1);
		f.add(p);
		f.setVisible(true);
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				b.reciveLoop();
			}
		});
		t.setName("Backend");
		t.setDaemon(true);
		t.start();
	}

	public static Room createNewRoomTab(final String name) {
		final Room r = new Room();
		r.name = name;
		rooms.put(name, r);
		r.splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		tabbedPane.addTab(name, r.splitPane);
		r.chatLable = new JTextArea();
		r.chatLable.setEditable(false);
		r.scrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		r.scrollPane.setViewportView(r.chatLable);
		r.splitPane.add(r.scrollPane);

		r.messageTextField = new JTextField();
		r.splitPane.add(r.messageTextField);
		r.messageTextField.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == 17) {
					int newIndex = tabbedPane.getSelectedIndex() + 1;
					if (newIndex >= tabbedPane.getComponentCount())
						newIndex = 1;
					tabbedPane.setSelectedIndex(newIndex);
				}
			}
		});
		r.messageTextField.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				String text = r.messageTextField.getText();
				if (text.equals("/clear")) {
					rooms.get(name).chatText.delete(0, rooms.get(name).chatText.length());
					update(name);
					r.messageTextField.setText("");
				} else if (text.equals("/close")) {
					rooms.remove(name);
					tabbedPane.remove(r.splitPane);
				} else if (text.equals("/help")) {
					rooms.get(name).chatText.append("\n/clear: Löscht alle Nachrichten dieses Fensters" + "\n/close: Schließt dieses Fenster"
							+ "\n/changelog: Zeigt das Changelog dieses Programms an");
					update(name);
					r.messageTextField.setText("");
				} else if (text.equals("/changelog")) {
					r.messageTextField.setText("");
					URL url = ClassLoader.getSystemResource("changelog.txt");
					System.out.println(url);
					if (url != null) {
						InputStream is = null;
						BufferedReader bis = null;
						InputStreamReader isr = null;
						try {
							is = url.openStream();
							isr = new InputStreamReader(is);
							bis = new BufferedReader(isr);
							String line = bis.readLine();
							while (line != null) {
								rooms.get(name).chatText.append("\n" + line);
								line = bis.readLine();
							}
						} catch (IOException e) {
							rooms.get(name).chatText.append("\nFehler beim Laden des Changelogs");
						} finally {
							if (bis != null)
								try {
									bis.close();
								} catch (IOException e) {
								}
							if (isr != null)
								try {
									isr.close();
								} catch (IOException e) {
								}
							if (is != null)
								try {
									is.close();
								} catch (IOException e) {
								}
						}
					} else {
						rooms.get(name).chatText.append("\nFehler beim Laden des Changelogs");
					}
					update(name);
				} else {
					try {
						b.sendMessage(nickname.getText(), text, name);
						r.messageTextField.setText("");
					} catch (IOException e) {
						rooms.get(name).chatText.append("\nFehler beim Senden der Nachricht");
						update(name);
					}
				}
			}
		});
		r.splitPane.setResizeWeight(1);
		return r;
	}

	public void MessageRecived(Message m) {
		if (debug)
			System.out.println("Nachricht von " + m.nickname + " auf Channel " + m.room + " erhalten: " + m.body);
		Room r = rooms.get(m.room);
		if (r == null)
			r = createNewRoomTab(m.room);
		r.chatText.append("\n" + m.nickname + ": " + m.body);
		update(m.room);
		if (r.red && (tabbedPane.indexOfComponent(r.splitPane) != tabbedPane.getSelectedIndex())) {
			tabbedPane.setTitleAt(tabbedPane.indexOfComponent(r.splitPane), "* " + r.name);
			r.red = false;
		}
	}

	public static void update(String room) {
		Room r = rooms.get(room);
		if (r != null) {
			r.chatLable.setText(r.chatText.toString());
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
			}
			JScrollBar bar = r.scrollPane.getVerticalScrollBar();
			bar.setValue(bar.getMaximum());
			f.toFront();
		}
	}

	public void discoveryClientLeft(String nickname) {
		rooms.get(BackendXMLStrings.defaultRoomName).chatText.append("\n" + nickname + " hat seinen Client beendet.");
		update(BackendXMLStrings.defaultRoomName);
	}

	public void nicknameChanged(String oldNickname, String newNickname) {
		rooms.get(BackendXMLStrings.defaultRoomName).chatText.append("\n" + oldNickname + " hat seinen Namen in \"" + newNickname + "\" geändert.");
		update(BackendXMLStrings.defaultRoomName);
	}
}
