run:
	twistd -y bearloc.tac

clean:
	make stop
	find . -name '*.pyc' -delete
	find . -name '*.pyo' -delete
	rm -rf twistd.log*
	rm -rf report.log*
	rm -rf audio

stop:
	if [ -a twistd.pid ] ; \
	then \
		kill `cat twistd.pid`; \
	fi;
