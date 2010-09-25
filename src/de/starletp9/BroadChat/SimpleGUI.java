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
import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.jdom.JDOMException;

public class SimpleGUI extends UI {

	public static boolean debug = false;

	public static JLabel chatLable = null;

	public static StringBuilder JLableString = new StringBuilder();

	public static Backend b;

	public static String oldNickname = "Nickname";

	public static JFrame f;

	public static void main(String[] args) throws IOException, JDOMException {
		b = new Backend(new SimpleGUI());
		f = new JFrame("BroadChat");
		Box box = new Box(BoxLayout.Y_AXIS);
		f.add(box);
		chatLable = new JLabel();
		box.add(chatLable);
		final JTextField nickname = new JTextField(oldNickname);
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
		final JTextField message = new JTextField();
		box.add(message);
		box.add(nickname);
		message.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				String text = message.getText();
				if (text.equals("/clear")) {
					JLableString.delete(0, JLableString.length());
					update();
				} else {
					try {
						b.sendMessage(nickname.getText(), text);
						message.setText("");
					} catch (IOException e) {
						JLableString.append("<br>Fehler beim Senden der Nachricht");
						update();
					}
				}
			}
		});
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

	public void MessageRecived(Message m) {
		if (debug)
			System.out.println("Nachricht von " + m.nickname + " erhalten: " + m.body);
		JLableString.append("<br>" + m.nickname + ": " + m.body);
		update();
	}

	public static void update() {
		chatLable.setText("<html>" + JLableString + "</html>");
		f.toFront();
	}

	public void discoveryClientLeft(String nickname) {
		JLableString.append("<br>" + nickname + " hat seinen Client beendet.");
		update();
	}

	public void nicknameChanged(String oldNickname, String newNickname) {
		JLableString.append("<br>" + oldNickname + " hat seinen Namen in \"" + newNickname + "\" geändert.");
		update();
	}
}
