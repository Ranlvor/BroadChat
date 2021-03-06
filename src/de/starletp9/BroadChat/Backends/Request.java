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

import java.io.Serializable;

public class Request implements Serializable {
	private static final long serialVersionUID = -9137974320810386142L;

	public int type = 0; /*
	0 = ignored
	1 = Message
	2 = Nickname changed
	3 = shutdown announcement
	*/
	
	public String parm1, parm2, parm3;
}
