with view as (
	select *,r.resultid id from results r
    	join (select * from subresults) sr on r.resultid = sr.resultid
    	join (select * from testinstances) ti on ti.testinstanceid = r.testinstanceid
    	join (select * from parameterinstances) pi on pi.testinstanceid = r.testinstanceid
)
select * from ( 
	select *,dense_rank() over(partition by testname,testtype order by timestamp desc) rank from view
) vv