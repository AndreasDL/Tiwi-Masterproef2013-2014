#rm -rvf schoolSite/*;
#cp -vr site/schoolSite/* schoolSite/;

rm -rvf playground/API;
rm -rvf playground/monitoringSite;
rm -rvf playground/monitor;
rm -rvf schoolSite/;

rm -rvf site/monitor/monitorService/results;
rm -rvf site/monitor/stresstest/results;
rm -rvf site/results;

cp -vr  site/* playground/;
mv playground/schoolSite .;
