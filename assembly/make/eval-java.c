#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <errno.h>
#ifdef _WIN32
#include <process.h>
#else
#include <inttypes.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <sys/wait.h>
#endif

char *quote(char *string) {
	int len = strlen(string);
	int new_len = len + 2;
	for (int i = 0; i < len; i++) {
		int k = 0;
		while (i + k < len && string[i + k] == '\\')
			k++;
		if (string[i + k] == '"')
			new_len++;
	}
	char *quoted = malloc(new_len + 1);
	int j = 0;
	quoted[j++] = '"';
	for (int i = 0; i < len; i++) {
		int k = 0;
		while (i + k < len && string[i + k] == '\\')
			k++;
		if (string[i + k] == '"')
			quoted[j++] = '\\';
		quoted[j++] = string[i];
	}
	quoted[j++] = '"';
	quoted[j++] = '\0';
	return quoted;
}

#ifdef _WIN32
#define PATH_SEPARATOR ';'
#else
#define PATH_SEPARATOR ':'
#endif

void exec_java(char *this_executable, char *java_executable, char *java_code, int argc, char **argv) {
	char *classpath = malloc(strlen(this_executable) + strlen("/../../..") + 1);
	*classpath = '\0';
	strcat(classpath, this_executable);
	strcat(classpath, "/../../..");
	char **java_argv = malloc((7 + argc) * sizeof (char *));
	int i = 0;
	java_argv[i++] = java_executable;
	java_argv[i++] = "-classpath";
	java_argv[i++] = classpath;
	java_argv[i++] = "eval_java";
	java_argv[i++] = this_executable;
	java_argv[i++] = java_code;
	for (int j = 0; j < argc; j++)
		java_argv[i++] = argv[j];
	java_argv[i] = NULL;
#ifdef _WIN32
	// because of how spawnvp works we need to quote the arguments
	// (see https://docs.microsoft.com/en-us/cpp/c-runtime-library/spawn-wspawn-functions)
	for (i = 0; java_argv[i]; i++)
		java_argv[i] = quote(java_argv[i]);
	int child_status = spawnvp(P_WAIT, java_executable, java_argv);
	if (child_status == -1) {
		fprintf(stderr, "%s: error running command `%s' (%s)\n", this_executable, java_executable, strerror(errno));
		exit(EXIT_FAILURE);
	}
	exit(child_status);
#else
	pid_t child_pid;
	pid_t w;
	int child_status;
	child_pid = fork();
	if (child_pid == -1) {
		perror("fork");
		exit(EXIT_FAILURE);
	} else if (child_pid == 0) {
		execvp(java_executable, java_argv);
		// command above only returns if error occurs
		fprintf(stderr, "%s: error running command `%s' (%s)\n", this_executable, java_executable, strerror(errno));
		exit(EXIT_FAILURE);
	} else {
		do {
			w = waitpid(child_pid, &child_status, 0);
			if (w == -1) {
				perror("waitpid");
				exit(EXIT_FAILURE);
			}
			if (WIFEXITED(child_status))
				// Note that trying to run this class with Java < 8 will result in:
				//
				//    Exception in thread "main" java.lang.UnsupportedClassVersionError: eval_java :
				//    Unsupported major.minor version 52.0
				//
				// This error can not be caught in Java and because the exit code is 1 it can not be
				// caught in C either.
				exit(WEXITSTATUS(child_status));
			else {
				fprintf(stderr, "%s: command `%s' did not exit normally\n", this_executable, java_executable);
				exit(EXIT_FAILURE);
			}
		} while (!WIFEXITED(child_status));
	}
	exit(EXIT_SUCCESS);
#endif
}

