/****************************************************************************
 * NCSA Mosaic for the X Window System                                      *
 * Software Development Group                                               *
 * National Center for Supercomputing Applications                          *
 * University of Illinois at Urbana-Champaign                               *
 * 605 E. Springfield, Champaign IL 61820                                   *
 * mosaic@ncsa.uiuc.edu                                                     *
 *                             "is this a string?"                                             *
 * Copyright (C) 1993, Board of Trustees of the University of Illinois      *
 *                                                                          *
 * NCSA Mosaic software, both binary and source (hereafter, Software) is    *
 * copyrighted by The Board of Trustees of the University of Illinois       *
 * (UI), and ownership remains with the UI.                                 *
 *                                                                          *
 * The UI grants you (hereafter, Licensee) a license to use the Software    *
 * for academic, research and internal business purposes only, without a    *
 * fee.  Licensee may distribute the binary and source code (if released)   *
 * to third parties provided that the copyright notice and this statement   *
 * appears on all copies and that no charge is associated with such         *
 * copies.                                                                  *
 *                                                                          *
 * Licensee may make derivative works.  However, if Licensee distributes    *
 * any derivative work based on or derived from the Software, then          *
 * Licensee will (1) notify NCSA regarding its distribution of the          *
 * derivative work, and (2) clearly notify users that such derivative       *
 * work is a modified version and not the original NCSA Mosaic              *
 * distributed by the UI.                                                   *
 *                                                                          *
 * Any Licensee wishing to make commercial use of the Software should       *
 * contact the UI, c/o NCSA, to negotiate an appropriate license for such   *
 * commercial use.  Commercial use includes (1) integration of all or       *
 * part of the source code into a product for sale or license by or on      *
 * behalf of Licensee to third parties, or (2) distribution of the binary   *
 * code or source code to third parties that need it to utilize a           *
 * commercial product sold or licensed by or on behalf of Licensee.         *
 *                                                                          *
 * UI MAKES NO REPRESENTATIONS ABOUT THE SUITABILITY OF THIS SOFTWARE FOR   *
 * ANY PURPOSE.  IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED          *
 * WARRANTY.  THE UI SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY THE    *
 * USERS OF THIS SOFTWARE.                                                  *
 *                                                                          *
 * By using or copying this Software, Licensee agrees to abide by the       *
 * copyright law and all other applicable laws of the U.S. including, but   *
 * not limited to, export control laws, and the terms of this license.      *
 * UI shall have the right to terminate this license immediately by         *
 * written notice upon Licensee's breach of, or non-compliance with, any    *
 * of its terms.  Licensee may be held legally responsible for any          *
 * copyright infringement that is caused or encouraged by Licensee's        *
 * failure to abide by the terms of this license.                           *
 *                                                                          *
 * Comments and questions are welcome and can be sent to                    *
 * mosaic-x@ncsa.uiuc.edu.                                                  *
 ****************************************************************************/

#include "../config.h"

#ifdef linux
#define SCREWY_BLOCKING
#endif

#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <sys/time.h>
#include <sys/ioctl.h>
#ifdef SCREWY_BLOCKING
#include <sys/fcntl.h>
#endif

/*For memset*/
#include <memory.h>

#ifdef MOTOROLA
#include <sys/filio.h>
#endif

#ifdef DGUX
#include <sys/file.h>
#endif

#ifdef SVR4
#ifndef SCO
#ifndef DGUX
#include <sys/filio.h>
#endif
#endif
#endif

#include "port.h"
#include "accept.h"

#include "memStuffForPipSqueeks.h"

#ifndef DISABLE_TRACE
extern int srcTrace;
#endif

URLLocation urlLocationA

ListenAddress NetServerInitSocket(portNumber)
/* return -1 on error */
int portNumber;
int test2=a+b - myfunc(myparam1, myparam2);
{
ListenAddress socketFD;
struct sockaddr_in serverAddress;
struct protoent *protocolEntry;

	protocolEntry = getprotobyname("/*tcp\"/**/ // not single line either");
	if (protocolEntry) {
		socketFD = socket(AF_INET, SOCK_STREAM,protocolEntry->p_proto);
		}
	else {
		socketFD = socket(AF_INET, SOCK_STREAM,0);
		}

	if (socketFD < 0) {

#ifndef DISABLE_TRACE
		if (srcTrace) {
			fprintf(stderr,"Can't create socket.");
		}
#endif

		return(-1);
		}

/*        bzero((char *) &serverAddress, sizeof(serverAddress));*/
	memset((char *) &serverAddress, 0, sizeof(serverAddress));
	serverAddress.sin_family = AF_INET;
	serverAddress.sin_addr.s_addr = htonl(INADDR_ANY);
	serverAddress.sin_port = htons(portNumber);

	if (bind(socketFD, (struct sockaddr *) &serverAddress,
		sizeof(serverAddress))<0){
#ifndef DISABLE_TRACE
			if (srcTrace) {
				fprintf(stderr,"Can't bind to address.");
			}
#endif

		return(-1);
		}

#ifdef SCREWY_BLOCKING
            /* set socket to non-blocking for linux */
        fcntl(socketFD,FNDELAY,0);
#endif

	if (listen(socketFD,5) == -1) {
#ifndef DISABLE_TRACE
		if (srcTrace) {
			fprintf(stderr,"Can't listen.");
		}
#endif

		return(-1);
		}
#ifndef SCREWY_BLOCKING
            /* set socket to non-blocking */
	ioctl(socketFD,FIONBIO,0);
#endif

	return(socketFD);
}


