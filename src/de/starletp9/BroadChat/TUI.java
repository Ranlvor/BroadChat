package de.starletp9.BroadChat;

import java.io.Console;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

public class TUI implements UI {
	private Backend b;

	private String nickname;

	private static Console c = System.console();

	@Override
	public void MessageRecived(Message m) {
		System.out.print("\n" + m.nickname + ": " + m.body + "\n" + this.nickname + "> ");
	}

	/**
	 * @param args
	 * @throws UnknownHostException
	 * @throws SocketException
	 */
	public static void main(String[] args) throws SocketException, UnknownHostException {
		final TUI tui = new TUI();
		System.out.print("Nickname: ");
		tui.nickname = c.readLine();
		Backend b = new Backend(tui);
		tui.b = b;
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				tui.b.reciveLoop();
			}
		});
		t.setDaemon(true);
		t.start();
		System.out.print(tui.nickname + "> ");
		while (true) {
			String zeile = c.readLine();
			if (zeile != null)
				if (zeile.startsWith("/")) {
					if (zeile.startsWith("/say "))
						try {
							tui.b.sendMessage(tui.nickname, zeile.substring(5));
						} catch (IOException e) {
							System.out.println("Fehler beim Senden!");
						}
					else if (zeile.startsWith("/quit"))
						System.exit(0);
				} else
					try {
						tui.b.sendMessage(tui.nickname, zeile);
					} catch (IOException e) {
						System.out.println("Fehler beim Senden!");
					}
		}
	}
}
