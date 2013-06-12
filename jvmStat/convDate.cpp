#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <sys/time.h>

// $Date: 2012-03-01 14:49:53 -0300 (Qui, 01 Mar 2012) $
// $Revision: 155 $

int isDst(void);

int main( int argc , char *argv[] )
{
	tzset();
	int dst = isDst();
	if( argc == 2 && (strcmp( argv[1] , "-h" ) == 0) ) {
		printf( "convDate v1.1a\n\n" );
		printf( "DayLight : %d\n" , dst );
	}
	if( argc == 3 ) {
		struct tm tm;
		char buf[255];
		memset( buf , 0 , 255 );
		sprintf( buf , "%s %s\n" , argv[1] , argv[2] );
		strptime(buf, "%Y-%m-%d %H:%M:%S", &tm);
		tm.tm_isdst = dst;
		printf( "%ld\n" , mktime(&tm));
	}
}

int isDst(void)
{
	time_t t;
	time(&t);

	const struct tm *tm = localtime( &t );	

	return tm->tm_isdst;
}
