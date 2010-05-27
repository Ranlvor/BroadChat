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
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class Backend {

	public static final int version = 1;

	public static final int defaultPort = 1337;

	public static final String defaultReciverIP = "255.255.255.255";

	public static SAXBuilder saxBuilder = null;

	public int port = defaultPort;

	public InetAddress reciver = null;

	public DatagramSocket socket = null;

	public Backend() throws SocketException, UnknownHostException {
		if (saxBuilder == null)
			saxBuilder = new SAXBuilder();
		reciver = InetAddress.getByName(defaultReciverIP);
		socket = new DatagramSocket(defaultPort);
	}

	public Backend(int port) throws SocketException, UnknownHostException {
		if (saxBuilder == null)
			saxBuilder = new SAXBuilder();
		reciver = InetAddress.getByName(defaultReciverIP);
		socket = new DatagramSocket(port);
	}

	public Backend(String reciverIP) throws SocketException, UnknownHostException {
		if (saxBuilder == null)
			saxBuilder = new SAXBuilder();
		reciver = InetAddress.getByName(reciverIP);
		socket = new DatagramSocket(defaultPort);
	}

	public Backend(String reciverIP, int port) throws SocketException, UnknownHostException {
		if (saxBuilder == null)
			saxBuilder = new SAXBuilder();
		reciver = InetAddress.getByName(reciverIP);
		socket = new DatagramSocket(port);
	}

	public void sendMessage(String nickname, String message) throws IOException {
		Element root = new Element(BackendXMLStrings.messageRootElement);
		root.setAttribute(BackendXMLStrings.version, "" + version);
		root.addContent(new Element(BackendXMLStrings.messageNickname).setText(nickname));
		root.addContent(new Element(BackendXMLStrings.messageBody).setText(message));

		Document doc = new Document(root);
		XMLOutputter out;
		if (GUI.debug)
			out = new XMLOutputter(Format.getPrettyFormat());
		else
			out = new XMLOutputter();
		String outputString = out.outputString(doc);
		if (GUI.debug)
			System.out.println("Sende: " + outputString);
		byte[] outputBytes = outputString.getBytes();
		socket.send(new DatagramPacket(outputBytes, outputBytes.length, reciver, port));
	}

	public void reciveLoop() {
		byte[] data = new byte[1500];
		while (true) {
			DatagramPacket packet = new DatagramPacket(data, data.length);
			try {
				socket.receive(packet);
				try {
					String dString = new String(packet.getData()).trim();
					if (GUI.debug)
						System.out.println("Empfange: " + dString);
					Document d = saxBuilder.build(new StringReader(dString));
					Element rootElement = d.getRootElement();
					if (!(Integer.parseInt(rootElement.getAttributeValue(BackendXMLStrings.version)) > version)) {
						Message m = new Message();
						m.nickname = rootElement.getChildText(BackendXMLStrings.messageNickname);
						m.body = rootElement.getChildText(BackendXMLStrings.messageBody);
						GUI.MessageRecived(m);
					} else if (GUI.debug)
						System.out.println("Paket verworfen wegen einer zu hohen Version");
				} catch (JDOMException e) {
					// Nichts machen -> Paket verwerfen
					if (GUI.debug)
						System.out.println("Paket verworfen wegen einer JDOMException");
				} catch (NumberFormatException e) {
					// Nichts machen -> Paket verwerfen
					if (GUI.debug)
						System.out.println("Paket verworfen wegen einer NumberFormatException");
				}
			} catch (IOException e) {
				if (GUI.debug)
					System.out.println("IOException, verlasse die reciveLoop!");
				break;
			}
			for (int i = 0; i < data.length; i++) {
				data[i] = 0;
			}
		}
	}
}
