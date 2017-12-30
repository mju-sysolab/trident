#! /usr/bin/python
"""

 LA-CC 05-135 Trident 0.7.1

Copyright Notice
Copyright 2006 (c) the Regents of the University of California.

This Software was produced under a U.S. Government contract
(W-7405-ENG-36) by Los Alamos National Laboratory, which is operated
by the University of California for the U.S. Department of Energy. The
U.S. Government is licensed to use, reproduce, and distribute this
Software. Permission is granted to the public to copy and use this
Software without charge, provided that this Notice and any statement
of authorship are reproduced on all copies. Neither the Government nor
the University makes any warranty, express or implied, or assumes any
liability or responsibility for the user of this Software.



"""



"""
The purpose of this python code is to read the template files, echo the input text
and execute any python that it finds.  This allows us to embed python in our templates
and have fancy bulk code generation.

"""



import re, string, os, sys
class Execute:

    def __init__(self, file, prefix):
        self.file = open(file, 'r+')
        self.new_file_name = re.sub(r'template',string.strip(prefix),
                          re.sub(r'_in','',file))
        self.new_file = open(self.new_file_name,'w+')
        self.space = {}
        exec "import sys" in self.space
        exec "from execute import *" in self.space
        self.space['sys'].stdout = self.new_file
        # how do we get other objects in the space
        self.execute = False
        self.begin = re.compile(r'<\?\s*python')
        self.empty = re.compile(r'^\s+$')
        self.end = re.compile(r'\?>')
        # always last
        self._read_file()


    def _read_file(self):
        """_read_file()  Read the default file and execute python in file.

        This opens the file and echos out the plain non-python text and
        executes the python code that is found.  All the python code for
        a group is read in and executed all at once.  Then the process resumes.

        This means there may be more than one block of python code.  All python
        code executes in the same name space, so that variables can be passed
        between blocks.

        """
        # while not eof()
        buffer = ""
        for line in self.file.readlines():
            if (self.empty.match(line) and self.execute is False):
                self.new_file.write(line)
                continue
            if (self.execute is True):
                if (self.end.match(line) is not None):
                    try:
                        exec buffer in self.space
                    except:
                        # this does not work right -- I want to print the
                        # exception.
                        sys.__stderr__.write("Execute: Unexpected error, ")
                        err_msg = str(sys.exc_info()[0])
                        sys.__stderr__.write(err_msg)
                        sys.__stderr__.write("\n")
                        sys.__stderr__.write("Code ")
                        sys.__stderr__.write(buffer)
                        sys.__stderr__.write("\n")
                        #os.unlink(self.new_file_name)
                        sys.exit(-1)
                        
                    buffer = ""
                    self.execute = False
                else:
                    buffer = buffer + line
            else:
                if (self.begin.match(line) is not None):
                    self.execute = True
                else:
                    self.new_file.write(line)


    def _execute(self, line):
        """
        _execute(line) This executes the python code in the current name space.
        """
        exec line in self.space
        return
    



def parseCmdLine():
    import sys
    import getopt

    try:
        opts, args = getopt.getopt(sys.argv[1:], 'f:p:')
        # print len(opts), " args"
        if len(opts) < 1 or len(opts) > 2:
            raise getopt.error, 'need file argument'
    except getopt.error, msg:
        print msg
        print ''
        print 'usage: python.py',
        print '-f filename'
        print '-p outfile prefix'
        print ''
        sys.exit(2)
    filename = ''
    prefix = ''
    for o,a in opts:
        if o == '-f' : filename = a
        if o == '-p' : prefix = a

    return filename, prefix


def main():
    file,prefix = parseCmdLine()
    sys.__stderr__.write("Executing "+file+"\n");
    Execute(file,prefix)
    sys.__stderr__.write("Done Executing "+file+"\n");



if __name__ == '__main__':
    main()
