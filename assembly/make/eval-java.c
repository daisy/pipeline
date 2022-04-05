#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <errno.h>
#ifdef _WIN32
#include <process.h>
#else
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

void exec_java(char *java_executable, char *java_code) {
#ifdef _WIN32
	// because of how spawnvp works we need to quote the arguments
	// (see https://docs.microsoft.com/en-us/cpp/c-runtime-library/spawn-wspawn-functions)
	char *java_argv[6] = { quote(java_executable), "-classpath", "make;make/java", "eval_java", quote(java_code), NULL };
	int child_status = spawnvp(P_WAIT, java_executable, java_argv);
	if (child_status == -1) {
		fprintf(stderr, "error running command `%s' (%s)\n", java_executable, strerror(errno));
		exit(EXIT_FAILURE);
	}
	exit(child_status);
#else
	char *java_argv[6] = { java_executable, "-classpath", "make:make/java", "eval_java", java_code, NULL };
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
		fprintf(stderr, "error running command `%s' (%s)\n", java_executable, strerror(errno));
		exit(EXIT_FAILURE);
	} else {
		do {
			w = waitpid(child_pid, &child_status, 0);
			if (w == -1) {
				perror("waitpid");
				exit(EXIT_FAILURE);
			}
			if (WIFEXITED(child_status))
				exit(WEXITSTATUS(child_status));
			else {
				fprintf(stderr, "command `%s' did not exit normally\n", java_executable);
				exit(EXIT_FAILURE);
			}
		} while (!WIFEXITED(child_status));
	}
	exit(EXIT_SUCCESS);
#endif
}

#ifdef _WIN32
#define PATH_SEPARATOR ';'
#else
#define PATH_SEPARATOR ':'
#endif

int main(int argc, char **argv) {
	char *java_code = argv[1];
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
			exec_java(java, java_code);
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
			exec_java(java, java_code);
		}
		free(java);
		dir = sep ? sep + 1 : NULL;
	} while (dir != NULL);
	free(PATH);
	fprintf(stderr, "java not found in JAVA_HOME or on PATH\n");
	exit(EXIT_FAILURE);
}
