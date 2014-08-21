LOCKFILE=/tmp/monitorLock

#add lock
if [ -e $LOCKFILE ]
then
	echo "MonitoringService already running";
	exit 1;
else
	touch $LOCKFILE;
	cd /root/www/longrun/
	DATE=$(date);
	echo "startup at $DATE";
	java -jar /root/www/longrun/Service-beta-jar-with-dependencies.jar --threads 10;
	rm -f $LOCKFILE;
	exit 0;
fi
