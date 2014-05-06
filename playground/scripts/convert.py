import re;
import pprint;
import os;

#settings
baseDir = "/home/drew/masterproef/f4ftestsuite/trunk/monitor_site/";


############################################################################
#create datastruct
dct = {};
contexts = {}; #testid => context
results = {};  #resultid => result

for file in os.listdir(baseDir):
	if (file.endswith(".sql")):
		
		print("opening",file);
		f = open(baseDir+file,'r');
		
		#drop useless first lines
		header = "";
		if file.startswith("db_dump_flsmonitoring"): 
#testbedid, testbedname, testbedurl, pinglatency, getversionstatus, freeresources, aggregatetestbedstate, last_check
			for line in f:
				if line.find("COPY flstestbeds") != -1: 
					header = line.split(",");
					#pprint.pprint(header);
					break;

			for line in f:
				if line.find("\\.") != -1 : break; # we don't need the tail
				
				lst = line.split("\t");
				if lst[1] not in dct: 
					dct[lst[1]] = {};
					dct[lst[1]]['tests'] = {};
				dct[lst[1]]['testbedid'] = lst[0];
				dct[lst[1]]['testbedname'] = lst[1];
				dct[lst[1]]['testbedurl'] = lst[2];
				for test in header[3:6]:
					dct[lst[1]]['tests'][test] = True;

		else:
			#test_context
#id, name, category, description, contextfilename, testbed_urn
			for line in f:#remove stuffs above table content
				if line.find("COPY test_context") != -1 :
					header = line.split("(");
					header = header[1].split(")");
					header = header[0:-1];
					header = header[0].split(",");
					#pprint.pprint(header);
					break;

			for line in f:
				if line.find("\\.") != -1 :break;

				lst = line.split("\t");
				con = {};
				for i in range(len(lst)):
					con[header[i]] = lst[i];
				contexts[lst[1]] = con;

			#results
			for line in f:#remove stuffs above table content
				if line.find("COPY test_results") != -1:
					header = line.split("(");
					header = header[1].split(")");
					header = header[0:-1];
					header = header[0].split(",");
					#pprint.pprint(header);
					break;

			for line in f:
				if line.find("\\.") != -1: break;

				lst = line.split("\t");
				res = {};
				for i in range(len(lst)):
					res[header[i]] = lst[i];
				results[lst[0]] = res;



			#testbeds
#id, name, fed4fire, international, description, type, urn, interface_url, info-_url
			for line in f:
				if line.find("COPY testbeds") != -1: 
					header = line.split(",");
					#pprint.pprint(header);
					break;

			for line in f:
				if line.find("\\.") != -1: break;#we don't need the tail

				lst = line.split("\t");
				if lst[1] not in dct: 
					dct[lst[1]] = {};
					dct[lst[1]]['tests'] = {};
				dct[lst[1]]['testbedid'] = lst[0];
				dct[lst[1]]['testbedname'] = lst[1];
				dct[lst[1]]['testbedurn'] = lst[6];
				for test in header[3:6]:
					dct[lst[1]]['tests']['scenarios'] = True;


		f.close();

print("found", len(dct), "testbeds");
pprint.pprint(dct);
pprint.pprint(contexts);
pprint.pprint(results);
