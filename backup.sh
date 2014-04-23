rm -rvf schoolSite/*;
cp -vr site/schoolSite/* schoolSite/;

rm -rvf playground/monitoringService;
rm -rvf site/monitoringService/Service/results;

rm -rvf playground/service;
rm -rvf playground/monitoringService;

cp -vr  site/* playground/;