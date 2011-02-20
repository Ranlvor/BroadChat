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

import java.io.Console;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import de.starletp9.BroadChat.Backends.DirectBackend;

public class TUI extends UI {
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
		Backend b = new DirectBackend(tui);
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
					else
						System.out.println("Unbekannter Befehl, um Nachrichten, die mit / beginnen zu posten, versuchen sie es mit /say...");
				} else
					try {
						tui.b.sendMessage(tui.nickname, zeile);
					} catch (IOException e) {
						System.out.println("Fehler beim Senden!");
					}
		}
	}
}
