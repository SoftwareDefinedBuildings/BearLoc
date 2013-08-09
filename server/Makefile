clean:
	make stop
	find . -name '*.pyc' -delete
	find . -name '*.pyo' -delete
	rm -rf twistd.log*
	rm -rf report.log*
	rm -rf audio
	rm -rf boss.db

run:
	twistd -y boss.tac

stop:
	if [ -a twistd.pid ] ; \
	then \
		kill `cat twistd.pid`; \
	fi;
