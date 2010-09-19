#!/bin/sh
rm -r html
mkdir html
cd standarts
for i in *; do
	cat > ../html/$i.html <<EOPOHTML
<html>
<head>
<!--Die Stylesheets sind von ottimo, der Rest von keine-ahnung-->
<style type="text/css">
<!--
body
{
white-space: pre-wrap;
}
a:link { color:#FFFFFF; }
a:visited { color:#FFFFFF; }
a:active { color:#FFFFFF; }
-->
</style>
<meta http-equiv="expires" content="0">
<meta http-equiv="content-type" content="text/html; charset=UTF-8" />
<title>$i - Eine Erweiterung des BroadChat-Protokolls</title>
</head>
<body bgcolor="#000000" text="#FFFFFF" link="#FFFFFF" vlink="#FFFFFF" alink="#FFFFFF">
<h1>$i - Eine Erweiterung des BroadChat-Protokoll</h1>
<pre>
EOPOHTML
	php >> ../html/$i.html <<EOPHPCode
<?php
		\$fp = @fopen("$i", "r") or die ("Kann Datei nicht lesen.");
		while(\$line = fgets(\$fp, 1024)){
			echo htmlspecialchars(\$line);
		}
		fclose(\$fp);
?>
EOPHPCode
	cat >> ../html/$i.html <<EOPOHTML
</pre>
</body>
</html>
EOPOHTML
done