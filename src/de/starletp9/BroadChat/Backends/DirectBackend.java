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
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import de.starletp9.BroadChat.Backend;
import de.starletp9.BroadChat.Message;
import de.starletp9.BroadChat.UI;

public class DirectBackend implements Backend {

	public static final int defaultPort = 1337;

	public static final String defaultReciverIP = "255.255.255.255";

	public static final boolean debug = true;

	public static SAXBuilder saxBuilder = null;

	public int port = defaultPort;

	public InetAddress reciver = null;

	public DatagramSocket socket = null;

	public UI ui;

	private volatile boolean goOn = true;

	public DirectBackend(UI ui) throws SocketException, UnknownHostException {
		if (saxBuilder == null)
			saxBuilder = new SAXBuilder();
		reciver = InetAddress.getByName(defaultReciverIP);
		socket = new DatagramSocket(defaultPort);
		this.ui = ui;
	}

	public DirectBackend(UI ui, int port) throws SocketException, UnknownHostException {
		if (saxBuilder == null)
			saxBuilder = new SAXBuilder();
		reciver = InetAddress.getByName(defaultReciverIP);
		socket = new DatagramSocket(port);
		this.ui = ui;
	}

	public DirectBackend(UI ui, String reciverIP) throws SocketException, UnknownHostException {
		if (saxBuilder == null)
			saxBuilder = new SAXBuilder();
		reciver = InetAddress.getByName(reciverIP);
		socket = new DatagramSocket(defaultPort);
		this.ui = ui;
	}

	public DirectBackend(UI ui, String reciverIP, int port) throws SocketException, UnknownHostException {
		if (saxBuilder == null)
			saxBuilder = new SAXBuilder();
		reciver = InetAddress.getByName(reciverIP);
		socket = new DatagramSocket(port);
		this.ui = ui;
	}

	public void sendMessage(String nickname, String message) throws IOException {
		sendMessage(nickname, message, DirectBackendXMLStrings.defaultRoomName);
	}

	public void sendMessage(String nickname, String message, String room) throws IOException {
		Element root = new Element(DirectBackendXMLStrings.messageRootElement);
		if (room.equals(DirectBackendXMLStrings.defaultRoomName))
			root.setAttribute(DirectBackendXMLStrings.version, "1");
		else
			root.setAttribute(DirectBackendXMLStrings.version, "2");
		root.addContent(new Element(DirectBackendXMLStrings.messageNickname).setText(nickname));
		root.addContent(new Element(DirectBackendXMLStrings.messageBody).setText(message));
		if (!room.equals(DirectBackendXMLStrings.defaultRoomName))
			root.addContent(new Element(DirectBackendXMLStrings.roomElement).setNamespace(Namespace.getNamespace(DirectBackendXMLStrings.roomNamespace)).setText(room));
		sendElement(root);
	}

