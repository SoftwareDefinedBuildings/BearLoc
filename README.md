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
5. sudo apt-get install python-dev
6. sudo apt-get install wget
7. cd ~\
8. git clone https://github.com/sandstorm-io/capnproto.git
9. cd capnproto/c++
10. ./setup-autotools.sh
11. autoreconf -i
12. ./configure
13. make -j6 check
14. sudo make install
15. wget https://bootstrap.pypa.io/get-pip.py
16. sudo pip install -U cython
17. sudo pip install -U setuptools
18. sudo pip install pycapnp
19. git clone this-repo-address


You should be done
