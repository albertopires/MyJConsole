#include <stdio.h>
#include <stdlib.h>
#include <time.h>

int main( int argc , char *argv[] ) {
	const long t = atol(argv[1]);
	const struct tm *tm = localtime( &t );
	printf( "%s" , asctime(tm) );
	exit(0);
}
