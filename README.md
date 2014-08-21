masterproef
===========

masterproef automatische monitoring for jFed
masterjaar inwe informatica ugent / tiwi


**Inhoud**

Door de huidige verschuiving naar cloudgebaseerde technologieën zal het belang van netwerkprotocollen en van de beschikbaarheid van netwerken alleen maar toenemen. Om deze verschuiving vlot te laten verlopen is er meer onderzoek nodig naar netwerktechnologieën. Dit onderzoek kan beter verlopen als onderzoekers en onderzoekscentra beter samenwerken. Hiervoor is FIRE (Future Internet Research and Experimentation) opgestart. FIRE is een Europees programma voor het uitvoeren van onderzoek naar het internet en de toekomst ervan. Hierbij werden universiteiten aangemoedigd om testbeds te bouwen. 

Fed4FIRE is het sluitstuk van dit programma, waarbij al deze testbeds een universele API implementeren: SFA. Hierdoor wordt het delen van testfaciliteiten gemakkelijker gemaakt. Om te controleren of de SFA juist geïmplementeerd is, is jFed ontworpen. jFed wordt periodiek gebruikt door beheerders om hun SFA-API implementatie te controleren. 

Deze masterproef bouwt hier verder op door een automatische monitoringsservice te maken. Deze kijkt periodiek of de SFA-API implementatie niet kapot is door nieuwe ontwikkelingen. Hierbij wordt informatie verzameld die via een monitoringsAPI ter beschikking gesteld wordt. Deze API (application programming interface) vormt een stevige basis waarvan andere toepassingen gebruik kunnen maken. 

Binnen deze masterproef wordt de monitoringsservice ook uitgebreid met een loadtester. De loadtester wordt gebruikt om loadtesten uit te voeren. Hierbij wordt gekeken hoe een testbed reageert op een bepaalde belasting.

**Inleiding**

Het gebruik van netwerken en het internet om computers en allehande randapperatuur te verbinden zal in de toekomst alleen maar stijgen. Het is dan ook van groot belang dat onderzoek op dit gebied vlot en correct verloopt en dat onderzoekers samenwerken om zo ideeën en nieuwe technologieën te delen. Daarnaast moeten er ook testfaciliteiten zijn om deze nieuwe technologieën te testen. FIRE (Future Internet Research and Experimentation) is een Europees onderzoeksprogramma dat zich op deze doelen richt. 

Om de configuratie en de werking van de verschillende testbeds gelijk te maken, is de federation architectuur ontworpen. De invoering hiervan zit in het Fed4FIRE (Federation for FIRE) project. De federation architectuur die in deze masterproef behandeld wordt, is SFA 2.0 (Slice-federation-architecture). Hierbij vormen alle testbeds van FIRE een federatie. Daardoor hebben onderzoekers binnen FIRE toegang tot alle testbeds binnen FIRE. 

Het beheer van al deze verschillende testbeds is geen sinecure. Om dit beheer te vereenvoudigen is er binnen IBCN (Internet Based Communication Networks and Services), een onderdeel van het onderzoekscentrum iMinds, een monitoringsservice gemaakt. Deze service controleert periodiek of de SFA-API nog werkt. Echter door zijn snelle ontwikkeling is de service niet voorzien op uitbreidingen. 

FIRE werkt samen met een gelijkaardig project, GENI. GENI (Global Environment for Network Innovations) is een Amerikaans project met gelijkaardige doelstellingen als FIRE. GENI is ook bezig met de ontwikkeling van een monitoringssysteem dat echter meer de nadruk legt op het monitoren van experimenten. De samenwerking tussen beide projecten kan bevorderd worden als beide monitoringssystemen compatibel zijn. 

De opdracht van deze masterproef bestaat uit 3 grote delen. Het eerste deel is het maken van een API die monitoringsdata beschikbaar maakt voor andere applicaties. Het tweede deel is een monitoringsservice maken die testbeds controleert. Het derde en laatste deel is de monitoringsservice uitbereiden om loadtesten uit te voeren. Met deze loadtesten wordt bekeken welke lading een testbed kan afhandelen.

**mappen**
* API: De api met de databank die de testen en resultaten bijhoudt. (PHP)
* documents: allerlei documenten o.a. logboek, uitgebreidvoorstel
* loadtest: de loadtester, belast een testbed om de resultaten de kunnen bekijken (java)
Hier zit nog een monotoringssite bij , omdat deze een kloon van de API gebruikt om de resultaten gescheiden te houden
* monitor: de monitor, haalt testen op en voert ze uit, stuurt resultaten terug naar API (java)
* monitoringsSite: geeft de resultaten weer (html/css/js)
* schoolSite: de bijhorende site (html/css/js/php)
* scriptie: de scriptie zelf (latex)
* scripts: allerlei scripts gebruikt tijdens masterproef
* Stage: Wat ik tijdens de stage heb bereikt (java + javaFX)

**Requirements**
* apachejava
* PHP 5.4.28
* postgresql 9.1.13 (databank)
* php_pgsql ( php<-> databank)
* OPtioneel voor converting script
* Python 3.4 of hoger (enkel voor converteren van oude testen, niet voor het systeem zelf)
* psycopg2
* python urllib.requests

**install**
* Plaatsen van certificaten in ~/.ssl
* jars moeten in de /var/www directory komen
* Service moet draaien op f4f-mon-dev.intec.ugent.be om een
*correcte verbinding met de service tot stand te kunnen brengen.
Zie ook het shell [script](scripts/deploy.sh).

**links / references**
* [jFed](http://jfed.iminds.be/)
* [iminds](http://www.iminds.be/)
* [FIRE](http://www.ict-fire.eu/home.html)
* [Fed4FIRE](http://www.fed4fire.eu/home.html)
* [ibcn](http://www.ibcn.intec.ugent.be/)
* [geni](https://www.geni.net/?page_id=2)
