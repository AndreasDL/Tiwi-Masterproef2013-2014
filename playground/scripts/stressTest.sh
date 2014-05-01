#clean databank
echo "!!Cleaning ALL previous results\n";
echo "Cleaning Database";
cd psqlHome;
su postgres -c "php -f StressTestRapidClear.php";

cd ~/www;#naar juist map gaan
#vorige logfiles wissen
echo "Cleaning log files";
rm -rf results/;

echo "starting test";
java -jar stresstest-beta-jar-with-dependencies.jar --number-of-tests 3 --wait-time 1 --test-name wall2;
#testnamen zijn:
#fail => test die draait op testbeds dat niet werkt, failt direct => snelle test om te kijken of config werkt; niet nodig (amv2)
#wall1 => draait op wall1 (amv2)
#wall2 => draait op wall2 (amv2)

#in databank ookal v3; maar jar moet nog aangepast worden om dit te draaien.
#De resultaten komen terrecht in ~/www/results. Je kan ze in de browser weergeven door te surfen naar http://f4f-mon-dev.intec.ugent.be/monitoringSite/summaryView/history.php?testname=<tess-name>

#certificaten etc staan in ~/.auth/, momentaal is dit mijn certificaat.
