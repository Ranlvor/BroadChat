package de.starletp9.BroadChat;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.jdom.JDOMException;

public class GUI {

	public static boolean debug = true;

	public static JLabel chatLable = null;

	public static String JLableString = "";

	public static Backend b;
	public static void main(String[] args) throws IOException, JDOMException {
		b = new Backend();
		JFrame f = new JFrame("BroadChat");
		Box box = new Box(BoxLayout.Y_AXIS);
		f.add(box);
		chatLable = new JLabel();
		box.add(chatLable);
		final JTextField nickname = new JTextField("Nickname");
		final JTextField message = new JTextField();
		box.add(message);
		box.add(nickname);
		message.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					b.sendMessage(nickname.getText(), message.getText());
					message.setText("");
				} catch (IOException e) {
					JLableString = JLableString+"<br>Fehler beim Senden der Nachricht";
					update();
				}
			}
		});
		f.setSize(500, 600);
		f.setVisible(true);
		
		b.sendMessage("test", "testtsettest");
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				b.reciveLoop();
			}
		});
		t.setDaemon(true);
		t.start();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		b.sendMessage("bla", "testtsettesttest");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		b.sendMessage("blablabla", "laber");
	}

	public static void MessageRecived(Message m) {
		if (debug)
			System.out.println("Nachricht von " + m.nickname + " erhalten: " + m.body);
		JLableString = JLableString + "<br>" + m.nickname + ": " + m.body;
		update();
	}
	
	public static void update(){
		chatLable.setText("<html>" + JLableString + "</html>");
	}
}