	public void reciveLoop() {
		byte[] data = new byte[1500];
		while (goOn) {
			DatagramPacket packet = new DatagramPacket(data, data.length);
			try {
				socket.receive(packet);
				try {
					String dString = new String(packet.getData()).trim();
					if (debug)
						System.out.println("Empfange: " + dString);
					Document d = saxBuilder.build(new StringReader(dString));
					Element rootElement = d.getRootElement();
					int paketVersion = Integer.parseInt(rootElement.getAttributeValue(DirectBackendXMLStrings.version));
					if (paketVersion == 1) {
						Message m = new Message();
						m.nickname = rootElement.getChildText(DirectBackendXMLStrings.messageNickname);
						m.body = rootElement.getChildText(DirectBackendXMLStrings.messageBody);
						m.room = DirectBackendXMLStrings.defaultRoomName;
						if ((m.nickname != null) && (m.body != null))
							ui.MessageRecived(m);
						else if (debug)
							System.out.println("Paket verworfen, da Pakete der Version 1 einen Nickname und einen Body enthalten MÜSSEN");
					} else if (paketVersion == 2) {
						Element element = rootElement.getChild(DirectBackendXMLStrings.discoveryClientLeft, Namespace.getNamespace(DirectBackendXMLStrings.discoveryNamespace));
						if (element != null) {
							String nickname = rootElement.getChildText(DirectBackendXMLStrings.messageNickname);
							if (nickname != null)
								ui.discoveryClientLeft(nickname);
							else if (debug)
								System.out.println("Discovery-ClientLeft-Paket verworfen wegen fehlendem Nickname!");
						} else if ((element = rootElement
								.getChild(DirectBackendXMLStrings.discoveryNicknameChanged, Namespace.getNamespace(DirectBackendXMLStrings.discoveryNamespace))) != null) {
							String nickname = rootElement.getChildText(DirectBackendXMLStrings.messageNickname);
							String oldNickname = element.getText();
							if (nickname != null && oldNickname != null)
								ui.nicknameChanged(oldNickname, nickname);
							else if (debug)
								System.out.println("Discovery-NicknameChanged-Paket verworfen wegen fehlendem Neuem oder Altem Nickname!");
						} else { // kein Discovery-Element, das könnte ne ganz normale Nachricht sein, eventuell
							// mit Raumelement
							Message m = new Message();
							m.nickname = rootElement.getChildText(DirectBackendXMLStrings.messageNickname);
							m.body = rootElement.getChildText(DirectBackendXMLStrings.messageBody);
							m.room = rootElement.getChildText(DirectBackendXMLStrings.roomElement, Namespace.getNamespace(DirectBackendXMLStrings.roomNamespace));
							if (m.room == null)
								m.room = DirectBackendXMLStrings.defaultRoomName;
							if ((m.nickname != null) && (m.body != null))
								ui.MessageRecived(m);
						}
					} else if (debug)
						System.out.println("Paket verworfen wegen einer unbekannten Version");
				} catch (JDOMException e) {
					// Nichts machen -> Paket verwerfen
					if (debug)
						System.out.println("Paket verworfen wegen einer JDOMException");
				} catch (NumberFormatException e) {
					// Nichts machen -> Paket verwerfen
					if (debug)
						System.out.println("Paket verworfen wegen einer NumberFormatException");
				}
			} catch (IOException e) {
				if (debug)
					System.out.println("IOException, verlasse die reciveLoop!");
				break;
			}
			for (int i = 0; i < data.length; i++) {
				data[i] = 0;
			}
		}
	}

	public void shutdown(String nickname) {
		sendShutdownAnnouncement(nickname);
		goOn = false;

	}

	public void sendShutdownAnnouncement(String nickname) {
		Element root = new Element(DirectBackendXMLStrings.messageRootElement);
		root.setAttribute(DirectBackendXMLStrings.version, "2");
		root.addContent(new Element(DirectBackendXMLStrings.messageNickname).setText(nickname));
		root.addContent(new Element(DirectBackendXMLStrings.discoveryClientLeft).setNamespace(Namespace.getNamespace(DirectBackendXMLStrings.discoveryNamespace)));
		sendElement(root);
	}

	public void nicknameChanged(String oldNickname, String nickname) {
		Element root = new Element(DirectBackendXMLStrings.messageRootElement);
		root.setAttribute(DirectBackendXMLStrings.version, "2");
		root.addContent(new Element(DirectBackendXMLStrings.messageNickname).setText(nickname));
		root.addContent(new Element(DirectBackendXMLStrings.discoveryNicknameChanged).setNamespace(Namespace.getNamespace(DirectBackendXMLStrings.discoveryNamespace)).setText(
				oldNickname));
		sendElement(root);
	}

	private void sendElement(Element root) {
		Document doc = new Document(root);
		XMLOutputter out;
		if (debug)
			out = new XMLOutputter(Format.getPrettyFormat());
		else
			out = new XMLOutputter();
		String outputString = out.outputString(doc);
		if (debug)
			System.out.println("Sende: " + outputString);
		byte[] outputBytes = outputString.getBytes();
		try {
			socket.send(new DatagramPacket(outputBytes, outputBytes.length, reciver, port));
		} catch (UnknownHostException e) {
		} catch (IOException e) {
		}
	}
}
