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

import de.starletp9.BroadChat.Backend;
import de.starletp9.BroadChat.Backends.Request;

public class InputThread implements Runnable {
	private ObjectInputStream ois;

	private Backend b;

	private Connection c;

	public boolean goOn = true;

	@Override
	public void run() {
		while (goOn) {
			try {
				Request r = (Request) ois.readObject();
				if (r.type == 1) {
					if (r.parm3 == null)
						b.sendMessage(r.parm1, r.parm2);
					else
						b.sendMessage(r.parm1, r.parm2, r.parm3);
				} else if (r.type == 2) {
					b.nicknameChanged(r.parm1, r.parm2);
				} else if (r.type == 3) {
					b.sendShutdownAnnouncement(r.parm1);
				}
			} catch (IOException e) {
				c.close();
			} catch (ClassNotFoundException e) {
				c.close();
			}
		}
	}

	public InputThread(ObjectInputStream ois, Backend b, Connection c) {
		this.ois = ois;
		this.b = b;
		this.c = c;
	}

}
