select *,t.testtype tetyp from testdefinitions t
	join (select * from parameterdefinitions) p on p.testtype = t.testtype
        join (select * from returndefinitions)    r on r.testtype = t.testtype
