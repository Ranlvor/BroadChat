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

public class Backend {

	public static final int defaultPort = 1337;

	public static final String defaultReciverIP = "255.255.255.255";

	public static final boolean debug = true;

	public static SAXBuilder saxBuilder = null;

	public int port = defaultPort;

	public InetAddress reciver = null;

	public DatagramSocket socket = null;

	public UI ui;

	private volatile boolean goOn = true;

	public Backend(UI ui) throws SocketException, UnknownHostException {
		if (saxBuilder == null)
			saxBuilder = new SAXBuilder();
		reciver = InetAddress.getByName(defaultReciverIP);
		socket = new DatagramSocket(defaultPort);
		this.ui = ui;
	}

	public Backend(UI ui, int port) throws SocketException, UnknownHostException {
		if (saxBuilder == null)
			saxBuilder = new SAXBuilder();
		reciver = InetAddress.getByName(defaultReciverIP);
		socket = new DatagramSocket(port);
		this.ui = ui;
	}

	public Backend(UI ui, String reciverIP) throws SocketException, UnknownHostException {
		if (saxBuilder == null)
			saxBuilder = new SAXBuilder();
		reciver = InetAddress.getByName(reciverIP);
		socket = new DatagramSocket(defaultPort);
		this.ui = ui;
	}

	public Backend(UI ui, String reciverIP, int port) throws SocketException, UnknownHostException {
		if (saxBuilder == null)
			saxBuilder = new SAXBuilder();
		reciver = InetAddress.getByName(reciverIP);
		socket = new DatagramSocket(port);
		this.ui = ui;
	}

	public void sendMessage(String nickname, String message) throws IOException {
		Element root = new Element(BackendXMLStrings.messageRootElement);
		root.setAttribute(BackendXMLStrings.version, "1");
		root.addContent(new Element(BackendXMLStrings.messageNickname).setText(nickname));
		root.addContent(new Element(BackendXMLStrings.messageBody).setText(message));
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
					int paketVersion = Integer.parseInt(rootElement.getAttributeValue(BackendXMLStrings.version));
					if (paketVersion == 1) {
						Message m = new Message();
						m.nickname = rootElement.getChildText(BackendXMLStrings.messageNickname);
						m.body = rootElement.getChildText(BackendXMLStrings.messageBody);
						if ((m.nickname != null) && (m.body != null))
							ui.MessageRecived(m);
						else if (debug)
							System.out.println("Paket verworfen, da Pakete der Version 1 einen Nickname und einen Body enthalten MÜSSEN");
					} else if (paketVersion == 2) {
						Element discoveryElement = rootElement
								.getChild(BackendXMLStrings.discoveryClientLeft, Namespace.getNamespace(BackendXMLStrings.discoveryNamespace));
						if (discoveryElement != null) {
							String nickname = rootElement.getChildText(BackendXMLStrings.messageNickname);
							if (nickname != null)
								ui.discoveryClientLeft(nickname);
							else if (debug)
								System.out.println("Discovery-ClientLeft-Paket verworfen wegen fehlendem Nickname!");
						} else {
							discoveryElement = rootElement.getChild(BackendXMLStrings.discoveryNicknameChanged, Namespace.getNamespace(BackendXMLStrings.discoveryNamespace));
							if (discoveryElement != null) {
								String nickname = rootElement.getChildText(BackendXMLStrings.messageNickname);
								String oldNickname = discoveryElement.getText();
								if (nickname != null && oldNickname != null)
									ui.nicknameChanged(oldNickname, nickname);
								else if (debug)
									System.out.println("Discovery-NicknameChanged-Paket verworfen wegen fehlendem Neuem oder Altem Nickname!");
							}
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
		Element root = new Element(BackendXMLStrings.messageRootElement);
		root.setAttribute(BackendXMLStrings.version, "2");
		root.addContent(new Element(BackendXMLStrings.messageNickname).setText(nickname));
		root.addContent(new Element(BackendXMLStrings.discoveryClientLeft).setNamespace(Namespace.getNamespace(BackendXMLStrings.discoveryNamespace)));
		goOn = false;
		sendElement(root);
	}

	public void nicknameChanged(String oldNickname, String nickname) {
		Element root = new Element(BackendXMLStrings.messageRootElement);
		root.setAttribute(BackendXMLStrings.version, "2");
		root.addContent(new Element(BackendXMLStrings.messageNickname).setText(nickname));
		root.addContent(new Element(BackendXMLStrings.discoveryNicknameChanged).setNamespace(Namespace.getNamespace(BackendXMLStrings.discoveryNamespace)).setText(
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
