CC    =	gcc
CFLAGS=	-O -c 
LIB=	-lm
TIMER=	timersun.o

all:	las2

las2.o:	las2.h

.c.o:   $*.c 
	${CC} ${CFLAGS} $*.c

las2:	las2.o ${TIMER}
	${CC} -o $@ las2.o ${TIMER} ${LIB}

clean:
	del las2.o
	del timersun.o
	del las2.exe
