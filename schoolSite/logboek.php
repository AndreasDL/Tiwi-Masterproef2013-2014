<!DOCTYPE HTML>
<!--
	ZeroFour 2.5 by HTML5 UP
	html5up.net | @n33co
	Free for personal and commercial use under the CCA 3.0 license (html5up.net/license)
-->
<html>
	<head>
		<title>Automatische testbed monitoring voor jFed - Fed4Fire - Logboek</title>
		<meta http-equiv="content-type" content="text/html; charset=utf-8" />
		<meta name="description" content="" />
		<meta name="keywords" content="" />
		<link href="http://fonts.googleapis.com/css?family=Open+Sans:400,300,700,800" rel="stylesheet" type="text/css" />
		<script src="js/jquery.min.js"></script>
		<script src="js/jquery.dropotron.min.js"></script>
		<script src="js/config.js"></script>
		<script src="js/skel.min.js"></script>
		<script src="js/skel-panels.min.js"></script>
		<noscript>
			<link rel="stylesheet" href="css/skel-noscript.css" />
			<link rel="stylesheet" href="css/style.css" />
			<link rel="stylesheet" href="css/style-desktop.css" />
		</noscript>
		<!--[if lte IE 9]><link rel="stylesheet" href="css/ie9.css" /><![endif]-->
		<!--[if lte IE 8]><script src="js/html5shiv.js"></script><link rel="stylesheet" href="css/ie8.css" /><![endif]-->
		<!--[if lte IE 7]><link rel="stylesheet" href="css/ie7.css" /><![endif]-->
	</head>
	<body class="no-sidebar">
		<!-- Header Wrapper -->
			<div id="header-wrapper">
				<div class="container">
					<div class="row">
						<div class="12u">
							<!-- Header -->
								<header id="header">
									<div class="inner">
										<!-- Logo -->
											<h1><a href="index.html" id="logo">Home</a></h1>
										<!-- Nav -->
											<nav id="nav">
												<ul>
													<li><a href="info.html">Info</a></li>
													<li><a href="doelstellingen.html">doelstellingen</a></li>
													<li>
														<span>Documenten</span>
														<ul>
															<li><a href="docs/eersteVoorstel.pdf">Eerste voorstel</a></li>
															<li><a href="docs/UitgebreidVoorstel.pdf">Uitgebreid voorstel</a></li>
															<li><a href="docs/usecases.pdf">Use Cases</a></li>
														</ul>
													</li>
													<li>
														<span>References</span>
														<ul>
															<li><a href="docs/SFA2.0.pdf">Slice-Based Federation Architecture</a></li>
															<li><a href="http://doc.fed4fire.eu/">fed4fire Doc</a></li>
															<li><a href="http://jfed.iminds.be/">jFed webstart</a></li>
														</ul>
													</li>
													<li><a href="logboek.php">Logboek</a></li>
													<li><a href="contact.html">Contact</a></li>
												</ul>
											</nav>
									</div>
								</header>
						</div>
					</div>
				</div>
			</div>
		<!-- Main Wrapper -->
			<div id="main-wrapper" align = center>
				<div class="main-wrapper-style2">
					<div class="inner">
						<div class="container">
							<div class="row">
								<div class="12u skel-cell-important">
									<div id="content">
										<!-- Content -->
											<article>
												<header class="major">
													<h2>Automatische testbed monitoring voor jFed - Fed4Fire</h2>
													<span class="byline">Een masterproef door  Andreas De Lille</span>
												</header>
												<a href="docs/logboek.ods" class=button>logboek.ods</a>
												<a href="docs/logboek.csv" class=button>logboek.csv</a>
												<a href="docs/logboek.pdf" class=button>logboek.pdf</a>
												<table>
													<?php 
														$file = fopen("./docs/logboek.csv","r");

														//header
														$line=fgetcsv($file,0,',','"','\\');
														echo "<tr>";
														echo "<td><h3>$line[0]</h3></td>";
														echo "<td><h3>$line[1]</h3></td>";
														echo "<td><h3>$line[2]</h3></td>";
														echo "<td><h3>$line[3]</h3></td>";
														echo "<td><h3>$line[6]</h3></td>";
														echo "</tr>";

														//next
														while( !feof($file) ){
															//echo fgets($file);
															$line=fgetcsv($file,0,',','"','\\');
															echo "<tr>";
															echo "<td>$line[0]</td>";
															echo "<td>$line[1]</td>";
															echo "<td>$line[2]</td>";
															echo "<td>$line[3]</td>";
															echo "<td>$line[6]</td>";
															echo "</tr>";
														}
														fclose($file);
													?>
												</table>
												<!--
													<a href="info.html" class="button">Uitleg</a>
													<a href="docs/eersteVoorstel.pdf" class="button">Eerste voorstel</a>
													<a href="docs/UitgebreidVoorstel.pdf" class="button">Uitgebreid voorstel</a>
													<a href="logboek.php" class="button">Logboek</a>-->
											</article>
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>
		<!-- Footer Wrapper -->
			<div id="footer-wrapper">
				<footer id="footer" class="container">
					<div class="row" align=center>
						<div class="4u">
							<!-- Links -->
								<section>
									<h2>Organisatie</h2>
									<ul class="style2">
										<li><a href="http://www.iminds.be/nl">iMinds</a></li>
										<li><a href="http://www.ibcn.intec.ugent.be/">ibcn</a></li>
									</ul>
								</section>
						</div>
						<div class="4u">
							<!-- Links -->
								<section>
									<h2>Project</h2>
									<ul class="style2">
										<li><a href="http://fed4fire.eu/">fed4fire</a></li>
										<li><a href="http://www.iminds.be/en/develop-test/ilab-t/virtual-wall">Virtual Wall</a></li>
									</ul>
								</section>
						</div>
						<div class="4u">
								<!-- Links -->
								<section>
									<h2>School</h2>
									<ul class="style2">
										<li><a href="http://tiwi.ugent.be/">Tiwi</a></li>
										<li><a href="http://www.ugent.be/">uGent</a></li>
									</ul>
								</section>
						</div>
					</div>
					<div class="row">
						<div class="12u">
							<div id="copyright">
								&copy; Untitled. All rights reserved | Images: <a href="http://fotogrph.com/">fotogrph</a> | Design: <a href="http://html5up.net/">HTML5 UP</a>
							</div>
						</div>
					</div>
				</footer>
			</div>

	</body>
</html>