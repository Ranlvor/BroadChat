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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import de.starletp9.BroadChat.Backend;
import de.starletp9.BroadChat.Backends.Request;

public class Connection {
	private ObjectOutputStream oos;

	private ObjectInputStream ois;

	private Thread t;

	private InputThread it;

	private TCPUserInterface ui;

	private Socket s;

	public Connection(Socket s, Backend b, TCPUserInterface ui) {
		try {
			this.ui = ui;
			this.s = s;
			oos = new ObjectOutputStream(s.getOutputStream());
			ois = new ObjectInputStream(s.getInputStream());
			it = new InputThread(ois, b, this);
			t = new Thread(it);
			t.start();
		} catch (IOException e) {
			close();
		}
	}

	public void sendRequest(Request r) {
		try {
			oos.writeObject(r);
		} catch (IOException e) {
			close();
		}
	}

	public void close() {
		ui.acticeConnections.remove(this);
		it.goOn = false;
		try {
			s.close();
		} catch (IOException e) {
		}
	}
}
