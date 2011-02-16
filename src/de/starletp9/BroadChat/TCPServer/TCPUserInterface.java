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
		for (Connection con : acticeConnections) {
			con.sendRequest(r);
		}
	}
}