#ifndef _WIN32
void exec_java_repl_client(char *this_executable, char *java_code, int port) {
	int sockfd = socket(AF_INET, SOCK_STREAM, 0);
	if (sockfd == -1)
		// Failed to create socket. This should not happen, but fall back to evaluating Java
		// directly.
		return;
	struct sockaddr_in servaddr;
	bzero(&servaddr, sizeof(servaddr));
	servaddr.sin_family = AF_INET;
	servaddr.sin_addr.s_addr = inet_addr("127.0.0.1");
	servaddr.sin_port = htons(port);
	if (connect(sockfd, (struct sockaddr*)&servaddr, sizeof(servaddr)) != 0)
		// Failed to connect to REPL on address 127.0.0.1 and specified port. Perhaps the server was
		// started but was stopped in the meantime. Fall back to evaluating Java directly.
		return;
	write(sockfd, java_code, strlen(java_code));
	static char NEWLINE = '\n';
	write(sockfd, &NEWLINE, 1);
	FILE *in_stream= fdopen(sockfd, "r");
	unsigned int buflen = 1000;
	char buf[buflen];
	int new_line = 1;
	FILE *out_stream = stdout;
	int exit_value = 0;
	int done = 0;
	for (;;) {
		bzero(buf, buflen);
		if (fgets(buf, buflen, in_stream) == NULL)
			break;
		if (done)
			goto unexpected;
		if (new_line) {
			switch (buf[0]) {
			case '1':
				out_stream = stdout;
				break;
			case '2':
				out_stream = stderr;
				break;
			case 'x':
				if (buf[1] != ':')
					goto unexpected;
				intmax_t v = strtoimax(&buf[2], NULL, 10);
				if (errno) {
					fprintf(stderr, "%s: could not parse exit value: %s\n", this_executable, &buf[2]);
					fflush(stdout);
					fflush(stderr);
					exit(EXIT_FAILURE);
				}
				exit_value = v;
				done = 1;
				continue;
			default:
				goto unexpected;
			}
			if (buf[1] != ':')
				goto unexpected;
			fprintf(out_stream, "%s", &buf[2]);
		} else
			fprintf(out_stream, "%s", buf);
		new_line = (buf[strlen(buf) - 1] == '\n');
	}
	if (!done) {
		fprintf(stderr, "%s: server hung up without providing exit value. System.exit() might have been called.\n", this_executable);
		exit_value = 1;
	}
  cleanup:
	fclose(in_stream);
	close(sockfd);
	fflush(stdout);
	fflush(stderr);
	exit(exit_value);
  unexpected:
	fprintf(stderr, "%s: received unexpected message from server: %s\n", this_executable, buf);
	exit_value = 1;
	goto cleanup;
}
#endif

int main(int argc, char **argv) {
	char *this_executable_path = argv[0];
	if (argc < 2) {
		fprintf(stderr, "%s: expected at least one argument\n", argv[0]);
		exit(EXIT_FAILURE);
	}
	if (argc == 2 && strcmp(argv[1], "--verify") == 0)
		// just checking that the binary can be executed on the current platform
		exit(EXIT_SUCCESS);
	char *java_code = argv[1];
	int extra_argc = 0;
	char **extra_argv = NULL;
	if (argc > 2) {
		if (strcmp(argv[2], "--") != 0) {
			fprintf(stderr, "%s: unexpected argument: %s\n", argv[0], argv[2]);
			exit(EXIT_FAILURE);
		}
		extra_argc = argc - 3;
		extra_argv = &argv[3];
	}
#ifndef _WIN32
	if (extra_argc == 0) {
		char *JAVA_REPL_PORT = getenv("JAVA_REPL_PORT");
		if (JAVA_REPL_PORT && *JAVA_REPL_PORT) {
			// try to connect with REPL
			intmax_t port = strtoimax(JAVA_REPL_PORT, NULL, 10);
			if (errno) {
				fprintf(stderr, "%s: could not parse JAVA_REPL_PORT: %s\n", this_executable_path, JAVA_REPL_PORT);
				exit(EXIT_FAILURE);
			}
			if (port < 0 || port > 65535) {
				fprintf(stderr, "%s: not a valid port number: %jd\n", this_executable_path, port);
				exit(EXIT_FAILURE);
			}
			exec_java_repl_client(this_executable_path, java_code, port);
		}
	}
#endif
	char *JAVA_HOME = getenv("JAVA_HOME");
	if (JAVA_HOME) {
		char *java = malloc(strlen(JAVA_HOME) + 14);
		strcpy(java, JAVA_HOME);
#ifdef _WIN32
		strcat(java, "\\bin\\java.exe");
#else
		strcat(java, "/bin/java");
#endif
		FILE *f;
		if ((f = fopen(java, "r"))) {
			fclose(f);
			exec_java(this_executable_path, java, java_code, extra_argc, extra_argv);
		}
		free(java);
	}
	char *PATH = strdup(getenv("PATH"));
	char *dir = PATH;
	char *sep = NULL;
	do {
		sep = strchr(dir, PATH_SEPARATOR);
		if (sep != NULL)
			sep[0] = '\0';
		char *java = malloc(strlen(dir) + 10);
		strcpy(java, dir);
#ifdef _WIN32
		strcat(java, "\\java.exe");
#else
		strcat(java, "/java");
#endif
		FILE *f;
		if ((f = fopen(java, "r"))) {
			fclose(f);
			exec_java(this_executable_path, java, java_code, extra_argc, extra_argv);
		}
		free(java);
		dir = sep ? sep + 1 : NULL;
	} while (dir != NULL);
	free(PATH);
	fprintf(stderr, "%s: Java is required to run this script (but not found in JAVA_HOME or on PATH)\n", this_executable_path);
	exit(EXIT_FAILURE);
}
