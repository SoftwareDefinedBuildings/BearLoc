# BearLoc

BearLoc is an indoor semantic localization service, built with data crowdsourcing and machine learning.


apps/ folder contains exemplary applications using BearLoc library. Each applications may have their own server codes.


bearloc/ contains all docs and codes of BearLoc service. doc/ is the documentation files of BearLoc, which are compatible with [Sphinx](http://sphinx-doc.org/). mobile/ is BearLoc mobile library codes, they can be imported as Android Library Project in [Eclipse](http://www.eclipse.org/) with [Android Development Tools](http://developer.android.com/tools/sdk/eclipse-adt.html) (ADT). [Here](http://developer.android.com/tools/projects/projects-eclipse.html#ReferencingLibraryProject) introduces how to import library project. server/ is BearLoc server codes in python using twisted. 


For more information and references, please visit <http://bearloc.cal-sdb.org/>.

Ubuntu Installation Instruction

1. sudo apt-get update
2. sudo apt-get upgrade
3. sudo apt-get install python
4. sudo apt-get install build-essential
6. sudo apt-get install git
7. sudo apt-get install python-dev
8. sudo apt-get install wget
9. cd ~\
10. git clone https://github.com/sandstorm-io/capnproto.git
11. cd capnproto/c++
12. ./setup-autotools.sh
13. autoreconf -i
14. ./configure
15. make -j6 check
16. sudo make install
17. cd ~/
18. wget https://bootstrap.pypa.io/get-pip.py
19. sudo python get-pip.py
20. sudo pip install -U cython
21. sudo pip install -U setuptools
22. sudo pip install pycapnp
23. git clone this-repo-address


You should be done
