run:
	twistd -y bearloc.tac

clean:
	make stop
	find . -name '*.pyc' -delete
	find . -name '*.pyo' -delete
	rm -rf twistd.log*
	rm -rf report.log*

stop:
	if [ -e twistd.pid ] ; \
	then \
		kill `cat twistd.pid`; \
	fi;
