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
					if ((r != null) && !r.red) {
						r.red = true;
						tabbedPane.setTitleAt(tabbedPane.indexOfComponent(r.splitPane), r.name);
					}
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
		Room r = new Room();
		r.name = name;
		rooms.put(name, r);
		final JSplitPane p = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		r.splitPane = p;
		tabbedPane.addTab(name, p);
		JLabel chatLable = new JLabel();
		r.chatLable = chatLable;
		JScrollPane scrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setViewportView(chatLable);
		r.scrollPane = scrollPane;
		p.add(scrollPane);

		final JTextField message = new JTextField();
		p.add(message);
		message.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				String text = message.getText();
				if (text.equals("/clear")) {
					rooms.get(name).chatText.delete(0, rooms.get(name).chatText.length());
					update(name);
					message.setText("");
				} else if (text.equals("/close")) {
					rooms.remove(name);
					tabbedPane.remove(p);
				} else if (text.equals("/help")) {
					rooms.get(name).chatText.append("<br>/clear: Löscht alle Nachrichten dieses Fensters" + "<br>/close: Schließt dieses Fenster"
							+ "<br>/changelog: Zeigt das Changelog dieses Programms an");
					update(name);
					message.setText("");
				} else if (text.equals("/changelog")) {
					message.setText("");
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
								rooms.get(name).chatText.append("<br>"+line);
								line = bis.readLine();
							}
						} catch (IOException e) {
							rooms.get(name).chatText.append("Fehler beim Laden des Changelogs<br>");
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
						rooms.get(name).chatText.append("Fehler beim Laden des Changelogs<br>");
					}
					update(name);
				} else {
					try {
						b.sendMessage(nickname.getText(), text, name);
						message.setText("");
					} catch (IOException e) {
						rooms.get(name).chatText.append("<br>Fehler beim Senden der Nachricht");
						update(name);
					}
				}
			}
		});
		p.setResizeWeight(1);
		return r;
	}

	public void MessageRecived(Message m) {
		if (debug)
			System.out.println("Nachricht von " + m.nickname + " auf Channel " + m.room + " erhalten: " + m.body);
		Room r = rooms.get(m.room);
		if (r == null)
			r = createNewRoomTab(m.room);
		r.chatText.append("<br>" + m.nickname + ": " + m.body);
		update(m.room);
		if (r.red && (tabbedPane.indexOfComponent(r.splitPane) != tabbedPane.getSelectedIndex())) {
			tabbedPane.setTitleAt(tabbedPane.indexOfComponent(r.splitPane), "* " + r.name);
			r.red = false;
		}
	}

	public static void update(String room) {
		Room r = rooms.get(room);
		if (r != null) {
			r.chatLable.setText("<html>" + r.chatText + "</html>");
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
		rooms.get(BackendXMLStrings.defaultRoomName).chatText.append("<br>" + nickname + " hat seinen Client beendet.");
		update(BackendXMLStrings.defaultRoomName);
	}

	public void nicknameChanged(String oldNickname, String newNickname) {
		rooms.get(BackendXMLStrings.defaultRoomName).chatText.append("<br>" + oldNickname + " hat seinen Namen in \"" + newNickname + "\" geändert.");
		update(BackendXMLStrings.defaultRoomName);
	}
}
