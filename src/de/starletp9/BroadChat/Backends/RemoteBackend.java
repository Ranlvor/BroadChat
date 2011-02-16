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

package de.starletp9.BroadChat.Backends;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import de.starletp9.BroadChat.Backend;
import de.starletp9.BroadChat.Message;
import de.starletp9.BroadChat.UI;

public class RemoteBackend implements Backend {
	private boolean goOn = true;

	private Socket socket;

	private ObjectOutputStream oos;

	private ObjectInputStream ois;

	private UI ui;

	public RemoteBackend(UI ui, String serverIP) throws UnknownHostException, IOException {
		socket = new Socket(serverIP, 1337);
		oos = new ObjectOutputStream(socket.getOutputStream());
		ois = new ObjectInputStream(socket.getInputStream());
		this.ui = ui;
	}

	@Override
	public void nicknameChanged(String oldNickname, String nickname) {
		Request r = new Request();
		r.parm1 = oldNickname;
		r.parm2 = nickname;
		r.type = 2;
		sendRequest(r);
	}

	@Override
	public void reciveLoop() {
		while (goOn) {
			try {
				Request r = (Request) ois.readObject();
				if (r.type == 1) {
					Message m = new Message();
					m.nickname = r.parm1;
					m.body = r.parm2;
					m.room = r.parm3;
					ui.MessageRecived(m);
				} else if (r.type == 2) {
					ui.nicknameChanged(r.parm1, r.parm2);
				} else if (r.type == 3) {
					ui.discoveryClientLeft(r.parm1);
				}
			} catch (IOException e) {
			} catch (ClassNotFoundException e) {
			}
		}
	}

	@Override
	public void sendMessage(String nickname, String message) throws IOException {
		Request r = new Request();
		r.parm1 = nickname;
		r.parm2 = message;
		r.type = 1;
		sendRequest(r);
	}

	@Override
	public void sendMessage(String nickname, String message, String room) throws IOException {
		Request r = new Request();
		r.parm1 = nickname;
		r.parm2 = message;
		r.parm3 = room;
		r.type = 1;
		sendRequest(r);
	}

	@Override
	public void sendShutdownAnnouncement(String nickname) {
		Request r = new Request();
		r.parm1 = nickname;
		r.type = 3;
		sendRequest(r);
	}

	@Override
	public void shutdown(String nickname) {
		goOn = false;
		sendShutdownAnnouncement(nickname);
		try {
			socket.close();
		} catch (IOException e) {
		}
	}

	public void sendRequest(Request r) {
		try {
			oos.writeObject(r);
		} catch (IOException e) {
		}
	}
}
