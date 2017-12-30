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



"""This is a helper class to read the operators_list.dat file and provide a
usable object for the template files.  This would not be necessary if the object
were in the template files, but they would need to be much longer.  This
could probably be improved by being an import statement for each of the
template files.
"""


class Operator:
    def __init__(self, index, symbol, name, format, traits, defs, uses,
                 lib_name, input_class, pipe_stages=0, latency=0, area=0, slices=0):
        self.symbol = symbol
        self.name = name
        self.format = format
        self.traits = traits
        self.index = index
        self.defs = defs;
        self.uses = uses;
        
        self.lib_name = lib_name;
        self.input_class = input_class;
        self.pipe_stages = pipe_stages;
        self.latency = latency;
        self.area = area;
        self.slices = slices;
        

import re, string, sys

class Operators:
    def __init__(self, file):
        try:
            self.file = open(file, 'r+')
        except:
            sys.__stderr__.write(" unable to open file ")
            sys.__stderr__.write(file)
            sys.__stderr__.write("\n")
            sys.exit(-1)
                                
        self.operators = []

        self.text = re.compile(r'^\w+')
        self.comment = re.compile(r'^#.*$')
        self.empty = re.compile(r'^\s*$')

        sys.__stderr__.write("Operators: start\n")
        self._parsefile()
        sys.__stderr__.write("Operators: done\n")


    def _parsefile(self):
        in_text = False
        info = []
        library_elements = False
        for line in self.file.readlines():
            if self.comment.match(line): continue
            if self.text.match(line):
                info.append(string.strip(line))
            if self.empty.match(line) and len(info) > 0 :
                size = len(self.operators)
                info_lines = len(info)
                #print "// info ",info
                #print "// size ",info_lines

                lib_symbol = string.split(info[0])
                if len(lib_symbol) > 1:
                    lib_name = lib_symbol[0]
                    symbol = lib_symbol[1]
                else:
                    lib_name = "NONE";
                    symbol = lib_symbol[0]
                
                nf = string.split(info[1])
                name, format = nf[0], nf[1]
                traits = info[2]
                def_use = string.split(info[3])

                if info_lines > 4:
                    input_class = info[4]
                    sched_info = string.split(info[5])
                    pipe_stages = sched_info[0]
                    latency = sched_info[1]
                    area = sched_info[1]
		    if info_lines > 6:
		       slices = info[6]
		    else:
		       slices = "0";
		    
                else:
                    input_class = "ANY"
                    pipe_stages = "-1.0"
                    latency     = "-1.0"
                    area        = "-1.0"
                    slices        = "-1"

                if symbol is "LIBOP":
                    info = []
                    continue
                
                self.operators.append(Operator(size, symbol, name, format, traits,
                                               def_use[0], def_use[1], lib_name,
                                               input_class, pipe_stages, latency, area, slices))
                #sys.__stderr__.write("writing ")
                #sys.__stderr__.write(symbol)
                #sys.__stderr__.write("\n")
                info = []


    def getList(self):
        return self.operators
                
                
