/**
 * Spawns a java process with proper comandline arguments.  
 * 
 * The win32 version calls the CreateProcess system call. There exists
 * a windows compatability layer (in the win32 api) _execvp, but it has
 * extremely weird behavior, thus, we are doing it MS's way.
 *
 * define DEBUG: used to compile jlc so that it runs java without a JIT.
 *
 * usage: jlc[d]
 */
#ifndef _WINDOWS
#include <unistd.h>
#else
#include <windows.h>
#endif

#ifdef DEBUG
#define prependArgs 2
char * prepend[] = { "-Djava.compiler=none", 
                     "jltools.main.Main" };
#else
#define prependArgs 2 
char * prepend[]  = { "-mx100m", "jltools.main.Main" } ;
#endif

#define postpendArgs 0
char * postpend[] = { "" };  // cl.exe complains w/o the empty string. 

int main ( int argc, char ** argv)
{
#ifndef _WINDOWS
  int i;

  char ** args = (char**)malloc ( sizeof ( char*) * 
                                  ( prependArgs + argc + postpendArgs));

#ifdef DEBUG
  args[0] = "jlcd";
#else
  args[0] = "jlc";
#endif
  for (i = 0; i < prependArgs; args[1 + i] = prepend[i++] );
  for (i = 1; i < argc; args[prependArgs + i ] = argv[i++]);
  for (i = 0; i < postpendArgs; args[1 + prependArgs + argc] = 
           postpend[i++]);

  args[prependArgs + argc + postpendArgs] = 0;
  execvp("java", args) ; 
  // only reach here if exec fails.
  printf("ERROR - could not spawn java executable.\n");
  return 1;
#else
  STARTUPINFO si;
  PROCESS_INFORMATION pi;
  char args[2048];
  int i;
  long returnCode;
  
  ZeroMemory( &si, sizeof(si) );
  si.cb = sizeof(si);

  // prep args:
  sprintf(args, "java ");
  for (i = 0; i < prependArgs; strcat( args ,prepend[i++] ), 
         strcat( args, " "));
  for (i = 1; i < argc; strcat ( args, argv[i++]),
         strcat( args, " "));
  for (i = 0; i < postpendArgs; strcat (args, postpend[i++] ),
         strcat( args, " "));

  // Start the child process. 
  if( !CreateProcess( NULL, 
              args, // Command line. 
              NULL,
              NULL,
              FALSE,
              0,                // No creation flags. 
              NULL,             // Use parent's environment block. 
              NULL,             // Use parent's starting directory. 
              &si,
              &pi ))
  {
    printf("ERROR - could not spawn java executable. \n");
    return (1);
  }
  
  // Wait until child process exits.
  WaitForSingleObject( pi.hProcess, INFINITE );

  GetExitCodeProcess( &pi, &returnCode);

  // Close process and thread handles. 
  CloseHandle( pi.hProcess );
  CloseHandle( pi.hThread );

  return returnCode;
#endif //windows

}
 
