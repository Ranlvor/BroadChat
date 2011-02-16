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

package de.starletp9.BroadChat.TCPServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import de.starletp9.BroadChat.Backend;
import de.starletp9.BroadChat.Backends.DirektBackend;

public class ListenThread implements Runnable {

	private Backend b;

	private TCPUserInterface ui;

	public static void main(String[] args) {
		ListenThread t = new ListenThread();
		t.ui = new TCPUserInterface();
		try {
			t.b = new DirektBackend(t.ui);
			new Thread(t).start();
			t.b.reciveLoop();
		} catch (SocketException e) {
		} catch (UnknownHostException e) {
		}
	}

	@Override
	public void run() {
		try {
			ServerSocket ss = new ServerSocket(1337);
			while (true) {
				Socket s = ss.accept();
				Connection ch = new Connection(s, b, ui);
				ui.acticeConnections.add(ch);
			}
		} catch (IOException e) {
		}
	}
}
