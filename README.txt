Hallo,
diese Datei gehört zu den Projekt "BroadChat". Du hast sie entweder dadurch bekommen das du eine ausführbar zusammengepackte Version dieses Projekts ausgepackt hast oder den Sourcecode des Projekts heruntergeladen hast.
Den aktuellen Sourcecode des Projektes findest du unter

    https://github.com/keine-ahnung/BroadChat

Falls du einen Bug gefunden hast oder einen Verbesserungsvorschlag für das Projekt hast fühle dich so frei diesen unter

    http://starletp9.de/issuetracker/index.php?project=1

zu melden.

Zum Kompilieren:
Du brauchst ant, git und ein JDK. Unter Ubuntu erfüllt das Paket openjdk-6-jdk seinen Zweck. Außerdem muss relativ zur build.xml in ../libs/jdom/build/jdom.jar die JDOM-Bibliothek vorhanden sein. Dazu lädst du am Besten den aktuellen Release Build von JDOM von

    http://www.jdom.org/downloads/index.html

herunter und entpackst ihn nach ../libs/jdom. Nun sollten beim Aufrufen des Programmes ant verschiedene Versionen des Projektes vollautomatisch kompiliert und in ausführbare jars gepackt werden. Zur Zeit sind das folgende:
 - SimpleGUI.jar: Eine grafische Oberfläche die dierekt über Brodcasts mit anderen Teilnehmern kommuniziert
 - TCP-Server.jar: Ein Server der über TCP ein Interface bereitstellt um Brodcasts zu anderen Teilnehmern zu senden und Brodcasts von anderen Teilnehmen zu empfangen (zum Beispiel um über Router hinweg zu chatten oder in ThinClient-Umgebungen mehr als einen Teilnehmer mit einem Server bedienen zu können)
 - SimpleGUI-TC-Edition.jar: Identisch zu SimpleGUI, nutzt aber die Dienste eines lokal laufenden TCP-Servers
 - TUI.jar: Ein Textinterface welches mal genausoviel konnte wie die SimpleGUI doch mitlerweile ziemlich hinterherhinkt.