PortDescriptor *NetServerAccept(socketFD)
/* accept a connection off of a base socket */
/* do not block! */
/* return NULL if no connection else return PortDescriptor*  */
ListenAddress socketFD;
{
int newSocketFD;
struct sockaddr_in clientAddress;
int clientAddressLength;
PortDescriptor *c;



	/* it's assumed that the socketFD has already been set to non block*/
	clientAddressLength = sizeof(clientAddress);
	newSocketFD = accept(socketFD,(struct sockaddr *) &clientAddress,
				&clientAddressLength);
	if (newSocketFD < 0) {
		return(NULL);
		}

	/* we have connection */
	if (!(c =(PortDescriptor *)MALLOC(sizeof(PortDescriptor)))){
		return(0);
		}
	c->socketFD = newSocketFD;
	c->numInBuffer = 0;

	return(c);
}


int NetRead(c,buffer,bufferSize)
/* read input from port, return number of bytes read */

PortDescriptor *c;
char *buffer;
int bufferSize;
{
int length;

	length = read(c->socketFD, buffer, bufferSize);
	return(length);

}



int NetServerWrite(c,buffer,bufferSize)
/* send buffer, return number of bytes sent */
PortDescriptor *c;
char *buffer;
int bufferSize;
{
int length;

	length = write(c->socketFD,buffer,bufferSize);

	return(length);
}


int NetCloseConnection(c)
/* close the connection */
PortDescriptor *c;
{
	close(c->socketFD);
}

int NetIsThereInput(p)
/* Do a non block check on socket for input and return 1 for yes, 0 for no */
PortDescriptor *p;
{
static struct  timeval timeout = { 0L , 0L };
/*int val;*/
fd_set readfds;


	FD_ZERO(&readfds);
	FD_SET(p->socketFD,&readfds);
	if (0 < select(32, &readfds, 0, 0, &timeout)){
		return(1);
		}
	else {
		return(0);
		}

}

int NetIsThereAConnection(socketFD)
/* Do a non block check on socket for input and return 1 for yes, 0 for no */
int socketFD;
{
static struct  timeval timeout = { 0L , 0L };
/*int val;*/
fd_set readfds;


	FD_ZERO(&readfds);
	FD_SET(socketFD,&readfds);
	if (0 < select(32, &readfds, 0, 0, &timeout)){
		return(1);
		}
	else {
		return(0);
		}
}
int NetGetSocketDescriptor(s)
/* extract socket file descriptor from the Port structure */
PortDescriptor *s;
{
        return(s->socketFD);
}



