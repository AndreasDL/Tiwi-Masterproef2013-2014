with view as (
	select t.testinstanceid as A,* from testinstances t 
		join (select * from parameterInstances) p on t.testinstanceid = p.testinstanceid
	)
select * from view 
where A = any(select A from view where parametervalue IN ('urn-testbed1','urn-testbed2') and testtype IN ('ping','stitch'));

