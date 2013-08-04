#   File:       las2.make
#   Target:     las2
#   Sources:    las2.c timermac.c
#   Created:    Saturday, March 27, 1993 1:41:47 PM


OBJECTS = las2.c.o timermac.c.o



las2 ÄÄ las2.make {OBJECTS}
	Link -d -c 'MPS ' -t MPST ¶
		{OBJECTS} ¶
		"{CLibraries}"Clib881.o ¶
		"{CLibraries}"CSANELib881.o ¶
		"{CLibraries}"Math881.o ¶
		#"{CLibraries}"Complex881.o ¶
		"{CLibraries}"StdClib.o ¶
		"{Libraries}"Stubs.o ¶
		"{Libraries}"Runtime.o ¶
		"{Libraries}"Interface.o ¶
		"{Libraries}"ToolLibs.o ¶
		-o las2
las2.c.o Ä las2.make las2.c
	 C -r  -mc68020 -mc68881 las2.c
timermac.c.o Ä las2.make timermac.c
	 C -r  -mc68020 -mc68881 timermac.c