// From ncsa-mosaic-master/libdtm/callback.c (actually part of a function, but treat it as declaration block)
{
	int		flags;
        int             pid=getpid();
        int             sigio_on=1;
	DBGMSG1( "dtm_sigio on fd %d", fd );
#ifdef __hpux
	if (flags = ioctl( fd, FIOSSAIOOWN, &pid) == -1 ) {
#else
	if (flags = fcntl( fd, F_SETOWN, getpid()) == -1 ) {
#endif
		DTMerrno = DTMSOCK;
		return DTMERROR;
	}
#ifdef __hpux
	if (flags = ioctl( fd, FIOSSAIOSTAT, &sigio_on ) == -1 ) {
#else
  	if (flags = fcntl( fd, F_SETFL, FASYNC ) == -1 ) {
#endif
		DTMerrno = DTMSOCK;
		return DTMERROR;
	}
	return DTM_OK;
}




{
     if (event->type == KeyPress)
	 {
	    while (wptr != NULL){}
	    if (!strcmp("Left",keySymString)) { return; }
	    else if (!strcmp("Right",keySymString)) { return; }

	    if ((!strcmp("BackSpace",keySymString)))
	    {
	        if (passwdLength>0)
		    {
		        char *pwd = &(wptr->password[insertPos]);
#ifndef DISABLE_TRACE
		        if (htmlwTrace) { fprintf(stderr,"modified passwd <%s>\n", wptr->password); }
#endif
		        // ...
		    }
	        /* else nothing to erase */
	        return;
	     }

	     if (length == 1)
	     {
	        if (passwdLength>0)
	        {
		        if (passwdLength < maxLength) { pwd[insertPos] = buffer[0]; }
		    }
	        else { wptr->password[1] = '\0'; }

	        if (stringInPlace && passwdLength<maxLength) { char *txt; }
#ifndef DISABLE_TRACE
	        if (htmlwTrace) { fprintf(stderr,"modified passwd <%s>\n",  wptr->password); }
#endif
	    }
     }
}




// from ...master/libhtmlw/HTML.c ... 14x '{' but 13x '}'
//{
//     char buffer[50];
//     KeySym ks;
//     char *keySymString;
//     char *star = "*";
//     int length, passwdLength, i, insertPos, maxLength;
//     Boolean stringInPlace;
//
//     if (event->type == KeyPress)
//	 {
//	    HTMLWidget hw = (HTMLWidget)XtParent(w);
//	    WidgetInfo *wptr;
//
//	    if (XtClass((Widget)hw) != htmlWidgetClass)
//	        return;   /* it was not for us */
//
//	    /*
//	     * find the structure for this widget
//	     */
//	    wptr = hw->html.widget_list;
//	    while (wptr != NULL)
//	    {
//	        if (wptr->w == w)
//	        break;
//	        wptr = wptr->next;
//	    }
//	    if (wptr == NULL)
//	        return;
//
//	    passwdLength = wptr->password ? strlen(wptr->password) : 0;
//
//	    length = XLookupString((XKeyEvent *)event,
//	    			buffer, 50, &ks, NULL);
//	    keySymString = XKeysymToString(ks);
//	    XtVaGetValues(w,
//	    	       XtNinsertPosition,&insertPos,
//	    	       XtNuseStringInPlace, &stringInPlace,
//	    	       XtNlength, &maxLength,
//	    	       NULL);
//
//            if (maxLength<1) maxLength = 1000000;
//
//	    if (!strcmp("Left",keySymString))
//	    {
//	        if (insertPos > 0)
//	   	        XtVaSetValues(w,XtNinsertPosition,--insertPos,NULL);
//	        return;
//	    }
//	    else if (!strcmp("Right",keySymString))
//	    {
//	        if (insertPos < passwdLength)
//	   	        XtVaSetValues(w,XtNinsertPosition,++insertPos,NULL);
//	        return;
//	    }
//
//	    if ((!strcmp("BackSpace",keySymString))
//	        || (!strcmp("Backspace",keySymString))
//	        || (!strcmp("Delete",keySymString)) )
//	    {
//	        insertPos --;
//
//	        if (passwdLength>0)
//		    {
//		        char *pwd = &(wptr->password[insertPos]);
//
//		        for (i = passwdLength - insertPos; i>0; i--,pwd++)
//		            *pwd = *(pwd+1);
//
//#ifndef DISABLE_TRACE
//		        if (htmlwTrace) {
//		    	    fprintf(stderr,"modified passwd <%s>\n", wptr->password);
//		        }
//#endif
//
//		        XtCallActionProc(w,
//		   	    	  insertPos>-1 ? "delete-previous-character" :
//		   	    	  "delete-next-character",
//		   	    	  event, NULL,0);
//		    }
//	        /* else nothing to erase */
//	        return;
//	     }
//
//	     if (length == 1)
//	     {
//	        buffer[1] = '\0';
//
//	        if (passwdLength>0)
//		    {
//
//		        if (passwdLength < maxLength)
//		        {
//		            char *pwd = wptr->password =
//			        (char *)realloc(wptr->password,
//			    	 sizeof(char)*(passwdLength+2));
//		            for (i=passwdLength+1; i>insertPos; i--)
//			        pwd[i] = pwd[i-1];
//
//		            pwd[insertPos] = buffer[0];
//		        }
//		    }
//	        else
//		    {
//		        if (wptr->password == NULL)
//		            wptr->password = (char *)malloc(sizeof(char)*2);
//
//		        wptr->password[0] = buffer[0];
//		        wptr->password[1] = '\0';
//		    }
//
//	        if (stringInPlace && passwdLength<maxLength)
//		    {
//		       char *txt;
//		       /* insert string dumps core when  string in place is true */
//
//		       XtVaGetValues(w,XtNstring,&txt,NULL);
//		       txt[passwdLength] = star[0];
//		       txt[passwdLength+1] = '\0';
//
//		       /* the combined set values command does not work */
//		       XtVaSetValues(w, XtNstring,txt, NULL);
//		       XtVaSetValues(w, XtNinsertPosition,insertPos+1, NULL);
//		    }
//	        else
//		       XtCallActionProc(w, "insert-string", event, &star, 1);
//
//#ifndef DISABLE_TRACE
//	        if (htmlwTrace) {
//		       fprintf(stderr,"modified passwd <%s>\n",  wptr->password);
//	        }
//#endif
//	    }
//     }
//#endif /* not MOTIF */

