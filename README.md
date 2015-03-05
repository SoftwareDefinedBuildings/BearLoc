# BearLoc

BearLoc is an indoor semantic localization service, built with data crowdsourcing and machine learning.


apps/ folder contains exemplary applications using BearLoc library. Each applications may have their own server codes.


bearloc/ contains all docs and codes of BearLoc service. doc/ is the documentation files of BearLoc, which are compatible with [Sphinx](http://sphinx-doc.org/). mobile/ is BearLoc mobile library codes, they can be imported as Android Library Project in [Eclipse](http://www.eclipse.org/) with [Android Development Tools](http://developer.android.com/tools/sdk/eclipse-adt.html) (ADT). [Here](http://developer.android.com/tools/projects/projects-eclipse.html#ReferencingLibraryProject) introduces how to import library project. server/ is BearLoc server codes in python using twisted. 


For more information and references, please visit <http://bearloc.cal-sdb.org/>.

Ubuntu Installation Instruction

1. sudo apt-get update
2. sudo apt-get install python
2. sudo apt-get install build-essential
2. sudo apt-get install python-dev
3. sudo apt-get install wget
4. cd ~\
5. git clone https://github.com/sandstorm-io/capnproto.git
6. cd capnproto/c++
7. ./setup-autotools.sh
8. autoreconf -i
9. ./configure
10. make -j6 check
11. sudo make install
12. wget https://bootstrap.pypa.io/get-pip.py
13. sudo pip install -U cython
14. sudo pip install -U setuptools
15. sudo pip install pycapnp
16. git clone <this repo's address>


You should be done
