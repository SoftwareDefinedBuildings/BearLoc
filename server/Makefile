clean:
	find . -name '*.pyc' -delete
	find . -name '*.pyo' -delete
	rm -rf twistd.log*

run:
	twistd -y boss.tac

stop:
	kill `cat twistd.pid`
