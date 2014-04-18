rm -rvf schoolSite/*;
cp -vr site/schoolSite/* schoolSite/;

rm -rvf playground/service/*;
cp -vr site/service/* playground/service/;

rm -rvf playground/monitoringSite/*;
cp -vr site/monitoringSite/* playground/monitoringSite/;

rm -rvf playground/Monitor/*;
rm -rvf site/results;
rm -rvf site/Monitor/results;
cp -vr site/Monitor/* playground/Monitor/;
