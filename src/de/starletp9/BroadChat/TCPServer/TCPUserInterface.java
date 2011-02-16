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

import java.util.ArrayList;

import de.starletp9.BroadChat.Message;
import de.starletp9.BroadChat.UI;
import de.starletp9.BroadChat.Backends.Request;

public class TCPUserInterface extends UI {
	public ArrayList<Connection> acticeConnections = new ArrayList<Connection>();

	@Override
	public void MessageRecived(Message m) {
		Request r = new Request();
		r.type = 1;
		r.parm1 = m.nickname;
		r.parm2 = m.body;
		r.parm3 = m.room;
		sendToAllConnections(r);
	}

	@Override
	public void nicknameChanged(String oldNickname, String newNickname) {
		Request r = new Request();
		r.type = 2;
		r.parm1 = oldNickname;
		r.parm2 = newNickname;
		sendToAllConnections(r);
	}

	@Override
	public void discoveryClientLeft(String nickname) {
		Request r = new Request();
		r.type = 3;
		r.parm1 = nickname;
		sendToAllConnections(r);
	}

	public void sendToAllConnections(Request r) {
		for (Connection con : acticeConnections)
			con.sendRequest(r);
	}
}
