LOCKFILE=/tmp/monitorLock

#add lock
if [ -e $LOCKFILE ]
then
	echo "MonitoringService already running";
	exit 1;
else
	touch $LOCKFILE;
	java -jar /root/www/Service-beta-jar-with-dependencies.jar --threads 10 --test-name wall1,wall1v3,wall2,wall2v3
	rm -f $LOCKFILE;
	exit 0;
fi
