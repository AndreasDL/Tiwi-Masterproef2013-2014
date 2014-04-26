LOCKFILE=/tmp/monitorLock

#add lock
if [ -e $LOCKFILE ]
then
	echo "MonitoringService already running";
	exit 1;
else
	touch $LOCKFILE;
	cd /root/www/longrun/
	java -jar /root/www/longrun/Service-beta-jar-with-dependencies.jar --threads 10 --test-name wall2,wall1,wall2v3,wall1v3,fail,failv3
	rm -f $LOCKFILE;
	exit 0;
fi
